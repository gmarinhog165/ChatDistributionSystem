package sa.overlay;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CyclonLauncher {
    private String filename;
    private String selfaddr;

    public CyclonLauncher(String filename, String selfaddr) {
        this.filename = filename;
        this.selfaddr = selfaddr;
    }

    public void launch() throws IOException {

        ScheduledExecutorService statusExecutor = Executors.newScheduledThreadPool(1);

            // Start the peer in a separate thread to avoid blocking
            new Thread(() -> {
                try {
                    System.out.println("Starting peer at address " + this.selfaddr);
                    CyclonPeer peer = new CyclonPeer(this.selfaddr, this.filename);
                    peer.start();

                    // Schedule periodic status printing
                    statusExecutor.scheduleAtFixedRate(() -> {
                        Map<String, Integer> neighbors = peer.getNeighbours();
                        StringBuilder status = new StringBuilder();
                        status.append("Peer ").append(this.selfaddr).append(" Neighbors: {");

                        boolean first = true;
                        for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                            if (!first) {
                                status.append(", ");
                            }
                            first = false;
                            status.append(entry.getKey()).append("=").append(entry.getValue());
                        }
                        status.append("}");

                        System.out.println(status.toString());
                    }, 2, 10, TimeUnit.SECONDS);

                } catch (IOException e) {
                    System.out.println("Error starting peer " + this.selfaddr + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

        System.out.println("Peer started. Press Ctrl+C to terminate.");
    }
}