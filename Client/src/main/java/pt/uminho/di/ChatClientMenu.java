package pt.uminho.di;

import java.util.List;
import java.util.Scanner;

/**
 * Client menu class for handling user interaction
 */
public class ChatClientMenu {
    private final String username;
    private final SPClient spClient;
    private final SCClient scClient;
    private final SAClient saClient;

    /**
     * Constructor for ChatClientMenu
     */
    public ChatClientMenu(String username, SPClient spClient, SCClient scClient, SAClient saClient) {
        this.username = username;
        this.spClient = spClient;
        this.scClient = scClient;
        this.saClient = saClient;

        // Set username in SC client
        scClient.setUsername(username);
    }

    /**
     * Main command loop for the client
     */
    public void commandLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Distributed Chat Client, " + username + "!");
        System.out.println("Type /help for available commands.");

        while (running) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // Process commands
            if (line.startsWith("/")) {
                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String arg = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "/topics":
                        listTopics();
                        break;

                    case "/servers":
                        if (arg.isEmpty()) {
                            System.out.println("Usage: /servers <topic>");
                        } else {
                            listServersForTopic(arg);
                        }
                        break;

                    case "/create":
                        if (arg.isEmpty()) {
                            System.out.println("Usage: /create <topic>");
                        } else {
                            saClient.createTopic(arg);
                        }
                        break;

                    case "/join":
                        if (arg.isEmpty()) {
                            System.out.println("Usage: /join <topic>");
                        } else {
                            joinTopic(arg);
                        }
                        break;

                    case "/leave":
                        scClient.leaveTopic();
                        break;

                    case "/users":
                        String currentTopic = scClient.getCurrentTopic();
                        if (currentTopic != null) {
                            scClient.displayActiveUsers(currentTopic);
                        } else {
                            System.out.println("Join a topic first.");
                        }
                        break;

                    case "/history":
                        showChatHistory(arg);
                        break;

                    case "/quit":
                    case "/exit":
                        running = false;
                        break;

                    case "/help":
                        showHelp();
                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Type /help for available commands.");
                        break;
                }
            } else {
                // Send as a message
                String currentTopic = scClient.getCurrentTopic();
                if (currentTopic != null) {
                    scClient.sendMessage(line);
                } else {
                    System.out.println("Join a topic first with /join <topic>");
                }
            }
        }

        System.out.println("Exiting...");
        scanner.close();
    }

    /**
     * List available topics
     */
    private void listTopics() {
        List<String> topics = spClient.getAvailableTopics();
        if (topics.isEmpty()) {
            System.out.println("No topics available.");
        } else {
            System.out.println("Available topics:");
            for (String topic : topics) {
                System.out.println("- " + topic);
            }
        }
    }

    /**
     * List servers for a specific topic
     */
    private void listServersForTopic(String topic) {
        List<ServerInfo> servers = spClient.getServersForTopic(topic);
        if (servers.isEmpty()) {
            System.out.println("No servers available for topic: " + topic);
        } else {
            System.out.println("Available servers for topic " + topic + ":");
            for (ServerInfo server : servers) {
                System.out.println("- " + server);
            }
        }
    }

    /**
     * Join a topic
     */
    private void joinTopic(String topic) {
        // Get servers for this topic
        List<ServerInfo> servers = spClient.getServersForTopic(topic);

        if (servers.isEmpty()) {
            System.out.println("No servers available for topic: " + topic);
            return;
        }

        // Set the topic servers in SC client
        scClient.setTopicServers(servers);

        // Connect to the first available server
        boolean connected = false;
        for (ServerInfo server : servers) {
            if (scClient.connectToServer(server)) {
                connected = true;
                break;
            }
        }

        if (!connected) {
            System.out.println("Failed to connect to any server for topic: " + topic);
            return;
        }

        // Join the topic
        scClient.joinTopic(topic);
    }

    /**
     * Show chat history
     */
    private void showChatHistory(String arg) {
        String currentTopic = scClient.getCurrentTopic();
        if (currentTopic != null) {
            int limit = 20;
            String user = null;

            if (!arg.isEmpty()) {
                String[] args = arg.split("\\s+", 2);
                try {
                    limit = Integer.parseInt(args[0]);
                    if (args.length > 1) {
                        user = args[1];
                    }
                } catch (NumberFormatException e) {
                    // If not a number, consider it as username
                    user = args[0];
                }
            }
            scClient.displayChatHistory(currentTopic, limit, user);
        } else {
            System.out.println("Join a topic first.");
        }
    }

    /**
     * Show help message
     */
    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("/topics - List all available topics");
        System.out.println("/servers <topic> - List servers for a specific topic");
        System.out.println("/create <topic> - Create a new topic");
        System.out.println("/join <topic> - Join a chat topic");
        System.out.println("/leave - Leave the current topic");
        System.out.println("/users - Display active users in current topic");
        System.out.println("/history [limit] [user] - Display message history (default: 20)");
        System.out.println("/quit or /exit - Exit the client");
        System.out.println("/help - Display this help message");
        System.out.println("Just type a message to send to the current topic");
    }

    /**
     * Shutdown the client
     */
    public void shutdown() throws InterruptedException {
        // Shutdown all the clients
        scClient.shutdown();
        saClient.shutdown();
        System.out.println("Client shutdown complete.");
    }
}
