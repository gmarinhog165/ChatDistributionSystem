import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ChatServer2 {
    private static ZMQ.Socket pullSocket;  // Socket para receber mensagens do cliente
    private static ZMQ.Socket pubSocket1;  // Socket para enviar mensagens para o cliente
    private static ZMQ.Socket puBSocket2;  // Socket para enviar mensagens para os SCs
    private static ZMQ.Socket subSocket;   // Socket para receber mensagens dos SCs

    public static void main(String[] args) {
        System.out.println("Chat server listening on port 6666");

        try (ZContext context = new ZContext()) {
            pullSocket = context.createSocket(SocketType.PULL);
            pullSocket.bind("tcp://*:" + 6666);

            pubSocket1 = context.createSocket(SocketType.PUB);
            pubSocket1.bind("tcp://*:" + 6667);

            puBSocket2 = context.createSocket(SocketType.PUB);
            puBSocket2.bind("tcp://*:" + 6668);

            subSocket = context.createSocket(SocketType.SUB);
            subSocket.connect("tcp://*:" + 5557);
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
