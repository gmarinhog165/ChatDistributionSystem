package pt.uminho.di.teste;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SAMock {
    private static final String CMD_TOPIC_CONFIG = "TOPIC_CONFIG";
    private static final String CMD_STATUS_REQUEST = "STATUS_REQUEST";

    public static void main(String[] args) throws Exception {
        String topic = "test_topic";
        String servers = "localhost:50052,localhost:50062";
        int scPort = 50062; // The SC port we're targeting

        try (ZContext context = new ZContext()) {
            // Create publisher socket for topic configuration
            ZMQ.Socket pubSocket = context.createSocket(SocketType.PUB);
            pubSocket.bind("tcp://*:" + (scPort - 1));

            // Create request socket for status queries
            ZMQ.Socket reqSocket = context.createSocket(SocketType.REQ);
            reqSocket.connect("tcp://localhost:" + (scPort + 200));

            System.out.println("SA Mock started");

            // Menu loop
            while (true) {
                System.out.println("\nOptions:");
                System.out.println("1. Send topic configuration");
                System.out.println("2. Request server status");
                System.out.println("3. Exit");
                System.out.print("Select option: ");

                int option = System.in.read();
                while (System.in.available() > 0) System.in.read(); // Clear input buffer

                if (option == '1') {
                    // Send topic configuration
                    System.out.println("Sending topic configuration...");
                    pubSocket.sendMore(CMD_TOPIC_CONFIG);
                    pubSocket.sendMore(topic);
                    pubSocket.send(servers);
                    System.out.println("Topic configuration sent");

                } else if (option == '2') {
                    // Request server status
                    System.out.println("Requesting server status...");
                    reqSocket.send(CMD_STATUS_REQUEST);

                    // Wait for response with timeout
                    String response = reqSocket.recvStr(ZMQ.DONTWAIT);
                    int attempts = 0;

                    while (response == null && attempts < 10) {
                        Thread.sleep(100);
                        response = reqSocket.recvStr(ZMQ.DONTWAIT);
                        attempts++;
                    }

                    if (response != null) {
                        String[] parts = response.split(":");
                        if (parts.length == 2) {
                            System.out.println("Server status received:");
                            System.out.println("- Active clients: " + parts[0]);
                            System.out.println("- Active topics: " + parts[1]);
                        } else {
                            System.out.println("Received unexpected response: " + response);
                        }
                    } else {
                        System.out.println("No response received from server");
                    }

                } else if (option == '3') {
                    System.out.println("Exiting...");
                    break;
                }
            }
        }
    }
}