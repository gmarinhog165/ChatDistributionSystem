package sa.clients;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import sa.sc.SCInfo;
import sa.overlay.CyclonPeer;
import sa.config.Config;
import sa.gossip.Aggregator;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ClientRequestHandler implements Runnable {
    Integer myPort;
    CyclonPeer peer;
    private final Aggregator aggregator;
    
    private final ExecutorService handlerPool = Executors.newCachedThreadPool();
    private final Map<String, ZContext> contextMap = new ConcurrentHashMap<>();
    private ZContext context = new ZContext();
    // ZeroMQ command constants
    private static final String CMD_TOPIC_CONFIG = "TOPIC_CONFIG";
    
    public ClientRequestHandler(Integer myPort, CyclonPeer cyclonPeer, Aggregator aggregator) {
        this.myPort = myPort;
        this.peer = cyclonPeer;
        this.aggregator = aggregator;
    }
    
    @Override
    public void run() {
        try {
            try (ServerSocket serverSocket = new ServerSocket(this.myPort)) {
                System.out.println("SA is listening for client requests on port " + (this.myPort));
                
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handlerPool.submit(() -> handleClient(clientSocket));
                }
            }
        } catch (IOException e) {
            System.err.println("Error in ClientRequestHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            String request = in.readLine();
            if (request == null || !request.startsWith("CREATE_TOPIC")) {
                out.write("ERROR: Invalid request\n");
                out.flush();
                return;
            }
            
            String[] parts = request.split(" ", 3);
            if (parts.length != 3) {
                out.write("ERROR: Missing topic name or username\n");
                out.flush();
                return;
            }
            
            String topic = parts[1].trim();
            String username = parts[2].trim();
            
            System.out.println("Received request to create topic '" + topic + "' from user '" + username + "'");
            
            // Start the aggregation process asynchronously
            Future<CopyOnWriteArrayList<SCInfo>> aggregationFuture = aggregator.startAggregation(topic, username);
            
            // Immediately respond to the client that the process has started
            out.write("Started topic '" + topic + "' creation by '" + username + "'\n");
            out.flush();
            
            handlerPool.submit(() -> {
                try {
                    CopyOnWriteArrayList<SCInfo> scInfoList = aggregationFuture.get(10, TimeUnit.SECONDS);
                    scInfoList.sort(Comparator
                        .comparingInt(SCInfo::getNumClients)
                        .thenComparingInt(SCInfo::getNumTopics));
                    
                    System.out.println("Creating Topics on:");
                    System.out.println("Sorted SCs: " + scInfoList);
                    
                    // Get the SC servers with least load (we'll pick the top 2 for redundancy)
                    List<SCInfo> selectedSCs = selectSCsForTopic(scInfoList, 2);
                    if (selectedSCs.isEmpty()) {
                        System.err.println("No SC servers available to create topic");
                        return;
                    }
                    
                    // Build comma-separated list of SC server addresses
                    StringBuilder serverListBuilder = new StringBuilder();
                    for (int i = 0; i < selectedSCs.size(); i++) {
                        SCInfo sc = selectedSCs.get(i);
                        serverListBuilder.append("localhost").append(":").append(sc.getPort());
                        if (i < selectedSCs.size() - 1) {
                            serverListBuilder.append(",");
                        }
                    }
                    String serverList = serverListBuilder.toString();
                    
                    // Configure the topic on each selected SC
                    for (SCInfo sc : selectedSCs) {
                        configureTopicOnSC(sc, topic, serverList);
                    }
                    
                    System.out.println("Topic '" + topic + "' created successfully on SC servers: " + serverList);
                    
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    System.err.println("Error getting aggregation results: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Select a specified number of SCs for topic creation based on load balancing
     */
    private List<SCInfo> selectSCsForTopic(CopyOnWriteArrayList<SCInfo> scInfoList, int count) {
        List<SCInfo> selected = new ArrayList<>();
        int selectCount = Math.min(count, scInfoList.size());
        
        for (int i = 0; i < selectCount; i++) {
            selected.add(scInfoList.get(i));
        }
        
        return selected;
    }
    
    /**
     * Configure the topic on a specific SC server using ZeroMQ
     */
    /**
     * Configure the topic on a specific SC server using ZeroMQ
     * Based on the SC's SAConnectionManager implementation
     */
    private void configureTopicOnSC(SCInfo sc, String topic, String serverList) {
    int scPort = sc.getPort();
    // Match the port calculation with SAConnectionManager (port - 1)
    int zmqPort = scPort - 4;
    
    System.out.println("SCPORT= " + scPort);
    System.out.println("Connecting to SC's SUB socket on port " + zmqPort);
    
   
        // Create PUSH socket to send message to the SC's PULL socket
        // Switch from PUB to PUSH since SAConnectionManager is using SUB
        ZMQ.Socket pushSocket = context.createSocket(SocketType.PUSH);
        
        String connectionUrl = "tcp://localhost:" + zmqPort;
        System.out.println("Connecting to: " + connectionUrl);
        pushSocket.connect(connectionUrl);
        
        // Instead of using sendMore (PUB/SUB pattern), send a single formatted message
        // Format should match what SAConnectionManager expects when it splits the message
        String message = CMD_TOPIC_CONFIG + "|" + topic + "|" + serverList;
        System.out.println("Sending message: " + message);
        
        // Send and wait a moment to ensure delivery
        boolean sent = pushSocket.send(message);
        
        System.out.println("Topic configuration sent: " + sent);
        
        // Add a small delay to ensure message delivery
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
    }
}
    
    
    /**
     * Clean up all ZMQ contexts when shutting down
     */
    public void shutdown() {
        // Clean shutdown of all ZeroMQ contexts
        for (ZContext context : contextMap.values()) {
            try {
                context.close();
            } catch (Exception e) {
                System.err.println("Error closing ZMQ context: " + e.getMessage());
            }
        }
        contextMap.clear();
        
        // Shutdown thread pool
        handlerPool.shutdown();
        try {
            if (!handlerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                handlerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            handlerPool.shutdownNow();
        }
    }
}