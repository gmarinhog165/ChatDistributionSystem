package pt.uminho.di;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pt.uminho.di.proto.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatServiceImpl extends Rx3ChatServiceGrpc.ChatServiceImplBase {
    private Map<String, Set<String>> topicUsers;
    private Map<String, List<ChatMessage>> chatMessages;
    private Map<String, PublishSubject<ChatMessage>> topicPublishers;

    public ChatServiceImpl() {
        this.topicUsers = new ConcurrentHashMap<>();
        this.chatMessages = new ConcurrentHashMap<>();
        this.topicPublishers = new ConcurrentHashMap<>();
    }

    @Override
    public Single<JoinLeaveResponse> joinTopic(Single<JoinLeaveRequest> joinRequest) {
        return joinRequest.map(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();

            // O primeiro a entrar inicializa a cena
            this.topicUsers.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(username);
            this.chatMessages.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>()));
            this.topicPublishers.computeIfAbsent(topic, k -> PublishSubject.create());

            //TODO chamar método da classe responsável pelo CRDT

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
                topicUsers.get(topic).remove(username);

                //TODO chamar método da classe responsável pelo CRDT

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

            if(!this.topicUsers.containsKey(topic) || !this.topicUsers.get(topic).contains(username)) {
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

            //TODO: propagar mensagens para os SCs com Vector Clocks

            // Notifica todos os observadores inscritos neste tópico através do PublishSubject
            if (topicPublishers.containsKey(topic)) {
                topicPublishers.get(topic).onNext(message);
            }

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
            if(!this.topicUsers.containsKey(topic) || !this.topicUsers.get(topic).contains(username)) {
                return GetUsersResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Error: topic not found or user not subscribed!")
                        .addAllUsernames(new ArrayList<>())
                        .build();
            }
            List<String> usernames = new ArrayList<>(topicUsers.get(topic));

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
            // Obtenha o nome de usuário se disponível no protobuf
            String username = req.getHasUsername() ? req.getUsername() : null;

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

                // Se filtrar por usuário
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

    // New method for reactive subscription
    @Override
    public Flowable<ChatMessage> subscribeToTopic(Single<SubscribeRequest> request) {
        return request.flatMapPublisher(req -> {
            String topic = req.getTopic();
            String username = req.getUsername();

            if (!topicUsers.containsKey(topic) || !topicUsers.get(topic).contains(username)) {
                return Flowable.error(new RuntimeException("User not subscribed to topic or topic doesn't exist"));
            }

            // Create a Flowable from historical messages
            Flowable<ChatMessage> historicalMessages = Flowable.fromIterable(
                    chatMessages.getOrDefault(topic, Collections.emptyList())
            );

            // Get the publisher for this topic or create a new one
            PublishSubject<ChatMessage> publisher = topicPublishers.computeIfAbsent(
                    topic, k -> PublishSubject.create()
            );

            // Create a Flowable from the publisher (for new messages)
            Flowable<ChatMessage> newMessages = publisher.toFlowable(BackpressureStrategy.BUFFER);

            // Concatenate historical messages followed by new ones
            return historicalMessages.concatWith(newMessages);
        });
    }
}