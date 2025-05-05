import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private static Map<String, List<Integer>> topics = new HashMap<>();
    private static ZMQ.Socket pullSocket;
    private static ZMQ.Socket pubSocket1;
    private static ZMQ.Socket puBSocket2;
    private static ZMQ.Socket subSocket;

    private static void joinTopic(String topic, List<Integer> servers) {
        topics.put(topic, servers);
        for (int server : servers) {
            subSocket.connect("tcp://*:" + server);
        }
        subSocket.subscribe(topic);
    }

    public static void main(String[] args) {

        int port = Integer.parseInt(args[0]);
        System.out.println("Chat server listening on port " + port);

        try (ZContext context = new ZContext()) {
            // Socket para receber mensagens do cliente
            pullSocket = context.createSocket(SocketType.PULL);
            pullSocket.bind("tcp://*:" + port);

            // Socket para enviar mensagens para o cliente
            pubSocket1 = context.createSocket(SocketType.PUB);
            pubSocket1.bind("tcp://*:" + port + 1);

            // Socket para enviar mensagens para os SCs
            puBSocket2 = context.createSocket(SocketType.PUB);
            puBSocket2.bind("tcp://*:" + port + 2);

            // Socket para receber mensagens dos SCs
            subSocket = context.createSocket(SocketType.SUB);
            joinTopic("MEI", List.of());

            // Thread para receber mensagens do cliente
            Thread receiverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = pullSocket.recvStr();
                    pubSocket1.send(message);
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Thread para receber mensagens dos SCs
            while (!Thread.currentThread().isInterrupted()) {
                String message = subSocket.recvStr();
                String[] parts = message.split(":", 2);
                String topic = parts[0];
                String content = parts[1];
                System.out.println("[" + topic + "] " + content);
                puBSocket2.send(message);
            }
        }
    }
}
