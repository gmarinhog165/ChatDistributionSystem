import java.io.*;
import java.net.*;
import java.util.*;

public class TestSPClient {
    private String host;
    private int port;
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    
    public TestSPClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        connect();
    }
    
    private void connect() throws IOException {
        socket = new Socket(host, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
    
    public List<String> getTopics() throws IOException {
        out.write("GET_TOPICS\n");
        out.flush();
        
        String response = in.readLine();
        System.out.println("Topics Response: " + response);
        
        return parseJsonArray(response);
    }
    
    public List<ServerAddress> getServersForTopic(String topic) throws IOException {
        out.write("GET_SCS:" + topic + "\n");
        out.flush();
        
        String response = in.readLine();
        System.out.println("SCs for " + topic + ": " + response);
        
        return parseServerList(response);
    }
    
    public void registerTopic(String topic, List<ServerAddress> servers) throws IOException {
        StringBuilder sb = new StringBuilder("REGISTER_TOPIC:");
        sb.append(topic).append(":");
        
        for (int i = 0; i < servers.size(); i++) {
            ServerAddress server = servers.get(i);
            sb.append(server.getIp()).append(":").append(server.getPort());
            if (i < servers.size() - 1) {
                sb.append(",");
            }
        }
        
        out.write(sb.toString() + "\n");
        out.flush();
        
        String response = in.readLine();
        System.out.println("Register Response: " + response);
    }
    
    // Simple JSON array parsing (for demonstration purposes)
    private List<String> parseJsonArray(String json) {
        List<String> result = new ArrayList<>();
        
        // Remove brackets
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1);
        }
        
        // Parse items
        if (!json.isEmpty()) {
            String[] items = json.split(",");
            for (String item : items) {
                // Remove quotes
                item = item.trim();
                if (item.startsWith("\"") && item.endsWith("\"")) {
                    item = item.substring(1, item.length() - 1);
                }
                result.add(item);
            }
        }
        
        return result;
    }
    
    // Parse server list from JSON
    private List<ServerAddress> parseServerList(String json) {
        List<ServerAddress> servers = new ArrayList<>();
        
        // Remove brackets
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1);
        }
        
        // Parse items
        if (!json.isEmpty()) {
            String[] items = json.split("},");
            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (!item.endsWith("}")) {
                    item += "}";
                }
                
                // Extract IP and port
                try {
                    int ipStart = item.indexOf("\"ip\":\"") + 6;
                    int ipEnd = item.indexOf("\"", ipStart);
                    String ip = item.substring(ipStart, ipEnd);
                    
                    int portStart = item.indexOf("\"port\":") + 7;
                    int portEnd = item.indexOf("}", portStart);
                    int port = Integer.parseInt(item.substring(portStart, portEnd).trim());
                    
                    servers.add(new ServerAddress(ip, port));
                } catch (Exception e) {
                    System.err.println("Error parsing server: " + item);
                    e.printStackTrace();
                }
            }
        }
        
        return servers;
    }
    
    // Inner class to represent a server address
    public static class ServerAddress {
        private String ip;
        private int port;
        
        public ServerAddress(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
        public String getIp() {
            return ip;
        }
        
        public int getPort() {
            return port;
        }
        
        @Override
        public String toString() {
            return "{ip:\"" + ip + "\", port:" + port + "}";
        }
    }
}