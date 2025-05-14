package pt.uminho.di;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import pt.uminho.di.proto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for communication with the SC server via gRPC
 */
public class SCClient {
    private ManagedChannel channel;
    private Rx3ChatServiceGrpc.RxChatServiceStub stub;
    private Disposable messageSubscription = null;

    private ServerInfo currentServer = null;
    private String currentTopic = null;
    private List<ServerInfo> topicServers = new ArrayList<>();

    private String username;

    /**
     * Set the username for this client
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the current topic
     */
    public String getCurrentTopic() {
        return currentTopic;
    }

    /**
     * Set the available servers for the current topic
     */
    public void setTopicServers(List<ServerInfo> servers) {
        this.topicServers = new ArrayList<>(servers);
    }

    /**
     * Connect to SC server
     */
    public boolean connectToServer(ServerInfo server) {
        try {
            // Close previous connection if exists
            if (channel != null && !channel.isShutdown()) {
                disconnectFromServer();
            }

            // Create new connection
            this.channel = ManagedChannelBuilder
                    .forAddress(server.getIp(), server.getPort())
                    .usePlaintext()  // For development only - no TLS
                    .build();

            this.stub = Rx3ChatServiceGrpc.newRxStub(channel);
            this.currentServer = server;

            System.out.println("Connected to SC server at " + server);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to SC server at " + server + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnect from current SC server
     */
    public void disconnectFromServer() {
        if (channel != null && !channel.isShutdown()) {
            try {
                // Cancel message subscription if active
                if (messageSubscription != null && !messageSubscription.isDisposed()) {
                    messageSubscription.dispose();
                    messageSubscription = null;
                }

                // Leave current topic if we're in one
                if (currentTopic != null) {
                    leaveTopic();
                }

                // Shutdown the channel with a timeout
                channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
                channel = null;
                stub = null;
                currentServer = null;

                System.out.println("Disconnected from SC server");
            } catch (Exception e) {
                System.err.println("Error disconnecting from SC server: " + e.getMessage());
            }
        }
    }

    /**
     * Try to connect to any available SC for the current topic
     */
    public boolean reconnectToAnyServer() {
        // Remove the current server from the list if it exists
        if (currentServer != null) {
            topicServers.remove(currentServer);
        }

        // Try to connect to any available server
        for (ServerInfo server : topicServers) {
            if (connectToServer(server)) {
                // Rejoin the topic
                if (currentTopic != null) {
                    joinTopic(currentTopic);
                }
                return true;
            }
        }

        System.out.println("Failed to connect to any SC server for topic: " + currentTopic);
        currentTopic = null;
        return false;
    }

    /**
     * Join a chat topic
     */
    public void joinTopic(String topic) {
        if (stub == null) {
            System.out.println("Not connected to any server.");
            return;
        }

        // Create the join request
        JoinLeaveRequest request = JoinLeaveRequest.newBuilder()
                .setTopic(topic)
                .setUsername(username)
                .build();

        // Join the topic
        Single<JoinLeaveResponse> response = stub.joinTopic(Single.just(request));
        response.blockingSubscribe(
                res -> {
                    if (res.getSuccess()) {
                        System.out.println("Successfully joined topic: " + topic);
                        currentTopic = topic;
                        // Subscribe to messages
                        subscribeToMessages(topic);
                        // Get and display active users
                        displayActiveUsers(topic);
                        // Get recent chat history
                        displayChatHistory(topic, 10, null);
                    } else {
                        System.out.println("Failed to join topic: " + res.getMessage());
                    }
                },
                error -> {
                    System.err.println("Error joining topic: " + error.getMessage());
                    // Try to reconnect to another server
                    System.out.println("Trying to reconnect to another server...");
                    reconnectToAnyServer();
                }
        );
    }

    /**
     * Leave the current topic
     */
    public void leaveTopic() {
        if (currentTopic == null) {
            System.out.println("You are not currently in any topic.");
            return;
        }

        // Cancel message subscription if active
        if (messageSubscription != null && !messageSubscription.isDisposed()) {
            messageSubscription.dispose();
            messageSubscription = null;
        }

        // Create the leave request
        JoinLeaveRequest request = JoinLeaveRequest.newBuilder()
                .setTopic(currentTopic)
                .setUsername(username)
                .build();

        Single<JoinLeaveResponse> response = stub.leaveTopic(Single.just(request));
        response.blockingSubscribe(
                res -> {
                    if (res.getSuccess()) {
                        System.out.println("Successfully left topic: " + currentTopic);
                        currentTopic = null;
                    } else {
                        System.out.println("Failed to leave topic: " + res.getMessage());
                    }
                },
                error -> {
                    System.err.println("Error leaving topic: " + error.getMessage());
                    // Consider the topic left anyway
                    currentTopic = null;
                }
        );
    }

    /**
     * Send a message to the current topic
     */
    public void sendMessage(String content) {
        if (currentTopic == null) {
            System.out.println("You are not currently in any topic. Join a topic first.");
            return;
        }

        // Create the send message request
        SendMessageRequest request = SendMessageRequest.newBuilder()
                .setTopic(currentTopic)
                .setUsername(username)
                .setContent(content)
                .build();

        // Send the message
        Single<SendMessageResponse> response = stub.sendMessage(Single.just(request));

        response.blockingSubscribe(
                res -> {
                    if (!res.getSuccess()) {
                        System.out.println("Failed to send message: " + res.getMessage());
                    }
                },
                error -> {
                    System.err.println("Error sending message: " + error.getMessage());
                    // Try to reconnect to another server
                    System.out.println("Connection lost. Trying to reconnect to another server...");
                    if (reconnectToAnyServer()) {
                        // Resend the message
                        sendMessage(content);
                    }
                }
        );
    }

    /**
     * Subscribe to messages in the current topic
     */
    private void subscribeToMessages(String topic) {
        // Dispose previous subscription if it exists
        if (messageSubscription != null && !messageSubscription.isDisposed()) {
            messageSubscription.dispose();
        }

        // Create the subscribe request
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setTopic(topic)
                .setUsername(username)
                .build();

        Flowable<ChatMessage> messageFlowable = stub.subscribeToTopic(Single.just(request));

        messageSubscription = messageFlowable.subscribe(
                message -> {
                    if (!message.getUsername().equals(username)) {
                        System.out.println("\n[" + message.getUsername() + "]: " + message.getContent());
                        System.out.print("> ");
                    }
                },
                error -> {
                    System.err.println("\nError in message subscription: " + error.getMessage());
                    System.out.println("Connection lost. Trying to reconnect to another server...");
                    reconnectToAnyServer();
                    System.out.print("> ");
                }
        );
    }

    /**
     * Display active users in the current topic
     */
    public void displayActiveUsers(String topic) {
        GetUsersRequest request = GetUsersRequest.newBuilder()
                .setTopic(topic)
                .setUsername(username)
                .build();

        Single<GetUsersResponse> response = stub.getActiveUsers(Single.just(request));

        response.blockingSubscribe(
                res -> {
                    if (res.getSuccess()) {
                        System.out.println("Active users in " + topic + ":");
                        List<String> users = res.getUsernamesList();
                        for (String user : users) {
                            System.out.println("- " + user + (user.equals(username) ? " (you)" : ""));
                        }
                    } else {
                        System.out.println("Failed to get active users: " + res.getMessage());
                    }
                },
                error -> {
                    System.err.println("Error getting active users: " + error.getMessage());
                    System.out.println("Connection lost. Trying to reconnect to another server...");
                    reconnectToAnyServer();
                }
        );
    }

    /**
     * Display chat history for the current topic
     */
    public void displayChatHistory(String topic, int limit, String filterUsername) {
        // Create the request builder
        GetLogRequest.Builder requestBuilder = GetLogRequest.newBuilder()
                .setTopic(topic)
                .setHasLimit(true)
                .setLimit(limit);

        // Add username filter if provided
        if (filterUsername != null) {
            requestBuilder.setHasUsername(true)
                    .setUsername(filterUsername);
        }

        GetLogRequest request = requestBuilder.build();

        Single<GetLogResponse> response = stub.getChatLog(Single.just(request));

        response.blockingSubscribe(
                res -> {
                    List<ChatMessage> messages = res.getMessagesList();
                    if (messages.isEmpty()) {
                        System.out.println("No message history for this topic" +
                                (filterUsername != null ? " from user " + filterUsername : "") + ".");
                    } else {
                        System.out.println("\nRecent message history" +
                                (filterUsername != null ? " from " + filterUsername : "") + ":");
                        for (ChatMessage message : messages) {
                            System.out.println("[" + message.getUsername() + "]: " + message.getContent());
                        }
                    }
                    System.out.println();
                },
                error -> {
                    System.err.println("Error getting chat history: " + error.getMessage());
                    System.out.println("Connection lost. Trying to reconnect to another server...");
                    reconnectToAnyServer();
                }
        );
    }

    /**
     * Shutdown client connections
     */
    public void shutdown() throws InterruptedException {
        // Disconnect from current SC server
        disconnectFromServer();
    }
}
