package sa.gossip;

import sa.config.Config;
import sa.overlay.CyclonPeer;
import sa.sc.SCInfo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Aggregator {
    private final CyclonPeer cyclonPeer;
    private final ExecutorService executorService;
    private final Set<String> seenRequests = ConcurrentHashMap.newKeySet();
    
    public Aggregator(CyclonPeer cyclonPeer) {
        this.cyclonPeer = cyclonPeer;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public Future<CopyOnWriteArrayList<SCInfo>> startAggregation(String topic, String username) {
        int initialTTL = Config.GOSSIP_TTL;
        // Generate a UUID for this request
        String uuid = UUID.randomUUID().toString();
        
        // Register this request as "seen" by us to prevent loops
        String requestId = topic + ":" + username + ":" + uuid;
        seenRequests.add(requestId);
        
        System.out.println("Initiating aggregation with ID: " + requestId);
        
        return executorService.submit(() -> collectSCInfoFromNetwork(topic, username, initialTTL, uuid));
    }
    
    private CopyOnWriteArrayList<SCInfo> collectSCInfoFromNetwork(String topic, String username, int ttl, String uuid) {
        System.out.println("Starting aggregation for topic '" + topic + "' with TTL=" + ttl);
        
        //Ip and age
        Map<String, Integer> neighbors = cyclonPeer.getNeighbours();
        CountDownLatch latch = new CountDownLatch(neighbors.size());
        CopyOnWriteArrayList<SCInfo> candidates = new CopyOnWriteArrayList<>();
        
        for (String peer : neighbors.keySet()) {
            executorService.submit(() -> {
                try {
                    List<SCInfo> peerResults = contactPeerForSCInfo(peer, topic, username, ttl, uuid);
                    if (peerResults != null && !peerResults.isEmpty()) {
                        candidates.addAll(peerResults);
                    }
                } catch (Exception e) {
                    System.err.println("Error contacting peer " + peer + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Warning: Not all peers responded within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Remove duplicates based on IP address
        Map<String, SCInfo> uniqueSCInfos = new HashMap<>();
        for (SCInfo info : candidates) {
            uniqueSCInfos.put(info.getAddress(), info);
        }
        
        CopyOnWriteArrayList<SCInfo> uniqueResults = new CopyOnWriteArrayList<>(uniqueSCInfos.values());
        System.out.println("Aggregation complete. Collected " + uniqueResults.size() + " unique SC nodes.");
        return uniqueResults;
    }
    
    private List<SCInfo> contactPeerForSCInfo(String peerAddress, String topic, String username, int ttl, String uuid) {
        List<SCInfo> results = new ArrayList<>();
        
        // Apply a short timeout to avoid hanging
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(peerAddress, Config.GOSSIP_PORT), 2000);
            socket.setSoTimeout(3000); // Read timeout
            
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Include the UUID with the request
            String request = "HOST_TOPIC " + topic + " " + username + " " + ttl + " " + uuid + "\n";
            System.out.println("Sending to " + peerAddress + ": " + request.trim());
            
            out.write(request);
            out.flush();
            
            // Direct peer info
            SCInfo peerInfo = null;
            
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                System.out.println("Response from " + peerAddress + ": " + line);
                
                // Handle direct peer info (same format as before)
                if (line.startsWith("NUM_CLIENTS")) {
                    int numClients = Integer.MAX_VALUE;
                    int numTopics = Integer.MAX_VALUE;
                    
                    String[] parts = line.split(" ");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts[i].equals("NUM_CLIENTS")) {
                            numClients = Integer.parseInt(parts[i + 1]);
                        } else if (parts[i].equals("NUM_TOPICOS")) {
                            numTopics = Integer.parseInt(parts[i + 1]);
                        }
                    }
                    
                    if (numClients != Integer.MAX_VALUE && numTopics != Integer.MAX_VALUE) {
                        peerInfo = new SCInfo(peerAddress, Config.SC_PORT, numClients, numTopics);
                        results.add(peerInfo);
                    }
                }
                // Handle SC_INFO from deeper in the network
                else if (line.startsWith("SC_INFO")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 5) {
                        String scIp = parts[1];
                        int scPort = Integer.parseInt(parts[2]);
                        int scClients = Integer.parseInt(parts[3]);
                        int scTopics = Integer.parseInt(parts[4]);
                        
                        SCInfo remoteInfo = new SCInfo(scIp, scPort, scClients, scTopics);
                        results.add(remoteInfo);
                    }
                }
            }
            
            System.out.println("Collected " + results.size() + " SC infos from peer " + peerAddress);
            return results;
            
        } catch (IOException e) {
            System.err.println("Failed to contact peer " + peerAddress + ": " + e.getMessage());
            return results; // Return any results we may have collected before the error
        }
    }
    
    public Set<String> getSeenRequests() {
        return seenRequests;
    }
}