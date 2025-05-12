package pt.uminho.di;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pt.uminho.di.proto.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ChatServiceImpl extends Rx3ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());

    // Chat state data structures
    private final Map<String, ORSet> topicUsers;
    private final Map<String, List<ChatMessage>> chatMessages;
    private final Map<String, PublishSubject<ChatMessage>> topicPublishers;

    // Server-to-server communication
    private final Map<String, Set<String>> topicServers;
    private final ChatDistributionManager distributionManager;
    private final SAConnectionManager saConnectionManager;

    // Server identification
    private final String serverId;
    private final int port;

    public ChatServiceImpl(String serverId, int port) {
        this.serverId = serverId;
        this.port = port;

        this.topicUsers = new ConcurrentHashMap<>();
        this.chatMessages = new ConcurrentHashMap<>();
        this.topicPublishers = new ConcurrentHashMap<>();
        this.topicServers = new ConcurrentHashMap<>();

        this.distributionManager = new ChatDistributionManager(
                serverId,
                port,
                chatMessages,
                topicPublishers,
                topicServers,
                topicUsers
        );

        this.saConnectionManager = new SAConnectionManager(
                serverId,
                port,
                topicServers
        );

        logger.info("ChatServiceImpl initialized with server ID: " + serverId + " on port: " + port);
    }

    @Override
    public Single<JoinLeaveResponse> joinTopic(Single<JoinLeaveRequest> joinRequest) {
        return joinRequest.map(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();

            if(!chatMessages.containsKey(topic)) {
                return JoinLeaveResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Topic " + topic + " not found")
                        .build();
            }

            // Initialize topic structures if this is the first join
            this.chatMessages.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>()));
            this.topicPublishers.computeIfAbsent(topic, k -> PublishSubject.create());

            // Increment active client count in SA connection manager
            saConnectionManager.incrementClientCount();

            //TODO chamar método da classe responsável pelo CRDT
            topicUsers.get(topic).add(serverId, username);
            System.out.println(topicUsers.get(topic).toString());

            logger.info("User " + username + " joined topic: " + topic);

            return JoinLeaveResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Successfully joined " + topic)
                    .build();
        });
    }

    @Override
    public Single<JoinLeaveResponse> leaveTopic(Single<JoinLeaveRequest> leaveRequest) {
        return leaveRequest.map(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();

            if (this.topicUsers.containsKey(topic)) {

                // Decrement active client count in SA connection manager
                saConnectionManager.decrementClientCount();

                //TODO chamar método da classe responsável pelo CRDT
                topicUsers.get(topic).remove(serverId, username);

                logger.info("User " + username + " left topic: " + topic);

                return JoinLeaveResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Successfully left " + topic)
                        .build();
            }

            return JoinLeaveResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Topic: " + topic + " not found!")
                    .build();
        });
    }

    @Override
    public Single<SendMessageResponse> sendMessage(Single<SendMessageRequest> request) {
        return request.map(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();
            String content = req.getContent();

            if(!this.topicUsers.containsKey(topic) || !this.topicUsers.get(topic).elements().contains(username)) {
                return SendMessageResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Error: topic not found or user not subscribed!")
                        .build();
            }

            ChatMessage message = ChatMessage.newBuilder()
                    .setUsername(username)
                    .setContent(content)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            chatMessages.get(topic).add(message);

            // Propagate message to other servers using distribution manager
            distributionManager.propagateMessage(topic, message);

            // Notify local subscribers through the PublishSubject
            if (topicPublishers.containsKey(topic)) {
                topicPublishers.get(topic).onNext(message);
            }

            logger.info("Message sent by " + username + " to topic: " + topic);

            return SendMessageResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Successfully sent message!")
                    .build();
        });
    }

    @Override
    public Single<GetUsersResponse> getActiveUsers(Single<GetUsersRequest> request) {
        return request.map(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();
            if(!this.topicUsers.containsKey(topic) || !this.topicUsers.get(topic).elements().contains(username)) {
                return GetUsersResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Error: topic not found or user not subscribed!")
                        .addAllUsernames(new ArrayList<>())
                        .build();
            }
            List<String> usernames = new ArrayList<>(topicUsers.get(topic).elements());

            return GetUsersResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Successfully retrieved " + usernames.size() + " users!")
                    .addAllUsernames(usernames)
                    .build();
        });
    }

    @Override
    public Single<GetLogResponse> getChatLog(Single<GetLogRequest> request) {
        return request.map(req -> {
            String topic = req.getTopic();

            if(!this.topicUsers.containsKey(topic)) {
                return GetLogResponse.newBuilder()
                        .addAllMessages(new ArrayList<>())
                        .build();
            }

            List<ChatMessage> messages = new ArrayList<>();

            if(chatMessages.containsKey(topic)) {
                messages = new ArrayList<>(chatMessages.get(topic));

                if (req.getHasLimit() && req.getLimit() > 0) {
                    int limit = Math.min(req.getLimit(), messages.size());
                    if (limit > 0) {
                        messages = messages.subList(messages.size() - limit, messages.size());
                    }
                }

                // Filter by username if requested
                if (req.getHasUsername()) {
                    String filterUsername = req.getUsername();
                    messages = messages.stream()
                            .filter(msg -> msg.getUsername().equals(filterUsername))
                            .collect(Collectors.toList());
                }
            }

            return GetLogResponse.newBuilder()
                    .addAllMessages(messages)
                    .build();
        });
    }

    @Override
    public Flowable<ChatMessage> subscribeToTopic(Single<SubscribeRequest> request) {
        return request.flatMapPublisher(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();

            if (!topicUsers.containsKey(topic) || !topicUsers.get(topic).elements().contains(username)) {
                return Flowable.error(new RuntimeException("User not subscribed to topic or topic doesn't exist"));
            }

            Flowable<ChatMessage> historicalMessages = Flowable.fromIterable(
                    chatMessages.getOrDefault(topic, Collections.emptyList())
            );

            PublishSubject<ChatMessage> publisher = topicPublishers.computeIfAbsent(
                    topic, k -> PublishSubject.create()
            );

            Flowable<ChatMessage> newMessages = publisher.toFlowable(BackpressureStrategy.BUFFER);

            logger.info("User " + username + " subscribed to topic: " + topic);

            return historicalMessages.concatWith(newMessages);
        });
    }

    public void shutdown() {
        try {
            if (distributionManager != null) {
                distributionManager.shutdown();
            }

            if (saConnectionManager != null) {
                saConnectionManager.shutdown();
            }

            logger.info("ChatServiceImpl shut down successfully");
        } catch (Exception e) {
            logger.warning("Error during ChatServiceImpl shutdown: " + e.getMessage());
        }
    }
}