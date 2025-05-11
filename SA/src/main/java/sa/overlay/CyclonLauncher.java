package sa.overlay;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sa.config.Config;

public class CyclonLauncher {
    private String filename;
    private Integer port;

    public CyclonLauncher(String filename, Integer port) {
        this.filename = filename;
        this.port = port;
    }

    public CyclonPeer launch() throws IOException {

        ScheduledExecutorService statusExecutor = Executors.newScheduledThreadPool(1);
            CyclonPeer peer = new CyclonPeer(this.port, this.filename);
            // Start the peer in a separate thread to avoid blocking
            new Thread(() -> {
                    System.out.println("Starting cyclon peer at port " + this.port);
                    peer.start();

                    // Schedule periodic status printing
                    statusExecutor.scheduleAtFixedRate(() -> {
                        Map<Integer, Integer> neighbors = peer.getNeighbours();
                        StringBuilder status = new StringBuilder();
                        status.append("Peer ").append(this.port).append(" Neighbors: {");

                        boolean first = true;
                        for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
                            if (!first) {
                                status.append(", ");
                            }
                            first = false;
                            status.append(entry.getKey()).append("=").append(entry.getValue());
                        }
                        status.append("}");

                        System.out.println(status.toString());
                    }, 2, 10, TimeUnit.SECONDS);

                
            }).start();

        System.out.println("Peer started. Press Ctrl+C to terminate.");
        return peer;
    }
}