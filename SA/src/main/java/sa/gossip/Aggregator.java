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
    
    // Messages seen to prevent duplicates
    private final Set<String> seenRequests = ConcurrentHashMap.newKeySet();
    
    // Plumtree-specific data structures
    private final Set<Integer> eagerPeers = ConcurrentHashMap.newKeySet(); // Eager push set
    private final Set<Integer> lazyPeers = ConcurrentHashMap.newKeySet(); // Lazy push set
    private final Map<String, Set<Integer>> messageOrigins = new ConcurrentHashMap<>(); // Track where we first saw a message
    
    public Aggregator(CyclonPeer cyclonPeer) {
        this.cyclonPeer = cyclonPeer;
        this.executorService = Executors.newCachedThreadPool();
        initializePeerSets();
    }
    
    private void initializePeerSets() {
        // Initially, all neighbors are in eager push mode
        Map<Integer, Integer> neighbors = cyclonPeer.getNeighbours();
        for (Integer peer : neighbors.keySet()) {
            eagerPeers.add(peer);
        }
    }
    
    public Future<CopyOnWriteArrayList<SCInfo>> startAggregation(String topic, String username) {
        // Generate a UUID for this request
        String uuid = UUID.randomUUID().toString();
        
        // Register this request as "seen" by us to prevent loops
        String requestId = topic + ":" + username + ":" + uuid;
        seenRequests.add(requestId);
        
        System.out.println("Initiating Plumtree aggregation with ID: " + requestId);
        
        return executorService.submit(() -> collectSCInfoFromNetwork(topic, username, 0, uuid));
    }
    
    private CopyOnWriteArrayList<SCInfo> collectSCInfoFromNetwork(String topic, String username, int ttl, String uuid) {
        System.out.println("Starting Plumtree aggregation for topic '" + topic + "' with TTL=" + ttl);
        
        CountDownLatch eagerLatch = new CountDownLatch(eagerPeers.size());
        CountDownLatch lazyLatch = new CountDownLatch(lazyPeers.size());
        CopyOnWriteArrayList<SCInfo> candidates = new CopyOnWriteArrayList<>();
        
        String requestId = topic + ":" + username + ":" + uuid;
        
        // Send EAGER push to eager peers
        for (Integer peer : eagerPeers) {
            executorService.submit(() -> {
                try {
                    List<SCInfo> peerResults = contactPeerWithEagerPush(peer, topic, username, ttl, uuid);
                    if (peerResults != null && !peerResults.isEmpty()) {
                        candidates.addAll(peerResults);
                    }
                } catch (Exception e) {
                    System.err.println("Error contacting eager peer " + peer + ": " + e.getMessage());
                } finally {
                    eagerLatch.countDown();
                }
            });
        }
        
        // Send LAZY push (notification only) to lazy peers
        for (Integer peer : lazyPeers) {
            executorService.submit(() -> {
                try {
                    sendLazyPushNotification(peer, topic, username, uuid);
                } catch (Exception e) {
                    System.err.println("Error sending lazy notification to peer " + peer + ": " + e.getMessage());
                } finally {
                    lazyLatch.countDown();
                }
            });
        }
        
        try {
            boolean eagerCompleted = eagerLatch.await(5, TimeUnit.SECONDS);
            boolean lazyCompleted = lazyLatch.await(2, TimeUnit.SECONDS);
            
            if (!eagerCompleted || !lazyCompleted) {
                System.out.println("Warning: Not all peers responded within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Remove duplicates based on port address
        Map<Integer, SCInfo> uniqueSCInfos = new HashMap<>();
        for (SCInfo info : candidates) {
            uniqueSCInfos.put(info.getPort(), info);
        }
        
        CopyOnWriteArrayList<SCInfo> uniqueResults = new CopyOnWriteArrayList<>(uniqueSCInfos.values());
        System.out.println("Plumtree aggregation complete. Collected " + uniqueResults.size() + " unique SC nodes.");
        return uniqueResults;
    }
    
    private List<SCInfo> contactPeerWithEagerPush(Integer peerPort, String topic, String username, int ttl, String uuid) {
        List<SCInfo> results = new ArrayList<>();
        String requestId = topic + ":" + username + ":" + uuid;
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", peerPort-2), 2000);
            socket.setSoTimeout(3000); // Read timeout
            
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send EAGER push message with TTL
            String request = "EAGER_PUSH " + topic + " " + username + " " + ttl + " " + uuid + "\n";
            System.out.println("Sending EAGER_PUSH to " + peerPort + ": " + request.trim());
            
            out.write(request);
            out.flush();
            
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                System.out.println("Response from " + peerPort + ": " + line);
                
                if (line.startsWith("PRUNE")) {
                    // Handle PRUNE message - move peer from eager to lazy set
                    System.out.println("Received PRUNE from " + peerPort + ", moving to lazy set");
                    moveToLazySet(peerPort);
                    continue;
                }
                
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
                        results.add(new SCInfo(peerPort, numClients, numTopics));
                    }
                }
                // Handle SC_INFO from deeper in the network
                else if (line.startsWith("SC_INFO")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 4) {
                        int scPort = Integer.parseInt(parts[1]);
                        int scClients = Integer.parseInt(parts[2]);
                        int scTopics = Integer.parseInt(parts[3]);
                        
                        results.add(new SCInfo(scPort, scClients, scTopics));
                    }
                }
            }
            
            // Track this peer as one that sent us this message
            Set<Integer> origins = messageOrigins.computeIfAbsent(requestId, k -> ConcurrentHashMap.newKeySet());
            origins.add(peerPort);
            
            System.out.println("Collected " + results.size() + " SC infos from peer " + peerPort);
            return results;
            
        } catch (IOException e) {
            System.err.println("Failed to contact peer " + peerPort + ": " + e.getMessage());
            return results;
        }
    }
    
    private void sendLazyPushNotification(Integer peerPort, String topic, String username, String uuid) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", peerPort-2), 2000);
            socket.setSoTimeout(2000); // Short timeout for lazy push
            
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send LAZY push notification (metadata only)
            String request = "LAZY_PUSH " + topic + " " + username + " " + uuid + "\n";
            System.out.println("Sending LAZY_PUSH to " + peerPort + ": " + request.trim());
            
            out.write(request);
            out.flush();
            
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                System.out.println("Lazy push response from " + peerPort + ": " + line);
                
                if (line.startsWith("GRAFT")) {
                    // Handle GRAFT message - peer wants the full message
                    System.out.println("Received GRAFT from " + peerPort + ", moving to eager set");
                    moveToEagerSet(peerPort);
                    
                    // Extract topic, username and uuid from the GRAFT message
                    String[] parts = line.split(" ");
                    if (parts.length >= 4) {
                        String graftTopic = parts[1];
                        String graftUsername = parts[2];
                        String graftUuid = parts[3];
                        
                        // Send the full message to the peer that wants it
                        executorService.submit(() -> 
                            contactPeerWithEagerPush(peerPort, graftTopic, graftUsername, Config.GOSSIP_TTL, graftUuid)
                        );
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Failed to send lazy push to peer " + peerPort + ": " + e.getMessage());
        }
    }
    
    public synchronized void moveToLazySet(Integer peer) {
        eagerPeers.remove(peer);
        lazyPeers.add(peer);
    }
    
    public synchronized void moveToEagerSet(Integer peer) {
        lazyPeers.remove(peer);
        eagerPeers.add(peer);
    }
    
    public boolean hasSeenRequest(String requestId) {
        return seenRequests.contains(requestId);
    }
    
    public void addSeenRequest(String requestId) {
        seenRequests.add(requestId);
    }
    
    public void recordMessageOrigin(String requestId, Integer peerPort) {
        Set<Integer> origins = messageOrigins.computeIfAbsent(requestId, k -> ConcurrentHashMap.newKeySet());
        origins.add(peerPort);
    }
    
    public boolean isFirstTimeFromPeer(String requestId, Integer peerPort) {
        Set<Integer> origins = messageOrigins.get(requestId);
        return origins == null || !origins.contains(peerPort);
    }
    
    public Set<String> getSeenRequests() {
        return seenRequests;
    }
    
    public Set<Integer> getEagerPeers() {
        return eagerPeers;
    }
    
    public Set<Integer> getLazyPeers() {
        return lazyPeers;
    }
}