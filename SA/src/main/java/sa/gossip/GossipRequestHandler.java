package sa.gossip;

import sa.config.Config;
import sa.overlay.CyclonPeer;
import sa.sc.SCInfo;

import java.io.*;
import java.net.*;
import java.nio.channels.Pipe.SourceChannel;
import java.util.*;
import java.util.concurrent.*;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GossipRequestHandler implements Runnable {
    private final Integer myPort;
    private final CyclonPeer cyclonPeer;
    private final Map<String, StoredMessage> pendingRequests = new ConcurrentHashMap<>();
    private final Set<Integer> eagerPeers = ConcurrentHashMap.newKeySet(); // Eager push set
    private final Set<Integer> lazyPeers = ConcurrentHashMap.newKeySet(); // Lazy push set
    // Store SC information collected from the network
    private final ConcurrentHashMap<String, SCInfo> networkSCInfo = new ConcurrentHashMap<>();

    public GossipRequestHandler(Integer myPort, CyclonPeer cyclonPeer, Aggregator aggregator) {
        this.myPort = myPort;
        this.cyclonPeer = cyclonPeer;
        initializePeerSets();

    }

    private void initializePeerSets() {
        // Initially, all neighbors are in eager push mode
        Map<Integer, Integer> neighbors = cyclonPeer.getNeighbours();
        for (Integer peer : neighbors.keySet()) {
            eagerPeers.add(peer);
        }
    }
    @Override
    public void run() {
        try {
            try (ServerSocket serverSocket = new ServerSocket(myPort)) {
                System.out.println("SA Plumtree Gossip Handler listening on port " + myPort);
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
            //System.out.println("Handling incoming message from peer: " + socket.getInetAddress() + ":" + socket.getPort());

            String line = in.readLine();
            //System.out.println("Line: " + line);
            if (line == null) {
                socket.close();
                return;
            }

            // Parse the message type - now supporting EAGER_PUSH and LAZY_PUSH
            String[] parts = line.split(" ");
            String messageType = parts[0];
            
            if (messageType.equals("EAGER_PUSH")) {
                handleEagerPush(parts, senderPort, in, out);
            } else if (messageType.equals("LAZY_PUSH")) {
                handleLazyPush(parts, senderPort, in, out);
            } else if (messageType.equals("GRAFT")) {
                handleGraft(parts, senderPort, in, out);
            } else if (messageType.equals("PRUNE")){
                handlePrune(parts,senderPort,in,out);
            } else if (messageType.equals("IWANT")){
                handleIWant(parts,senderPort,in,out);
            }
            else {
                out.write("ERROR: Unknown message type\n");
                out.write("END\n");
                out.flush();
            }
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
   private void handleEagerPush(String[] parts, int senderPort, BufferedReader in, BufferedWriter out) throws IOException {
    if (!isValidFormat(parts, out)) return;

    String topic = parts[1];
    String username = parts[2];
    int ttl = Integer.parseInt(parts[3]);
    String uuid = parts[4];
    String requestId = uuid;
    System.out.println("Received EAGER_PUSH: " + requestId + " from " + senderPort + " with ttl " + ttl);

    
    
    StoredMessage mes = pendingRequests.get(requestId);
    //se ja tenho a mensagem entao mando um prune
    
    if (mes!=null){
        sendPrune(requestId, out, senderPort);
        return;
    }
    pendingRequests.put(requestId, new StoredMessage(topic, username, uuid, ttl));

    int[] scStats = querySCStatus();
    out.write("NUM_CLIENTS " + scStats[0] + " NUM_TOPICOS " + scStats[1] + "\n");
    out.flush();

    List<SCInfo> collectedInfo = new ArrayList<>();

    if (ttl < Config.GOSSIP_TTL) {
        collectedInfo = forwardToEagerPeers(topic, username, ttl, uuid, senderPort, requestId);
        notifyLazyPeers(uuid, senderPort);
    } else {
        System.out.println("Max propagation ttl reached, switching to lazy push");
        notifyAllPeersLazy(uuid, senderPort);
    }

    sendCollectedSCInfo(out, collectedInfo);
}

private boolean isValidFormat(String[] parts, BufferedWriter out) throws IOException {
    if (parts.length < 5) {
        out.write("ERROR: Incomplete EAGER_PUSH format\n");
        out.write("END\n");
        out.flush();
        return false;
    }
    return true;
}

private void sendPrune(String requestId, BufferedWriter out, int senderPort) throws IOException {
    System.out.println("Duplicate message detected, sending PRUNE to " + senderPort);
    out.write("PRUNE " + "\n");
    out.write("END\n");
    out.flush();
}

private void handlePrune(String parts[],int senderPort,BufferedReader in, BufferedWriter out){
    System.out.println("Received prune from node: " + senderPort);
    moveToLazySet(senderPort);
}

//coleta a info dos eagerpeers
private List<SCInfo> forwardToEagerPeers(String topic, String username, int ttl, String uuid, int senderPort, String requestId) {
    System.out.println("Forwarding to neighbors with ttl=" + (ttl + 1) + " (ID: " + requestId + ")");
    List<Future<List<SCInfo>>> forwardingTasks = new ArrayList<>();
    List<SCInfo> collectedInfo = new ArrayList<>();

    for (Integer peer : this.eagerPeers) {
        if (peer.equals(senderPort + 2)) {
            System.out.println("Skipping forwarding back to sender: " + peer);
            continue;
        }

        forwardingTasks.add(CompletableFuture.supplyAsync(() -> contactEagerPeer(peer, topic, username, ttl, uuid)));
    }

    for (Future<List<SCInfo>> task : forwardingTasks) {
        try {
            collectedInfo.addAll(task.get(3, TimeUnit.SECONDS));
        } catch (Exception e) {
            System.err.println("Error or timeout while forwarding: " + e.getMessage());
        }
    }

    return collectedInfo;
}

private List<SCInfo> contactEagerPeer(int peer, String topic, String username, int ttl, String uuid) {
    List<SCInfo> peerInfo = new ArrayList<>();
    try (Socket peerSocket = new Socket("localhost", peer - 2)) {
        BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
        BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));

        peerOut.write("EAGER_PUSH " + topic + " " + username + " " + (ttl + 1) + " " + uuid + "\n");
        peerOut.flush();

        String peerLine;
        while ((peerLine = peerIn.readLine()) != null && !peerLine.equals("END")) {
            System.out.println("Response from " + peer + ": " + peerLine);

            if (peerLine.startsWith("PRUNE")) {
                System.out.println("Received PRUNE from " + peer + ", moving to lazy set");
                this.moveToLazySet(peer);
            } else if (peerLine.startsWith("NUM_CLIENTS")) {
                peerInfo.add(parseSCStatsLine(peerLine, peer));
            } else if (peerLine.startsWith("SC_INFO")) {
                peerInfo.add(parseSCInfoLine(peerLine));
            }
        }
    } catch (IOException e) {
        System.err.println("Failed to forward EAGER_PUSH to peer " + peer + ": " + e.getMessage());
    }
    return peerInfo;
}

private SCInfo parseSCStatsLine(String line, int peerPort) {
    String[] infoParts = line.split(" ");
    int peerNumClients = Integer.MAX_VALUE;
    int peerNumTopics = Integer.MAX_VALUE;

    for (int i = 0; i < infoParts.length - 1; i++) {
        if (infoParts[i].equals("NUM_CLIENTS")) {
            peerNumClients = Integer.parseInt(infoParts[i + 1]);
        } else if (infoParts[i].equals("NUM_TOPICOS")) {
            peerNumTopics = Integer.parseInt(infoParts[i + 1]);
        }
    }

    return new SCInfo(peerPort, peerNumClients, peerNumTopics);
}

private SCInfo parseSCInfoLine(String line) {
    String[] parts = line.split(" ");
    if (parts.length >= 4) {
        int scPort = Integer.parseInt(parts[1]);
        int scClients = Integer.parseInt(parts[2]);
        int scTopics = Integer.parseInt(parts[3]);
        return new SCInfo(scPort, scClients, scTopics);
    }
    return null;
}

private void notifyLazyPeers(String uuid, int senderPort) {
    for (Integer peer : this.lazyPeers) {
        if (peer.equals(senderPort + 2)) continue;

        CompletableFuture.runAsync(() -> {
            try (Socket peerSocket = new Socket("localhost", peer - 2)) {
                BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
                peerOut.write("LAZY_PUSH " + uuid + "\n");
                peerOut.flush();
            } catch (IOException e) {
                System.err.println("Failed lazy push to peer " + peer + ": " + e.getMessage());
            }
        });
    }
}

    private void notifyAllPeersLazy(String uuid, int senderPort) {
        System.out.println("Notifing all peers on lazy mode.");
        for (Integer peer : this.eagerPeers) {
            if (peer.equals(senderPort + 2)) continue;
        
            CompletableFuture.runAsync(() -> {
                try (Socket peerSocket = new Socket("localhost", peer - 2)) {
                    BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
                    peerOut.write("LAZY_PUSH " + uuid + "\n");
                    peerOut.flush();
                } catch (IOException e) {
                    System.err.println("Failed lazy push at max ttl to peer " + peer + ": " + e.getMessage());
                }
            });
        }
    }

    private void sendCollectedSCInfo(BufferedWriter out, List<SCInfo> collectedInfo) throws IOException {
        for (SCInfo info : collectedInfo) {
            if (info != null && info.getPort() != this.myPort) {
                out.write("SC_INFO " + info.getPort() + " " + info.getNumClients() + " " + info.getNumTopics() + "\n");
            }
        }
        out.write("END\n");
        out.flush();
    }

    
    private void handleLazyPush(String[] parts, int senderPort, BufferedReader in, BufferedWriter out) throws IOException {
        if (parts.length < 4) {
            out.write("ERROR: Incomplete LAZY_PUSH format\n");
            out.write("END\n");
            out.flush();
            return;
        }
        
        String uuid = parts[1];
        String requestId = uuid;
        
        System.out.println("Received LAZY_PUSH: " + requestId + " from " + senderPort);
        
        // Check if we've seen this message before
        StoredMessage mes = pendingRequests.get(requestId);
        if (mes==null) {
            // We haven't seen this message yet - send IWANT to get the full message
            System.out.println("Unknown message ID, sending GRAFT for " + requestId);
            out.write("IWANT " + uuid + "\n");
            out.write("GRAFT " + uuid + "\n");
        } else {
            System.out.println("Already have message " + requestId + ", no GRAFT needed");
            propagateLazyPushToNeighbors(uuid, requestId);
        }
        
        out.write("END\n");
        out.flush();
    }

    private void propagateLazyPushToNeighbors(String uuid, String requestId) {
    System.out.println("Propagating message " + requestId + " to both eager and lazy peers");
    
    StoredMessage message = pendingRequests.get(requestId);
    if (message == null) {
        System.err.println("Cannot propagate message " + requestId + ": not found in pendingRequests");
        return;
    }
    
    // For eager peers: send the full message via EAGER_PUSH
    for (Integer peer : this.eagerPeers) {
        CompletableFuture.runAsync(() -> {
            try (Socket peerSocket = new Socket("localhost", peer - 2)) {
                BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
                // Send full message to eager peers
                peerOut.write("EAGER_PUSH " + message.getTopic() + " " + message.getUsername() + " " + 
                             message.getTTL() + " " + uuid + "\n");
                peerOut.flush();
                System.out.println("Forwarded EAGER_PUSH for " + requestId + " to eager peer " + peer);
                
                // Read response
                BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
                String peerLine;
                while ((peerLine = peerIn.readLine()) != null && !peerLine.equals("END")) {
                    if (peerLine.startsWith("PRUNE")) {
                        System.out.println("Received PRUNE from " + peer + ", moving to lazy set");
                        moveToLazySet(peer);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to forward EAGER_PUSH to peer " + peer + ": " + e.getMessage());
            }
        });
    }
    
    // For lazy peers: just send the message ID via LAZY_PUSH
    for (Integer peer : this.lazyPeers) {
        CompletableFuture.runAsync(() -> {
            try (Socket peerSocket = new Socket("localhost", peer - 2)) {
                BufferedWriter peerOut = new BufferedWriter(new OutputStreamWriter(peerSocket.getOutputStream()));
                peerOut.write("LAZY_PUSH " + uuid + "\n");
                peerOut.flush();
                System.out.println("Forwarded LAZY_PUSH for " + requestId + " to lazy peer " + peer);
            } catch (IOException e) {
                System.err.println("Failed to forward LAZY_PUSH to peer " + peer + ": " + e.getMessage());
            }
        });
    }
}
    
    private void handleIWant(String[] parts, int senderPort, BufferedReader in, BufferedWriter out){
        if (parts.length >= 2) {
            System.out.println("Handling IWANT message.");
            String wantedid = parts[1];
            StoredMessage mes = pendingRequests.get(wantedid);
            contactEagerPeer(senderPort, mes.getTopic(),mes.getUsername(),mes.getTTL(),wantedid);
        }
        else{
            System.out.println("Invalid format for IWANT");
        }

    }
    private void handleGraft(String[] parts, int senderPort, BufferedReader in, BufferedWriter out) throws IOException {
        if (parts.length < 2) {
            out.write("ERROR: Incomplete GRAFT format\n");
            out.write("END\n");
            out.flush();
            return;
        }
        
        String uuid = parts[1];
        String requestId = uuid;
        
        System.out.println("Received GRAFT request for " + requestId + " from " + senderPort);
        
        // Move the sender to our eager set
        moveToEagerSet(senderPort + 2);    
    }
    
    private int[] querySCStatus() {
        int numClients = -1;
        int numTopics = -1;
        
        try {
            // Create ZMQ context and socket
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket requester = context.socket(ZMQ.REQ);
            
            // Connect to the SAConnectionManager's REP port
            requester.connect("tcp://localhost:" + (myPort - 1 + 200));
            
            System.out.println("Sending STATUS_REQUEST to tcp://localhost:" + (myPort - 1 + 200));
            requester.send("STATUS_REQUEST");
            
            // Set timeout and receive response
            String response = requester.recvStr(2000);
            System.out.println("Received status " + response);
            
            if (response != null && response.contains(":")) {
                String[] parts = response.split(":");
                numClients = Integer.parseInt(parts[0]);
                numTopics = Integer.parseInt(parts[1]);
            } else {
                System.err.println("Invalid or no response from SAConnectionManager");
            }
            
            requester.close();
            context.term();
        } catch (Exception e) {
            System.err.println("Error querying SC status: " + e.getMessage());
        }
        
        return new int[]{numClients, numTopics};
    }
    
    public synchronized void moveToLazySet(Integer peer) {
        eagerPeers.remove(peer);
        lazyPeers.add(peer);
    }
    public synchronized void moveToEagerSet(Integer peer) {
        lazyPeers.remove(peer);
        eagerPeers.add(peer);
    }
}