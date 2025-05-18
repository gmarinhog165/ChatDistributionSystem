package pt.uminho.di;

import java.io.*;
import java.net.*;

public class SAClient {

    private final String host;
    private final int port;
    private final String username; // Username assumed to be needed as per protocol
    private final int spPort;

    public SAClient(String host, int port, String username,int spPort) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.spPort = spPort;
    }

    /**
     * Communicate with SA to create a new topic
     */
    public void createTopic(String topic) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String request = "CREATE_TOPIC " + topic + " " + username + " " + spPort;
            System.out.println("Sending: " + request);
            out.write(request + "\n");
            out.flush();

            String response = in.readLine();
            System.out.println("Response from SA: " + response);

        } catch (IOException e) {
            System.err.println("Failed to create topic: " + e.getMessage());
        }
    }

    /**
     * Shutdown client connections
     */
    public void shutdown() {
        // Nothing needed yet
    }
}
