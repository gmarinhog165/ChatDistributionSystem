package pt.uminho.di.teste;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SAMock {
    public static void main(String[] args) throws Exception {
        String topic = "test_topic";
        String servers = "localhost:50052,localhost:50062";

        try (ZContext context = new ZContext()) {
            // Create publisher sockets for each SC
            ZMQ.Socket pubSocket1 = context.createSocket(SocketType.PUB);
            ZMQ.Socket pubSocket2 = context.createSocket(SocketType.PUB);

            // Bind to ports that SCs are listening on (SC port - 1)
            pubSocket1.bind("tcp://*:50051");
            pubSocket2.bind("tcp://*:50061");

            System.out.println("SA Mock started, press Enter to send topic configuration...");
            System.in.read(); // Wait for user input

            // Send topic configuration to both SCs
            System.out.println("Sending topic configuration to SC1...");
            pubSocket1.sendMore(topic);
            pubSocket1.send(servers);

            System.out.println("Sending topic configuration to SC2...");
            pubSocket2.sendMore(topic);
            pubSocket2.send(servers);

            System.out.println("Topic configuration sent. Press Enter to exit.");
            System.in.read(); // Wait for user input before exiting
        }
    }
}