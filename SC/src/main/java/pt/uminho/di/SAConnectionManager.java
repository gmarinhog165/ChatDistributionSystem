package pt.uminho.di;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class SAConnectionManager {
    private static final Logger logger = Logger.getLogger(SAConnectionManager.class.getName());

    private final String serverId;
    private final int port;

    private final Map<String, Set<String>> topicServers;

    private final ZContext context;
    private final ZMQ.Socket subscribeSocket;

    private final ExecutorService executor;
    private volatile boolean running = true;


    public SAConnectionManager(String serverId, int port, Map<String, Set<String>> topicServers) {
        this.serverId = serverId;
        this.port = port;
        this.topicServers = topicServers;

        this.context = new ZContext();
        this.subscribeSocket = context.createSocket(SocketType.SUB);

        this.subscribeSocket.connect("tcp://localhost:" + (port - 1)); // SA usually runs on port-1

        this.subscribeSocket.subscribe("".getBytes());

        this.executor = Executors.newSingleThreadExecutor();

        logger.info("SA Connection Manager initialized for SC: " + serverId);

        startSAMessageReceiver();
    }

    private void startSAMessageReceiver() {
        executor.submit(() -> {
            logger.info("Starting SA message receiver thread");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    String topic = subscribeSocket.recvStr();
                    if (topic == null) continue;

                    String serversStr = subscribeSocket.recvStr();
                    if (serversStr == null) continue;

                    Set<String> servers = Set.of(serversStr.split(","));

                    logger.info("Received topic configuration from SA: " + topic + " with servers: " + servers);

                    configureTopic(topic, servers);

                } catch (Exception e) {
                    logger.warning("Error receiving message from SA: " + e.getMessage());
                }
            }

            logger.info("SA message receiver thread stopped");
        });
    }

    public void configureTopic(String topic, Set<String> servers) {
        // Store the server list in the shared map
        topicServers.put(topic, servers);
        logger.info("Configured topic " + topic + " with servers: " + servers);
    }


    public void shutdown() {
        running = false;

        try {
            executor.shutdown();

            if (subscribeSocket != null) {
                subscribeSocket.close();
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