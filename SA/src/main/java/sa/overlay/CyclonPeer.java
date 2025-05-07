package sa.overlay;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CyclonPeer {
    private final String address;
    private final int port;
    private final int viewSize = 3;
    private final Map<String, Integer> neighbours = new ConcurrentHashMap<>(); // <peer, age>
    /*
    1 thread-listenForConnections() — runs a server socket in a background thread.
    2 thread-incrementAges() — runs every second to age all neighbors.
    3 thread-cyclonShuffle() — runs every 5 seconds to exchange neighbors with peers.
     */
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    private final Random random = new Random();

    public CyclonPeer(String address, String peerFile) throws IOException {
        String[] parts = address.split(":");
        this.address = parts[0];
        this.port = Integer.parseInt(parts[1]);

        String myFullAddress = address;
        List<String> initialPeers = readInitialPeers(peerFile, myFullAddress);

        for (String peer : initialPeers) {
            if (!peer.equals(myFullAddress)) {
                neighbours.put(peer, 0); // Initialize with age 0
            }
        }

        System.out.println("Initialized peer " + myFullAddress + " with neighbors: " + neighbours.keySet());
    }

    public void start() {
        // Start server thread
        executor.submit(this::listenForConnections);

        // Start periodic age increment
        executor.scheduleAtFixedRate(this::incrementAges, 1, 1, TimeUnit.SECONDS);

        // Start periodic shuffle
        executor.scheduleAtFixedRate(this::cyclonShuffle, 10, 10, TimeUnit.SECONDS);
    }

    public Map<String, Integer> getNeighbours() {
        return new HashMap<>(neighbours);
    }

    private void listenForConnections() {
        try {
            InetAddress bindAddr = InetAddress.getByName(address);
            try (ServerSocket serverSocket = new ServerSocket(port, 50, bindAddr)) {
                System.out.println("Listening on " + address + ":" + port);
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
            Map<String, Integer> receivedPeers = (Map<String, Integer>) in.readObject();

            // Select random subset to send back
            int shuffleLength = Math.min(neighbours.size(), receivedPeers.size());
            Map<String, Integer> toSend = selectRandomSubset(shuffleLength);

            // Send back our subset
            out.writeObject(toSend);

            // Merge received peers into our view
            mergePeerList(receivedPeers);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void incrementAges() {
        for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
            neighbours.put(entry.getKey(), entry.getValue() + 1);
        }
    }

    private void cyclonShuffle() {
        if (neighbours.isEmpty()) return;

        // 1. Select oldest peer as target
        String oldestPeer = findOldestPeer();
        if (oldestPeer == null) return;

        // 2. Select random subset of our view (excluding the oldest)
        int shuffleLength = Math.min(viewSize / 2, neighbours.size() - 1);
        Map<String, Integer> toSend = selectRandomSubset(shuffleLength);

        // 3. Add ourselves with age 0
        String selfAddress = address + ":" + port;
        toSend.put(selfAddress, 0);

        try {
            String[] parts = oldestPeer.split(":");
            String host = parts[0];
            int targetPort = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket(host, targetPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // 4. Send our subset to the target
                out.writeObject(toSend);

                // 5. Receive their subset
                @SuppressWarnings("unchecked")
                Map<String, Integer> receivedPeers = (Map<String, Integer>) in.readObject();

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

    private String findOldestPeer() {
        String oldestPeer = null;
        int maxAge = -1;

        for (Map.Entry<String, Integer> entry : neighbours.entrySet()) {
            if (entry.getValue() > maxAge) {
                maxAge = entry.getValue();
                oldestPeer = entry.getKey();
            }
        }

        return oldestPeer;
    }

    private Map<String, Integer> selectRandomSubset(int count) {
        Map<String, Integer> subset = new HashMap<>();
        List<String> peers = new ArrayList<>(neighbours.keySet());
        Collections.shuffle(peers);

        int actualCount = Math.min(count, peers.size());
        for (int i = 0; i < actualCount; i++) {
            String peer = peers.get(i);
            subset.put(peer, neighbours.get(peer));
        }

        return subset;
    }

    private void mergePeerList(Map<String, Integer> newPeers) {
        // Remove self from received peers
        String selfAddress = address + ":" + port;
        newPeers.remove(selfAddress);

        // Merge with local view, keeping view size limited
        while (!newPeers.isEmpty() && neighbours.size() < viewSize) {
            // Find a peer to add (preferably young ones)
            String peerToAdd = findYoungestPeer(newPeers);
            neighbours.put(peerToAdd, newPeers.get(peerToAdd));
            newPeers.remove(peerToAdd);
        }
    }

    private String findYoungestPeer(Map<String, Integer> peers) {
        String youngestPeer = null;
        int minAge = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : peers.entrySet()) {
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

    private List<String> readInitialPeers(String filePath, String myAddress) throws IOException {
        List<String> peers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Split only on the first colon to separate address from neighbors
                int firstColonIndex = line.indexOf(":");
                if (firstColonIndex == -1) continue;

                // Now look for the second colon (after port)
                int secondColonIndex = line.indexOf(":", firstColonIndex + 1);
                if (secondColonIndex == -1) continue;

                String address = line.substring(0, secondColonIndex).trim();
                String neighborPart = line.substring(secondColonIndex + 1).trim();

                if (address.equals(myAddress)) {
                    String[] neighbors = neighborPart.split(",");
                    for (String neighbor : neighbors) {
                        String trimmed = neighbor.trim();
                        if (!trimmed.isEmpty() && !trimmed.equals(myAddress)) {
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