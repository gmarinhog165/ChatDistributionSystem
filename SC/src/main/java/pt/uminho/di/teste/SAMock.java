package pt.uminho.di.teste;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SAMock {
    private static final String CMD_TOPIC_CONFIG = "TOPIC_CONFIG";
    private static final String CMD_STATUS_REQUEST = "STATUS_REQUEST";

    public static void main(String[] args) throws Exception {
        String topic = "test_topic";
        String servers = "localhost:30000,localhost:20000";
        int scPort = 6997; // The SC port we're targeting

        try (ZContext context = new ZContext()) {
            // Create publisher socket for topic configuration
            ZMQ.Socket pubSocket = context.createSocket(SocketType.PUB);
            pubSocket.bind("tcp://*:" + (scPort - 1));

            // Create request socket for status queries
            ZMQ.Socket reqSocket = context.createSocket(SocketType.REQ);
            reqSocket.connect("tcp://localhost:" + (scPort + 200));

            System.out.println("SA Mock started");
            Thread.sleep(10);
                    // Send topic configuration
            System.out.println("Sending topic configuration...");
            pubSocket.sendMore(CMD_TOPIC_CONFIG);
            pubSocket.sendMore(topic);
            pubSocket.send(servers);
            System.out.println("Topic configuration sent");

        }
    }
}