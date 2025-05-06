import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ChatServer {
    // Server info
    private int port;

    // Topics and servers
    private Map<String, List<Integer>> topics;

    // ZeroMQ context e sockets
    private ZContext context;
    private ZMQ.Socket pullSocket;   // Socket para receber mensagens do cliente
    private ZMQ.Socket clPubSocket;  // Socket para enviar mensagens para o cliente
    private ZMQ.Socket scPuBSocket;  // Socket para enviar mensagens para os SCs
    private ZMQ.Socket subSocket;    // Socket para receber mensagens dos SCs

    // Causal broadcast implementation
    private Map<String, Map<Integer, Integer>> versionVectors;
    private Map<String, List<Message>> buffer;

    public ChatServer(int port) {
        this.port = port;
        this.topics = new HashMap<>();
        this.versionVectors = new HashMap<>();
        this.buffer = new HashMap<>();

        // Initialize ZeroMQ context and sockets
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
                String[] parts = message.split(":", 2);
                String topic = parts[0];
                String content = parts[1];

                Map<Integer, Integer> vv = versionVectors.get(topic);
                vv.put((port + 2), vv.get((port + 2)) + 1);
                versionVectors.put(topic, vv);

                Message msg = new Message(topic, content, new HashMap<>(vv), (port + 2));
                scPuBSocket.sendMore(topic);
                scPuBSocket.send(msg.toBytes());
            }
        }).start();

        // Thread para receber mensagens dos SCs
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String topic = subSocket.recvStr();
                byte[] data = subSocket.recv();

                Message msg = Message.fromBytes(data);
                handleMessage(topic, msg);
            }
        }).start();
    }

    public void joinTopic(String topic, List<Integer> servers) {
        this.topics.put(topic, servers);

        Map<Integer, Integer> topicClock = new HashMap<>();
        for (Integer server : servers) {
            topicClock.put(server, 0);
        }
        topicClock.put((port + 2), 0);

        this.versionVectors.put(topic, topicClock);
        this.buffer.put(topic, new ArrayList<>());

        for (int server : servers) {
            this.subSocket.connect("tcp://*:" + server);
        }
        this.subSocket.subscribe(topic);
    }

    private void handleMessage(String topic, Message msg) {
        Map<Integer, Integer> localVV = versionVectors.get(topic);

        if (canDeliver(msg, localVV)) {
            deliver(topic, msg);
            tryDeliverBuffered(topic);
        } else {
            buffer.get(topic).add(msg);
        }
    }

    private boolean canDeliver(Message msg, Map<Integer, Integer> localVV) {
        int j = msg.senderId;
        Map<Integer, Integer> msgVV = msg.versionVector;

        if (msgVV.get(j) != localVV.get(j) + 1)
            return false;

        for (Integer k : localVV.keySet()) {
            if (k != j && msgVV.get(k) > localVV.get(k))
                return false;
        }
        return true;
    }

    private void deliver(String topic, Message msg) {
        versionVectors.get(topic).put(msg.senderId, msg.versionVector.get(msg.senderId));
        clPubSocket.send(msg.topic + ":" + msg.message);
    }

    private void tryDeliverBuffered(String topic) {
        List<Message> b = buffer.get(topic);
        boolean progress;
        do {
            progress = false;
            List<Message> toRemove = new ArrayList<>();
            for (Message m : b) {
                if (canDeliver(m, versionVectors.get(topic))) {
                    deliver(topic, m);
                    toRemove.add(m);
                    progress = true;
                }
            }
            b.removeAll(toRemove);
        } while (progress);
    }

}
