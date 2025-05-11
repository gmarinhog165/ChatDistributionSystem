package sa.gossip;

import sa.config.Config;
import sa.overlay.CyclonPeer;
import sa.sc.SCInfo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GossipRequestHandler implements Runnable {
    private final Integer myPort;
    private final CyclonPeer cyclonPeer;
    private final Aggregator aggregator;
    // Add a map to store SCInfo collected from the network
    private final ConcurrentHashMap<String, SCInfo> networkSCInfo = new ConcurrentHashMap<>();

    public GossipRequestHandler(Integer myPort, CyclonPeer cyclonPeer, Aggregator aggregator) {
        this.myPort = myPort;
        this.cyclonPeer = cyclonPeer;
        this.aggregator = aggregator;
    }

    @Override
    public void run() {
        try {
            try (ServerSocket serverSocket = new ServerSocket(myPort)) {
                System.out.println("SA Gossip Handler listening on port " + myPort);
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleRequest(socket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) {
        try {
            int senderPort = socket.getPort();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    
            String line = in.readLine();
            if (line == null || !line.startsWith("HOST_TOPIC")) {
                out.write("ERROR: Invalid request format\n");
                out.write("END\n");
                out.flush();
                socket.close();
                return;
            }
            
            String[] parts = line.split(" ");
            if (parts.length < 4) {
                out.write("ERROR: Incomplete request format\n");
                out.write("END\n");
                out.flush();
                socket.close();
                return;
            }
    
            String topic = parts[1];
            String username = parts[2];
            int ttl = Integer.parseInt(parts[3]);
            
            // Generate or use existing request UUID
            String uuid;
            if (parts.length > 4) {
                uuid = parts[4];
            } else {
                uuid = UUID.randomUUID().toString();
            }
            
            String requestId = topic + ":" + username + ":" + uuid;
    
            System.out.println("Received HOST_TOPIC request: " + line + " (ID: " + requestId + ") from " + senderPort);
    
            // Generate my own SC info (simulated values) depois aqui vai buscar ao sc
            int numClients = ThreadLocalRandom.current().nextInt(0, 6); // 0 to 5 inclusive
            int numTopics = ThreadLocalRandom.current().nextInt(0, 6);  // 0 to 5 inclusive
            
            // Create SCInfo for myself
            //SCInfo myInfo = new SCInfo(selfIPAddr, Config.SC_PORT, numClients, numTopics);
            
            // Write my SC info in the response
            out.write("NUM_CLIENTS " + numClients + " NUM_TOPICOS " + numTopics + "\n");
            
            // Create a list to collect SC infos from neighbors
            List<SCInfo> collectedInfo = new ArrayList<>();
            //com isto depois enviava info repetida
            //collectedInfo.add(myInfo); // Add my info
            
            // Only forward if:
            // 1. We haven't seen this request before
            // 2. TTL is greater than 0
            boolean isNewRequest = aggregator.getSeenRequests().add(requestId);
            
            if (isNewRequest && ttl > 0) {
                System.out.println("New request, forwarding to neighbors with TTL=" + (ttl - 1) + " (ID: " + requestId + ")");
                Map<Integer, Integer> neighbors = cyclonPeer.getNeighbours();
                
                // Create a collection for tracking forwarding attempts
                List<Future<List<SCInfo>>> forwardingTasks = new ArrayList<>();
                
                for (Integer peer : neighbors.keySet()) {
                    // Skip sending back to the node that sent us this request
                    if (peer.equals(senderPort)) {
                        System.out.println("Skipping forwarding back to sender: " + peer);
                        continue;
                    }
                    
                    // Forward to each neighbor in parallel and collect results
                    forwardingTasks.add(CompletableFuture.supplyAsync(() -> {
                        List<SCInfo> peerInfo = new ArrayList<>();
                        try (Socket peerSocket = new Socket("localhost",peer-2);
                             BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
                             BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()))) {
                            
                            // Forward with decreased TTL but same UUID
                            peerOut.write("HOST_TOPIC " + topic + " " + username + " " + (ttl - 1) + " " + uuid + "\n");
                            peerOut.flush();
                            
                            System.out.println("Forwarded to " + peer + ", reading response...");
    
                            String peerLine;
                            while ((peerLine = peerIn.readLine()) != null && !peerLine.equals("END")) {
                                System.out.println("Response from " + peer + ": " + peerLine);
                                
                                // Parse SC information from the response
                                if (peerLine.startsWith("NUM_CLIENTS")) {
                                    String[] infoParts = peerLine.split(" ");
                                    int peerNumClients = Integer.MAX_VALUE;
                                    int peerNumTopics = Integer.MAX_VALUE;
                                    
                                    for (int i = 0; i < infoParts.length - 1; i++) {
                                        if (infoParts[i].equals("NUM_CLIENTS")) {
                                            peerNumClients = Integer.parseInt(infoParts[i + 1]);
                                        } else if (infoParts[i].equals("NUM_TOPICOS")) {
                                            peerNumTopics = Integer.parseInt(infoParts[i + 1]);
                                        }
                                    }
                                    
                                    if (peerNumClients != Integer.MAX_VALUE && peerNumTopics != Integer.MAX_VALUE) {
                                        SCInfo info = new SCInfo(peer, peerNumClients, peerNumTopics);
                                        peerInfo.add(info);
                                    }
                                }
                                // Also relay SC_INFO from further nodes
                                else if (peerLine.startsWith("SC_INFO")) {
                                    out.write(peerLine + "\n");
                                    out.flush();
                                    
                                    // Parse the SC_INFO line to extract data
                                    String[] scInfoParts = peerLine.split(" ");
                                    if (scInfoParts.length >= 4) {
                                        int scPort = Integer.parseInt(scInfoParts[1]);
                                        int scClients = Integer.parseInt(scInfoParts[2]);
                                        int scTopics = Integer.parseInt(scInfoParts[3]);
                                        
                                        SCInfo remoteInfo = new SCInfo(scPort, scClients, scTopics);
                                        peerInfo.add(remoteInfo);
                                    }
                                }
                            }
                            
                            System.out.println("Completed forwarding to " + peer + ", collected " + peerInfo.size() + " SC infos");
    
                        } catch (IOException e) {
                            System.err.println("Failed to forward message to peer " + peer + ": " + e.getMessage());
                        }
                        return peerInfo;
                    }));
                }
                
                // Wait for all forwarding to complete with a timeout and collect results
                try {
                    for (Future<List<SCInfo>> task : forwardingTasks) {
                        try {
                            List<SCInfo> results = task.get(3, TimeUnit.SECONDS);
                            collectedInfo.addAll(results);
                        } catch (Exception e) {
                            System.err.println("Error or timeout while forwarding: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error managing forwarding tasks: " + e.getMessage());
                }
            } else if (!isNewRequest) {
                System.out.println("Duplicate request detected. Responding but not forwarding: " + requestId);
            } else {
                System.out.println("TTL is 0, not forwarding: " + requestId);
            }
            
            // Forward all collected SC infos back to the requester
            for (SCInfo info : collectedInfo) {
                if (!(info.getPort()==this.myPort)) { // Don't repeat our own info
                    out.write("SC_INFO " +  
                              info.getPort() + " " + 
                              info.getNumClients() + " " + 
                              info.getNumTopics() + "\n");
                }
            }
    
            out.write("END\n");
            out.flush();
            socket.close();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}