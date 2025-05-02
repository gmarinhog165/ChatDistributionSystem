import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Cliente melhorado para testar servidores SP (Service Provider)
 */
public class TestSPClient {
    private String host;
    private int port;
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean verbose;
    
    /**
     * Construtor para o cliente SP
     * @param host Endereço do servidor
     * @param port Porta do servidor
     * @param verbose Se true, imprime mensagens detalhadas
     * @throws IOException Em caso de erro de conexão
     */
    public TestSPClient(String host, int port, boolean verbose) throws IOException {
        this.host = host;
        this.port = port;
        this.verbose = verbose;
        connect();
    }
    
    /**
     * Construtor com verbose=true por padrão
     */
    public TestSPClient(String host, int port) throws IOException {
        this(host, port, true);
    }
    
    /**
     * Conecta ao servidor
     */
    private void connect() throws IOException {
        if (verbose) System.out.println("Conectando a " + host + ":" + port + "...");
        socket = new Socket(host, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        if (verbose) System.out.println("Conectado com sucesso!");
    }
    
    /**
     * Fecha a conexão
     */
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            if (verbose) System.out.println("Fechando conexão...");
            socket.close();
            if (verbose) System.out.println("Conexão fechada.");
        }
    }
    
    /**
     * Envia um comando e retorna a resposta
     */
    public String sendCommand(String command) throws IOException {
        if (verbose) System.out.println("Enviando: " + command);
        out.write(command + "\n");
        out.flush();
        
        String response = in.readLine();
        if (verbose) System.out.println("Recebido: " + response);
        return response;
    }
    
    /**
     * Obtém todos os tópicos registrados
     */
    public List<String> getTopics() throws IOException {
        String response = sendCommand("GET_TOPICS");
        return parseJsonArray(response);
    }
    
    /**
     * Obtém servidores para um tópico específico
     */
    public List<ServerAddress> getServersForTopic(String topic) throws IOException {
        String response = sendCommand("GET_SCS:" + topic);
        return parseServerList(response);
    }
    
    /**
     * Registra um novo tópico com seus servidores
     */
    public boolean registerTopic(String topic, List<ServerAddress> servers) throws IOException {
        StringBuilder sb = new StringBuilder("REGISTER_TOPIC:");
        sb.append(topic).append(":");
        
        for (int i = 0; i < servers.size(); i++) {
            ServerAddress server = servers.get(i);
            sb.append(server.getIp()).append(":").append(server.getPort());
            if (i < servers.size() - 1) {
                sb.append(",");
            }
        }
        
        String response = sendCommand(sb.toString());
        return "OK".equals(response);
    }
    
    /**
     * Registra um tópico com um único servidor
     */
    public boolean registerTopic(String topic, String ip, int port) throws IOException {
        List<ServerAddress> servers = new ArrayList<>();
        servers.add(new ServerAddress(ip, port));
        return registerTopic(topic, servers);
    }
    
    /**
     * Registra um tópico com múltiplos servidores usando string no formato "ip1:port1,ip2:port2"
     */
    public boolean registerTopic(String topic, String serversStr) throws IOException {
        List<ServerAddress> servers = new ArrayList<>();
        String[] serverEntries = serversStr.split(",");
        
        for (String entry : serverEntries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                servers.add(new ServerAddress(parts[0], Integer.parseInt(parts[1])));
            }
        }
        
        return registerTopic(topic, servers);
    }
    
    /**
     * Parse simples de array JSON
     */
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
    
    /**
     * Parse de lista de servidores a partir de JSON
     */
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
                    System.err.println("Erro ao analisar servidor: " + item);
                    if (verbose) e.printStackTrace();
                }
            }
        }
        
        return servers;
    }
    
    /**
     * Classe interna para representar um endereço de servidor
     */
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

    /**
     * Executa testes automatizados em um servidor
     */
    public void runAutomatedTests() throws IOException {
        System.out.println("\n===== INICIANDO TESTES AUTOMATIZADOS =====");
        
        // Teste 1: Registrar tópicos
        System.out.println("\nTeste 1: Registrando tópicos...");
        registerTopic("games", "192.168.1.10:5000,192.168.1.11:5001");
        registerTopic("musica", "192.168.1.10:5000");
        registerTopic("sports", "192.168.1.12:3000");
        
        // Teste 2: Listar tópicos
        System.out.println("\nTeste 2: Listando tópicos...");
        List<String> topics = getTopics();
        System.out.println("Tópicos disponíveis: " + topics);
        
        // Teste 3: Obter servidores para um tópico
        System.out.println("\nTeste 3: Obtendo servidores para tópicos...");
        for (String topic : topics) {
            List<ServerAddress> servers = getServersForTopic(topic);
            System.out.println("Servidores para '" + topic + "': " + servers);
        }
        
        System.out.println("\n===== TESTES AUTOMATIZADOS CONCLUÍDOS =====");
    }
}