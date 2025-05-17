package pt.uminho.di;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for communication with the SP server via TCP
 * Uses a persistent connection for better performance
 */
public class SPClient implements AutoCloseable {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;

    public SPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Establishes connection to the SP server
     */
    public void connect() throws IOException {
        if (!connected) {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        }
    }

    /**
     * Closes the connection to the SP server
     */
    @Override
    public void close() {
        if (connected) {
            try {
                in.close();
                out.close();
                socket.close();
                connected = false;
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Ensures connection is established before sending commands
     */
    private void ensureConnected() throws IOException {
        if (!connected) {
            connect();
        }
    }

    /**
     * Query SP for available topics using TCP
     */
    public List<String> getAvailableTopics() {
        List<String> topics = new ArrayList<>();

        try {
            ensureConnected();

            // Send GET_TOPICS command
            out.println("GET_TOPICS");

            // Read response
            String response = in.readLine();
            if (response != null && !response.startsWith("ERROR")) {
                // Parse JSON response
                topics = Utilities.parseJsonStringArray(response);
            } else {
                System.err.println("Error getting topics: " + (response != null ? response : "No response"));
            }
        } catch (IOException e) {
            System.err.println("Error communicating with SP server: " + e.getMessage());
            reconnect();
        }

        return topics;
    }

    /**
     * Query SP for SC servers for a specific topic using TCP
     */
    public List<ServerInfo> getServersForTopic(String topic) {
        List<ServerInfo> servers = new ArrayList<>();

        try {
            ensureConnected();

            // Send GET_SCS command
            out.println("GET_SCS:" + topic);

            // Read response
            String response = in.readLine();
            if (response != null && !response.startsWith("ERROR")) {
                // Parse JSON response
                servers = Utilities.parseJsonServerArray(response);
            } else {
                System.err.println("Error getting servers for topic " + topic + ": " + (response != null ? response : "No response"));
            }
        } catch (IOException e) {
            System.err.println("Error communicating with SP server: " + e.getMessage());
            reconnect();
        }

        return servers;
    }

    /**
     * Attempt to reconnect if connection was lost
     */
    private void reconnect() {
        close();
        try {
            connect();
        } catch (IOException e) {
            System.err.println("Failed to reconnect: " + e.getMessage());
        }
    }
}