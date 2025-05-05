import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (ZContext context = new ZContext()) {
            ZMQ.Socket pushSocket = context.createSocket(SocketType.PUSH);
            pushSocket.connect("tcp://*:" + port);

            ZMQ.Socket subSocket = context.createSocket(SocketType.SUB);
            subSocket.connect("tcp://*:" + (port + 1));

            String room = args[1];
            subSocket.subscribe(room);

            Thread receiverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = subSocket.recvStr();
                    String[] parts = message.split(":", 2);
                    String topic = parts[0];
                    String content = parts[1];
                    System.out.println("[" + topic + "] " + content);
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                pushSocket.send(room + ":" + input);
            }
        }
    }
}
