package pt.uminho.di;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class SAConnectionManager {
    private static final Logger logger = Logger.getLogger(SAConnectionManager.class.getName());

    private final String serverId;
    private final int port;

    private final Map<String, Set<String>> topicServers;

    // Counter for total active clients
    private final AtomicInteger activeClientCount = new AtomicInteger(0);

    private final ZContext context;
    private final ZMQ.Socket subscribeSocket;
    private final ZMQ.Socket replySocket;

    private final ExecutorService executor;
    private volatile boolean running = true;

    // Command constants for SA requests
    private static final String CMD_TOPIC_CONFIG = "TOPIC_CONFIG";
    private static final String CMD_STATUS_REQUEST = "STATUS_REQUEST";

    public SAConnectionManager(String serverId, int port, Map<String, Set<String>> topicServers) {
        this.serverId = serverId;
        this.port = port;
        this.topicServers = topicServers;

        this.context = new ZContext();

        // Socket for receiving topic configurations
        this.subscribeSocket = context.createSocket(SocketType.PULL);
        System.out.println("zerosocket on port " + (port-1));
        this.subscribeSocket.bind("tcp://localhost:" + (port - 1)); // SA usually runs on port-1

        // Socket for handling request-reply pattern
        this.replySocket = context.createSocket(SocketType.REP);
        System.out.println("Status socket on port " + (port+200));
        this.replySocket.bind("tcp://*:" + (port + 200)); // Using port+200 for REP socket

        this.executor = Executors.newFixedThreadPool(2); // One thread for SUB, one for REP

        logger.info("SA Connection Manager initialized for SC: " + serverId);

        startSAMessageReceiver();
        startRequestHandler();
    }

    private void startSAMessageReceiver() {
    executor.submit(() -> {
        logger.info("Starting SA message receiver thread");
        System.out.println("Waiting for messages on PULL socket...");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Using PULL now, so we get a single message, not multi-frame
                String message = subscribeSocket.recvStr(1000); // 1 second timeout
                
                if (message == null) {
                    continue; // No message received, try again
                }
                
                System.out.println("Received raw message: " + message);
                
                // Parse the message that should be in format "CMD|TOPIC|SERVERS"
                String[] parts = message.split("\\|");
                if (parts.length < 3 || !CMD_TOPIC_CONFIG.equals(parts[0])) {
                    logger.warning("Invalid message format received: " + message);
                    continue;
                }
                
                String topic = parts[1];
                Set<String> servers = Set.of(parts[2].split(","));
                
                logger.info("Received topic configuration from SA: " + topic + " with servers: " + servers);
                System.out.println("Configuring topic: " + topic + " with servers: " + servers);
                
                configureTopic(topic, servers);
                
            } catch (Exception e) {
                logger.warning("Error receiving message from SA: " + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("SA message receiver thread stopped");
    });
}

    private void startRequestHandler() {
        executor.submit(() -> {
            logger.info("Starting SA request handler thread");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    String request = replySocket.recvStr();
                    if (request == null) continue;

                    if (CMD_STATUS_REQUEST.equals(request)) {
                        // Create status response: format "clientCount:topicCount"
                        int clientCount = activeClientCount.get();
                        int topicCount = topicServers.size();

                        String response = clientCount + ":" + topicCount;
                        replySocket.send(response);

                        logger.info("Sent status response to SA: " + response);
                    } else {
                        // Unknown command
                        replySocket.send("ERROR:Unknown command");
                    }
                } catch (Exception e) {
                    logger.warning("Error handling SA request: " + e.getMessage());
                    try {
                        replySocket.send("ERROR:" + e.getMessage());
                    } catch (Exception sendError) {
                        logger.severe("Failed to send error response: " + sendError.getMessage());
                    }
                }
            }

            logger.info("SA request handler thread stopped");
        });
    }

    public void configureTopic(String topic, Set<String> servers) {
        // Store the server list in the shared map
        topicServers.put(topic, servers);
        logger.info("Configured topic " + topic + " with servers: " + servers);
    }

    /**
     * Increment the active client count when a client joins
     */
    public void incrementClientCount() {
        activeClientCount.incrementAndGet();
        logger.fine("Client count incremented: " + activeClientCount.get());
    }

    /**
     * Decrement the active client count when a client leaves
     */
    public void decrementClientCount() {
        activeClientCount.decrementAndGet();
        logger.fine("Client count decremented: " + activeClientCount.get());
    }

    /**
     * Get the current client count
     * @return Current number of active clients
     */
    public int getClientCount() {
        return activeClientCount.get();
    }

    public void shutdown() {
        running = false;

        try {
            executor.shutdown();

            if (subscribeSocket != null) {
                subscribeSocket.close();
            }
            if (replySocket != null) {
                replySocket.close();
            }
            if (context != null) {
                context.close();
            }

            logger.info("SA Connection Manager shut down");
        } catch (Exception e) {
            logger.warning("Error shutting down SA Connection Manager: " + e.getMessage());
        }
    }
}