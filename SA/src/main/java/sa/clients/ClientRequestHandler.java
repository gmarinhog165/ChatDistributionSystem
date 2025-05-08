package sa.clients;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import sa.sc.SCInfo;
import sa.overlay.CyclonPeer;
import sa.config.Config;
import sa.gossip.Aggregator;

public class ClientRequestHandler implements Runnable {
    String myIpAddr;
    CyclonPeer peer;
    private final Aggregator aggregator;
    
    private final ExecutorService handlerPool = Executors.newCachedThreadPool();
    
    public ClientRequestHandler(String myip, CyclonPeer cyclonPeer,Aggregator agregator) {
        this.myIpAddr = myip;
        this.peer = cyclonPeer;
        this.aggregator = agregator;
    }
    
    @Override
    public void run() {
        try {
            InetAddress bindAddr = InetAddress.getByName(this.myIpAddr);
            
            try (ServerSocket serverSocket = new ServerSocket(Config.CLIENT_PORT, 50, bindAddr)) {
                System.out.println("SA is listening for client requests on port " + Config.CLIENT_PORT);
                
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
                    //@todo lógica de criar os topicos nos scs 
                    CopyOnWriteArrayList<SCInfo> scInfoList = aggregationFuture.get(10, TimeUnit.SECONDS);
                    scInfoList.sort(Comparator
                    .comparingInt(SCInfo::getNumClients)
                    .thenComparingInt(SCInfo::getNumTopics));
                    System.out.println("Creating Topics on:");
                    System.out.println("Sorted SCs: " + scInfoList);
                    
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    System.err.println("Error getting aggregation results: " + e.getMessage());
                }
            });
            
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}