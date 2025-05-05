import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ChatServer2 {
    private static ZMQ.Socket pullSocket;  // Socket para receber mensagens do cliente
    private static ZMQ.Socket pubSocket1;  // Socket para enviar mensagens para o cliente
    private static ZMQ.Socket puBSocket2;  // Socket para enviar mensagens para os SCs
    private static ZMQ.Socket subSocket;   // Socket para receber mensagens dos SCs

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        System.out.println("Chat server listening on port: " + port);

        try (ZContext context = new ZContext()) {
            pullSocket = context.createSocket(SocketType.PULL);
            pullSocket.bind("tcp://*:" + port);

            pubSocket1 = context.createSocket(SocketType.PUB);
            pubSocket1.bind("tcp://*:" + (port + 1));

            puBSocket2 = context.createSocket(SocketType.PUB);
            puBSocket2.bind("tcp://*:" + (port + 2));

            subSocket = context.createSocket(SocketType.SUB);
            subSocket.connect("tcp://*:" + 5557);
            subSocket.connect("tcp://*:" + 5560);
            subSocket.subscribe("MEI");

            // Thread para receber mensagens do cliente
            Thread receiverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = pullSocket.recvStr();
                    puBSocket2.send(message);
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Thread para receber mensagens dos SCs
            while (!Thread.currentThread().isInterrupted()) {
                String message = subSocket.recvStr();
                pubSocket1.send(message);
            }
        }
    }
}
