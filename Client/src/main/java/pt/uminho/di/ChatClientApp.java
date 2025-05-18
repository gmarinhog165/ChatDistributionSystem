package pt.uminho.di;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the Chat Client Application
 */
public class ChatClientApp {
    private static final Logger logger = Logger.getLogger(ChatClientApp.class.getName());

    public static void main(String[] args) {
        String spHost = "localhost";
        int spPort = 30000;  // Default SP port

        String username = "user" + System.currentTimeMillis() % 1000; // Default username

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-h".equals(args[i]) || "--host".equals(args[i])) {
                if (i + 1 < args.length) {
                    spHost = args[++i];
                }
            } else if ("-p".equals(args[i]) || "--port".equals(args[i])) {
                if (i + 1 < args.length) {
                    try {
                        spPort = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number: " + args[i]);
                        System.exit(1);
                    }
                }
            } else if ("-u".equals(args[i]) || "--username".equals(args[i])) {
                if (i + 1 < args.length) {
                    username = args[++i];
                }
            }
        }

        // Create the clients
        SPClient spClient = new SPClient(spHost, spPort);
        SCClient scClient = new SCClient();
        SAClient saClient = new SAClient("localhost",9999,username,spPort);

        // Create and run the client menu
        final ChatClientMenu clientMenu = new ChatClientMenu(username, spClient, scClient, saClient);
        try {
            clientMenu.commandLoop();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Client error", e);
        } finally {
            try {
                clientMenu.shutdown();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Error shutting down client", e);
            }
        }
    }
}
