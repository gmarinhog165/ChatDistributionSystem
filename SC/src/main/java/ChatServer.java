import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ChatServer {
    private Map<String, List<Integer>> topics;
    private ZContext context;
    private ZMQ.Socket pullSocket;   // Socket para receber mensagens do cliente
    private ZMQ.Socket clPubSocket;  // Socket para enviar mensagens para o cliente
    private ZMQ.Socket scPuBSocket;  // Socket para enviar mensagens para os SCs
    private ZMQ.Socket subSocket;    // Socket para receber mensagens dos SCs

    public ChatServer(int port) {
        this.topics = new HashMap<>();
        this.context = new ZContext();

        this.pullSocket = context.createSocket(SocketType.PULL);
        pullSocket.bind("tcp://*:" + port);

        this.clPubSocket = context.createSocket(SocketType.PUB);
        clPubSocket.bind("tcp://*:" + (port + 1));

        this.scPuBSocket = context.createSocket(SocketType.PUB);
        scPuBSocket.bind("tcp://*:" + (port + 2));

        this.subSocket = context.createSocket(SocketType.SUB);
    }

    public void start() {
        // Thread para receber mensagens dos clientes
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String message = pullSocket.recvStr();
                scPuBSocket.send(message);
            }
        }).start();

        // Thread para receber mensagens dos SCs
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String message = subSocket.recvStr();
                clPubSocket.send(message);
            }
        }).start();
    }

    public void joinTopic(String topic, List<Integer> servers) {
        this.topics.put(topic, servers);
        for (int server : servers) {
            this.subSocket.connect("tcp://*:" + server);
        }
        this.subSocket.subscribe(topic);
    }
}
