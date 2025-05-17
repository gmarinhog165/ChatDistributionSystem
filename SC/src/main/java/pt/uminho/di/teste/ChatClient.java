package pt.uminho.di.teste;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import pt.uminho.di.proto.*;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interactive command-line client for the ChatService
 */
public class ChatClient {
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());

    private final ManagedChannel channel;
    private final Rx3ChatServiceGrpc.RxChatServiceStub chatServiceStub;
    private final String username;
    private String currentTopic = null;
    private Disposable messageSubscription = null;

    /**
     * Constructor for ChatClient
     */
    public ChatClient(String host, int port, String username) {
        this.username = username;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()  // For development only - no TLS
                .build();

        this.chatServiceStub = Rx3ChatServiceGrpc.newRxStub(channel);
    }

    public void joinTopic(String topic) {
        // Create the join request
        JoinLeaveRequest request = JoinLeaveRequest.newBuilder()
                .setTopic(topic)
                .setUsername(username)
                .build();

        // Join the topic
        Single<JoinLeaveResponse> response = chatServiceStub.joinTopic(Single.just(request));
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
                error -> System.err.println("Error joining topic: " + error.getMessage())
        );
    }


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

        Single<JoinLeaveResponse> response = chatServiceStub.leaveTopic(Single.just(request));
        response.blockingSubscribe(
                res -> {
                    if (res.getSuccess()) {
                        System.out.println("Successfully left topic: " + currentTopic);
                        currentTopic = null;
                    } else {
                        System.out.println("Failed to leave topic: " + res.getMessage());
                    }
                },
                error -> System.err.println("Error leaving topic: " + error.getMessage())
        );
    }

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
        Single<SendMessageResponse> response = chatServiceStub.sendMessage(Single.just(request));

        response.blockingSubscribe(
                res -> {
                    if (!res.getSuccess()) {
                        System.out.println("Failed to send message: " + res.getMessage());
                    }
                },
                error -> System.err.println("Error sending message: " + error.getMessage())
        );
    }


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


        Flowable<ChatMessage> messageFlowable = chatServiceStub.subscribeToTopic(Single.just(request));

        messageSubscription = messageFlowable.subscribe(
                message -> {
                    if (!message.getUsername().equals(username)) {
                        System.out.println("\n[" + message.getUsername() + "]: " + message.getContent());
                        System.out.print("> ");
                    }
                },
                error -> {
                    System.err.println("\nError in message subscription: " + error.getMessage());
                    System.out.print("> ");
                }
        );
    }

    private void displayActiveUsers(String topic) {
        GetUsersRequest request = GetUsersRequest.newBuilder()
                .setTopic(topic)
                .setUsername(username)
                .build();

        Single<GetUsersResponse> response = chatServiceStub.getActiveUsers(Single.just(request));

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
                error -> System.err.println("Error getting active users: " + error.getMessage())
        );
    }

    private void displayChatHistory(String topic, int limit, String filterUsername) {
        // Cria o builder
        GetLogRequest.Builder requestBuilder = GetLogRequest.newBuilder()
                .setTopic(topic)
                .setHasLimit(true)
                .setLimit(limit);

        // Adiciona filtro de usuário se não for nulo
        if (filterUsername != null) {
            requestBuilder.setHasUsername(true)
                    .setUsername(filterUsername);
        }

        GetLogRequest request = requestBuilder.build();

        Single<GetLogResponse> response = chatServiceStub.getChatLog(Single.just(request));

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
                error -> System.err.println("Error getting chat history: " + error.getMessage())
        );
    }

    public void shutdown() throws InterruptedException {
        // Dispose any active subscriptions
        if (messageSubscription != null && !messageSubscription.isDisposed()) {
            messageSubscription.dispose();
        }

        // Leave current topic if any
        if (currentTopic != null) {
            leaveTopic();
        }

        // Shutdown the channel with a timeout
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // Menu do cliente
    public void commandLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Chat Client, " + username + "!");
        System.out.println("Type /help for available commands.");

        while (running) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // Process commands
            if (line.startsWith("/")) {
                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String arg = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "/join":
                        if (arg.isEmpty()) {
                            System.out.println("Usage: /join <topic>");
                        } else {
                            joinTopic(arg);
                        }
                        break;

                    case "/leave":
                        leaveTopic();
                        break;

                    case "/users":
                        if (currentTopic != null) {
                            displayActiveUsers(currentTopic);
                        } else {
                            System.out.println("Join a topic first.");
                        }
                        break;

                    case "/history":
                        if (currentTopic != null) {
                            int limit = 20;
                            String user = null;

                            if (!arg.isEmpty()) {
                                String[] args = arg.split("\\s+", 2);
                                try {
                                    limit = Integer.parseInt(args[0]);
                                    if (args.length > 1) {
                                        user = args[1];
                                    }
                                } catch (NumberFormatException e) {
                                    // Se não for um número, considera como nome de usuário
                                    user = args[0];
                                }
                            }
                            displayChatHistory(currentTopic, limit, user);
                        } else {
                            System.out.println("Join a topic first.");
                        }
                        break;

                    case "/quit":
                    case "/exit":
                        running = false;
                        break;

                    case "/help":
                        System.out.println("Available commands:");
                        System.out.println("/join <topic> - Join a chat topic");
                        System.out.println("/leave - Leave the current topic");
                        System.out.println("/users - Display active users in current topic");
                        System.out.println("/history [limit] [user] - Display message history (default: 20)");
                        System.out.println("/quit or /exit - Exit the client");
                        System.out.println("/help - Display this help message");
                        System.out.println("Just type a message to send to the current topic");
                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Type /help for available commands.");
                        break;
                }
            } else {
                // Send as a message
                if (currentTopic != null) {
                    sendMessage(line);
                } else {
                    System.out.println("Join a topic first with /join <topic>");
                }
            }
        }

        System.out.println("Exiting...");
        scanner.close();
    }


    public static void main(String[] args) {
        String host = "localhost";
        int port = 24997;
        String username = "user" + System.currentTimeMillis() % 1000; // Default username

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-h".equals(args[i]) || "--host".equals(args[i])) {
                if (i + 1 < args.length) {
                    host = args[++i];
                }
            } else if ("-p".equals(args[i]) || "--port".equals(args[i])) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number: " + args[i]);
                        System.exit(1);
                    }
                }
            } else if ("-u".equals(args[i]) || "--username".equals(args[i])) {
                if (i + 1 < args.length) {
                    username = args[++i];
                }
            }
        }

        // Create and run the client
        final ChatClient client = new ChatClient(host, port, username);
        try {
            client.commandLoop();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Client error", e);
        } finally {
            try {
                client.shutdown();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Error shutting down client", e);
            }
        }
    }
}