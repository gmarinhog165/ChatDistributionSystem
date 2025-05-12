package pt.uminho.di;

import io.reactivex.rxjava3.subjects.PublishSubject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pt.uminho.di.proto.ChatMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChatDistributionManager {
    private static final Logger logger = Logger.getLogger(ChatDistributionManager.class.getName());

    private final String serverId;
    private final int port;

    // structs
    private final Map<String, List<ChatMessage>> chatMessages;
    private final Map<String, PublishSubject<ChatMessage>> topicPublishers;
    private final Map<String, Set<String>> topicServers;

    // ZeroMQ
    private final ZContext context;
    private final ZMQ.Socket publishSocket;  // For sending messages to other SCs
    private final ZMQ.Socket subscribeSocket; // For receiving messages from other SCs

    // Vector clock
    private final Map<String, Map<String, Integer>> topicVectorClocks;
    private final Map<String, List<MessageContainer>> messageBuffers;

    // CRDT
    private final Map<String, ORSet> topicUsers;

    private final ScheduledExecutorService scheduler;

    public ChatDistributionManager(
            String serverId,
            int port,
            Map<String, List<ChatMessage>> chatMessages,
            Map<String, PublishSubject<ChatMessage>> topicPublishers,
            Map<String, Set<String>> topicServers,
            Map<String, ORSet> topicUsers) {

        this.serverId = serverId;
        this.port = port;
        this.chatMessages = chatMessages;
        this.topicPublishers = topicPublishers;
        this.topicServers = topicServers;
        this.topicUsers = topicUsers;

        this.topicVectorClocks = new ConcurrentHashMap<>();
        this.messageBuffers = new ConcurrentHashMap<>();

        this.context = new ZContext();
        this.publishSocket = context.createSocket(SocketType.PUB);
        this.publishSocket.bind("tcp://*:" + (port + 100)); // Use offset for SC communication

        this.subscribeSocket = context.createSocket(SocketType.SUB);

        this.scheduler = Executors.newScheduledThreadPool(1);

        startMessageReceiver();

        // Start periodic buffer check to try delivering delayed messages
        scheduler.scheduleAtFixedRate(this::tryDeliverBufferedMessages, 1, 1, TimeUnit.SECONDS);

        // Start the topic monitor to detect changes in the topicServers map
        scheduler.scheduleWithFixedDelay(this::monitorTopicChanges, 5, 5, TimeUnit.SECONDS);

        //
        scheduler.scheduleAtFixedRate(this::propagateORSets, 3, 3, TimeUnit.SECONDS);

        logger.info("Chat Distribution Manager initialized with ID: " + serverId);
    }

    private void monitorTopicChanges() {
        try {
            for (Map.Entry<String, Set<String>> entry : topicServers.entrySet()) {
                String topic = entry.getKey();
                Set<String> servers = entry.getValue();

                // Check if we already have this topic configured
                if (!topicVectorClocks.containsKey(topic)) {
                    // This is a new topic, initialize it
                    configureTopic(topic, servers);
                }
            }
        } catch (Exception e) {
            logger.warning("Error in topic monitor: " + e.getMessage());
        }
    }

    private void propagateORSets() {
        for (Map.Entry<String, ORSet> entry : topicUsers.entrySet()) {
            String topic = entry.getKey();
            ORSet orset = entry.getValue();

            UserSetContainer container = new UserSetContainer(
                    topic,
                    new HashMap<>(orset.getCausalContext()),
                    orset.getDotMap(),
                    serverId
            );

            publishSocket.sendMore(topic + "_userset");
            publishSocket.send(container.toBytes());

            logger.info("Propagated ORSet for topic " + topic + " to peers.");
        }
    }


    private void configureTopic(String topic, Set<String> servers) {
        // Initialize vector clock for this topic
        Map<String, Integer> vectorClock = new ConcurrentHashMap<>();
        for (String server : servers) {
            vectorClock.put(server, 0);
        }
        topicVectorClocks.put(topic, vectorClock);
        chatMessages.put(topic, new ArrayList<>());

        messageBuffers.put(topic, Collections.synchronizedList(new ArrayList<>()));

        ORSet orSet = new ORSet();
        orSet.initCausalContext(servers);
        topicUsers.put(topic, orSet);

        for (String serverAddress : servers) {
            if (!serverAddress.equals(serverId)) {
                String[] parts = serverAddress.split(":");
                String host = parts[0];
                int scPort = Integer.parseInt(parts[1]);

                subscribeSocket.connect("tcp://" + host + ":" + (scPort + 100));
                logger.info("Connected to server: " + serverAddress + " for topic: " + topic);
            }
        }

        subscribeSocket.subscribe(topic);
        subscribeSocket.subscribe((topic + "_userset"));
        logger.info("Configured topic: " + topic + " with servers: " + servers);
    }


    private void startMessageReceiver() {
        Thread receiverThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String topic = subscribeSocket.recvStr();
                    if (topic == null) continue;

                    byte[] messageData = subscribeSocket.recv();
                    if (messageData == null) continue;

                    if (topic.endsWith("_userset")) {
                        UserSetContainer container = UserSetContainer.fromBytes(messageData);
                        processUserSet(container);
                        continue;
                    }

                    MessageContainer container = MessageContainer.fromBytes(messageData);
                    processReceivedMessage(container);

                } catch (Exception e) {
                    logger.warning("Error receiving message: " + e.getMessage());
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
        logger.info("Message receiver thread started");
    }

    public void propagateMessage(String topic, ChatMessage message) {
        Map<String, Integer> vectorClock = topicVectorClocks.get(topic);
        if (vectorClock == null) {
            logger.warning("No vector clock found for topic: " + topic);
            return;
        }

        int currentClock = vectorClock.getOrDefault(serverId, 0);
        vectorClock.put(serverId, currentClock + 1);

        MessageContainer container = new MessageContainer(topic, message, vectorClock, serverId);

        publishSocket.sendMore(topic);
        publishSocket.send(container.toBytes());

        logger.info("Propagated message to topic: " + topic + " with clock: " + vectorClock);
    }

    private void processReceivedMessage(MessageContainer container) {
        String topic = container.getTopic();

        Map<String, Integer> localClock = topicVectorClocks.get(topic);
        if (localClock == null) {
            logger.warning("Received message for unknown topic: " + topic);
            return;
        }

        if (canDeliver(container, localClock)) {
            deliverMessage(container);
            // After delivering, try to deliver any buffered messages that are now ready
            tryDeliverBufferedMessagesForTopic(topic);
        } else {
            // If we can't deliver now, buffer the message for later
            messageBuffers.get(topic).add(container);
            logger.info("Buffered message for topic: " + topic + " from server: " + container.getSenderId());
        }
    }

    private void processUserSet(UserSetContainer container) {
        String topic = container.getTopic();
        if (!topicUsers.containsKey(topic)) {
            logger.warning("Received ORSet update for unknown topic: " + topic);
            return;
        }

        ORSet localSet = topicUsers.get(topic);

        ORSet remote = new ORSet();
        remote.setCausalContext(container.getCausalContext());
        remote.setDotMap(container.getDotMap());

        System.out.println("Local");
        System.out.println(localSet.toString());
        System.out.println("Remote");
        System.out.println(remote.toString());

        localSet.join(remote);
        System.out.println("Depois do merge");
        System.out.println(localSet.toString());
        logger.info("Merged ORSet for topic " + topic + " from server " + container.getSenderId());
    }

    private boolean canDeliver(MessageContainer container, Map<String, Integer> localClock) {
        String senderId = container.getSenderId();
        Map<String, Integer> msgClock = container.getVectorClock();

        if (msgClock.getOrDefault(senderId, 0) != localClock.getOrDefault(senderId, 0) + 1) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : msgClock.entrySet()) {
            String server = entry.getKey();
            int clock = entry.getValue();

            if (!server.equals(senderId) && clock > localClock.getOrDefault(server, 0)) {
                return false;
            }
        }

        return true;
    }

    private void deliverMessage(MessageContainer container) {
        String topic = container.getTopic();
        ChatMessage message = container.getMessage();
        String senderId = container.getSenderId();

        Map<String, Integer> localClock = topicVectorClocks.get(topic);
        localClock.put(senderId, localClock.getOrDefault(senderId, 0) + 1);

        synchronized (chatMessages) {
            if (chatMessages.containsKey(topic)) {
                chatMessages.get(topic).add(message);
            } else {
                List<ChatMessage> messages = Collections.synchronizedList(new ArrayList<>());
                messages.add(message);
                chatMessages.put(topic, messages);
            }
        }

        if (topicPublishers.containsKey(topic)) {
            topicPublishers.get(topic).onNext(message);
            logger.info("Delivered message from server " + senderId + " to topic: " + topic);
        } else {
            logger.warning("No publisher for topic: " + topic);
        }
    }

    private void tryDeliverBufferedMessages() {
        for (String topic : messageBuffers.keySet()) {
            tryDeliverBufferedMessagesForTopic(topic);
        }
    }

    private void tryDeliverBufferedMessagesForTopic(String topic) {
        List<MessageContainer> buffer = messageBuffers.get(topic);
        if (buffer == null) return;

        Map<String, Integer> localClock = topicVectorClocks.get(topic);
        if (localClock == null) return;

        boolean progress;
        do {
            progress = false;
            List<MessageContainer> toDeliver = new ArrayList<>();

            synchronized (buffer) {
                Iterator<MessageContainer> iterator = buffer.iterator();
                while (iterator.hasNext()) {
                    MessageContainer container = iterator.next();
                    if (canDeliver(container, localClock)) {
                        toDeliver.add(container);
                        iterator.remove();
                        progress = true;
                    }
                }
            }

            for (MessageContainer container : toDeliver) {
                deliverMessage(container);
            }
        } while (progress);
    }

    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

            if (publishSocket != null) {
                publishSocket.close();
            }
            if (subscribeSocket != null) {
                subscribeSocket.close();
            }
            if (context != null) {
                context.close();
            }

            logger.info("Chat Distribution Manager shut down");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}