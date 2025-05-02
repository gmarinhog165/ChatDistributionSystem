import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSPClientMain {
    public static void main(String[] args) {
        try {
            // Basic test connecting to SP node
            runBasicTest();
            
            // Interactive test
            runInteractiveTest();
            
        } catch (Exception e) {
            System.err.println("Error in test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runBasicTest() throws IOException {
        System.out.println("Running basic SP client test...");
        
        TestSPClient client = new TestSPClient("localhost", 8080);
        
        // Register some topics with chat servers
        List<TestSPClient.ServerAddress> sportServers = new ArrayList<>();
        sportServers.add(new TestSPClient.ServerAddress("127.0.0.1", 9001));
        sportServers.add(new TestSPClient.ServerAddress("127.0.0.1", 9002));
        client.registerTopic("esportes", sportServers);
        
        List<TestSPClient.ServerAddress> musicServers = new ArrayList<>();
        musicServers.add(new TestSPClient.ServerAddress("127.0.0.1", 9003));
        client.registerTopic("música", musicServers);
        
        // Get all topics
        List<String> topics = client.getTopics();
        System.out.println("Available topics: " + topics);
        
        // Get servers for a specific topic
        if (!topics.isEmpty()) {
            String testTopic = topics.get(0);
            List<TestSPClient.ServerAddress> servers = client.getServersForTopic(testTopic);
            System.out.println("Servers for topic '" + testTopic + "': " + servers);
        }
        
        client.close();
        
        System.out.println("Basic test completed.\n");
    }
    
    private static void runInteractiveTest() throws IOException {
        System.out.println("Starting interactive SP client test...");
        System.out.println("--------------------------------------");
        
        Scanner scanner = new Scanner(System.in);
        TestSPClient client = new TestSPClient("localhost", 8080);
        
        boolean running = true;
        while (running) {
            System.out.println("\nChoose an option:");
            System.out.println("1. List all topics");
            System.out.println("2. Get servers for a topic");
            System.out.println("3. Register a new topic");
            System.out.println("4. Exit");
            System.out.print("> ");
            
            String option = scanner.nextLine();
            
            try {
                switch (option) {
                    case "1":
                        List<String> topics = client.getTopics();
                        System.out.println("Available topics: " + topics);
                        break;
                        
                    case "2":
                        System.out.print("Enter topic name: ");
                        String topic = scanner.nextLine();
                        List<TestSPClient.ServerAddress> servers = client.getServersForTopic(topic);
                        System.out.println("Servers for topic '" + topic + "': " + servers);
                        break;
                        
                    case "3":
                        System.out.print("Enter new topic name: ");
                        String newTopic = scanner.nextLine();
                        
                        System.out.print("Enter number of servers: ");
                        int numServers = Integer.parseInt(scanner.nextLine());
                        
                        List<TestSPClient.ServerAddress> newServers = new ArrayList<>();
                        for (int i = 0; i < numServers; i++) {
                            System.out.print("Enter server " + (i+1) + " IP: ");
                            String ip = scanner.nextLine();
                            
                            System.out.print("Enter server " + (i+1) + " port: ");
                            int port = Integer.parseInt(scanner.nextLine());
                            
                            newServers.add(new TestSPClient.ServerAddress(ip, port));
                        }
                        
                        client.registerTopic(newTopic, newServers);
                        System.out.println("Topic registered!");
                        break;
                        
                    case "4":
                        running = false;
                        break;
                        
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        client.close();
        scanner.close();
        System.out.println("Interactive test completed.");
    }
}