package sa.overlay;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import sa.config.Config;

public class CyclonPeer {
    private final Integer port;
    private final int viewSize = Config.VIEWSIZE;
    private final Map<Integer, Integer> neighbours = new ConcurrentHashMap<>(); // <peer, age>
    /*
    1 thread-listenForConnections() — runs a server socket in a background thread.
    2 thread-incrementAges() — runs every second to age all neighbors.
    3 thread-cyclonShuffle() — runs every 5 seconds to exchange neighbors with peers.
     */
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    public CyclonPeer(Integer port, String peerFile) throws IOException {
        this.port = port;
        List<Integer> initialPeers = readInitialPeers(peerFile, this.port);

        for (Integer peer : initialPeers) {
            if (!(peer.equals(this.port))) {
                neighbours.put(peer, 0); // Initialize with age 0
            }
        }

        System.out.println("Initialized peer " + this.port + " with neighbors: " + neighbours.keySet());
    }

    public void start() {
        // Start server thread
        executor.submit(this::listenForConnections);

        // Start periodic age increment
        executor.scheduleAtFixedRate(this::incrementAges, 1, 1, TimeUnit.SECONDS);

        // Start periodic shuffle
        executor.scheduleAtFixedRate(this::cyclonShuffle, 10, 10, TimeUnit.SECONDS);
    }

    public Map<Integer, Integer> getNeighbours() {
        return new HashMap<>(neighbours);
    }

    private void listenForConnections() {
        try {
            try (ServerSocket serverSocket = new ServerSocket(this.port)) {
                
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.submit(() -> handleIncoming(socket));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleIncoming(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            @SuppressWarnings("unchecked")
            Map<Integer, Integer> receivedPeers = (Map<Integer, Integer>) in.readObject();

            // Select random subset to send back
            int shuffleLength = Math.min(neighbours.size(), receivedPeers.size());
            Map<Integer, Integer> toSend = selectRandomSubset(shuffleLength);

            // Send back our subset
            out.writeObject(toSend);

            // Merge received peers into our view
            mergePeerList(receivedPeers);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void incrementAges() {
        for (Map.Entry<Integer, Integer> entry : neighbours.entrySet()) {
            neighbours.put(entry.getKey(), entry.getValue() + 1);
        }
    }

    private void cyclonShuffle() {
        if (neighbours.isEmpty()) return;

        // 1. Select oldest peer as target
        Integer oldestPeer = findOldestPeer();
        if (oldestPeer == null) return;

        // 2. Select random subset of our view (excluding the oldest)
        int shuffleLength = Math.min(viewSize / 2, neighbours.size() - 1);
        Map<Integer, Integer> toSend = selectRandomSubset(shuffleLength);

        // 3. Add ourselves with age 0
        Integer selfPort = port;
        toSend.put(selfPort, 0);

        try {
            try (Socket socket = new Socket("localhost", oldestPeer);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // 4. Send our subset to the target
                out.writeObject(toSend);

                // 5. Receive their subset
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> receivedPeers = (Map<Integer, Integer>) in.readObject();

                // 6. Remove oldest peer from our view
                neighbours.remove(oldestPeer);

                // 7. Merge received peers
                mergePeerList(receivedPeers);
            }
        } catch (IOException | ClassNotFoundException e) {
            // Target peer may be down, remove it
            System.out.println("Failed to connect to " + oldestPeer + ", removing it");
            neighbours.remove(oldestPeer);
        }
    }

    private Integer findOldestPeer() {
        Integer oldestPeer = null;
        int maxAge = -1;

        for (Map.Entry<Integer, Integer> entry : neighbours.entrySet()) {
            if (entry.getValue() > maxAge) {
                maxAge = entry.getValue();
                oldestPeer = entry.getKey();
            }
        }

        return oldestPeer;
    }

    private Map<Integer, Integer> selectRandomSubset(int count) {
        Map<Integer, Integer> subset = new HashMap<>();
        List<Integer> peers = new ArrayList<>(neighbours.keySet());
        Collections.shuffle(peers);

        int actualCount = Math.min(count, peers.size());
        for (int i = 0; i < actualCount; i++) {
            Integer peer = peers.get(i);
            subset.put(peer, neighbours.get(peer));
        }

        return subset;
    }

    private void mergePeerList(Map<Integer, Integer> newPeers) {
        // Remove self from received peers
        
        newPeers.remove(this.port);

        // Merge with local view, keeping view size limited
        while (!newPeers.isEmpty() && neighbours.size() < viewSize) {
            // Find a peer to add (preferably young ones)
            Integer peerToAdd = findYoungestPeer(newPeers);
            neighbours.put(peerToAdd, newPeers.get(peerToAdd));
            newPeers.remove(peerToAdd);
        }
    }

    private Integer findYoungestPeer(Map<Integer, Integer> peers) {
        Integer youngestPeer = null;
        int minAge = Integer.MAX_VALUE;

        for (Map.Entry<Integer, Integer> entry : peers.entrySet()) {
            if (!neighbours.containsKey(entry.getKey()) && entry.getValue() < minAge) {
                minAge = entry.getValue();
                youngestPeer = entry.getKey();
            }
        }

        // If all are already in our view, just pick one
        if (youngestPeer == null && !peers.isEmpty()) {
            youngestPeer = peers.keySet().iterator().next();
        }
        return youngestPeer;
    }

    private List<Integer> readInitialPeers(String filePath, Integer port) throws IOException {
        List<Integer> peers = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
    
                // Espera um formato tipo "5001: 5002, 5003"
                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;
    
                Integer nodePort =Integer.parseInt(parts[0].trim());
                String neighborPart = parts[1].trim();
                if (nodePort.equals(port)) {
                    String[] neighbors = neighborPart.split(",");
                    for (String neighbor : neighbors) {
                        Integer trimmed = Integer.parseInt(neighbor.trim());
                        if (!(trimmed.equals(port))) {
                            // Adiciona a porta fixa (ex: Config.CYCLON_PORT)
                            peers.add(trimmed);
                        }
                    }
                    break;
                }
            }
        }
        return peers;
    }
    

}