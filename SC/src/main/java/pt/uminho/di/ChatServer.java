package pt.uminho.di;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final Server grpcServer;

    public ChatServer(int port) throws IOException {
        this.port = port;

        this.grpcServer = ServerBuilder.forPort(port)
                .addService(new ChatServiceImpl())
                .build();
    }

    public void start() throws IOException {
        grpcServer.start();
        logger.info("Server started, listening on port " + port);

        // Add shutdown hook to gracefully stop the server
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
        if (grpcServer != null) {
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

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid port number, using default: " + port);
            }
        }

        final ChatServer server = new ChatServer(port);
        server.start();

        // Keep the main thread from exiting
        server.blockUntilShutdown();
    }
}