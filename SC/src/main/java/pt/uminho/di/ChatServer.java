package pt.uminho.di;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final String serverId;
    private final Server grpcServer;
    private final ChatServiceImpl chatService;

    public ChatServer(int port, String serverId) throws IOException {
        this.port = port;
        this.serverId = serverId;

        this.chatService = new ChatServiceImpl(serverId, port);

        this.grpcServer = ServerBuilder.forPort(port)
                .addService(this.chatService)
                .build();

        logger.info("Initialized ChatServer with ID: " + serverId + " on port: " + port);
    }

    public void start() throws IOException {
        grpcServer.start();
        logger.info("Server started, listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down gRPC server due to JVM shutdown");
                try {
                    ChatServer.this.stop();
                } catch (InterruptedException e) {
                    logger.severe("Error shutting down: " + e.getMessage());
                }
                logger.info("Server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (chatService != null) {
            logger.info("Shutting down chat service components...");
            chatService.shutdown();
        }

        if (grpcServer != null) {
            logger.info("Shutting down gRPC server...");
            grpcServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        // Default port
        int port = 50052;
        String hostname = InetAddress.getLocalHost().getHostName();

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid port number, using default: " + port);
            }
        }

        // Create server ID in format "hostname:port"
        String serverId = "localhost" + ":" + port;

        if (args.length > 1) {
            serverId = args[1];
        }

        logger.info("Starting server with ID: " + serverId + " on port: " + port);

        final ChatServer server = new ChatServer(port, serverId);
        server.start();

        server.blockUntilShutdown();
    }
}