import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class TestClient {
    private static final String DEFAULT_ROOM = "MEI";

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            int port = Integer.parseInt(args[0]);

            ZMQ.Socket pusher = context.createSocket(SocketType.PUSH);
            pusher.connect("tcp://*:" + port);

            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://*:" + port + 1);

            String currentRoom = DEFAULT_ROOM;
            subscriber.subscribe(currentRoom);

            Thread receiverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = subscriber.recvStr();
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
                pusher.send(currentRoom + ":" + input);
            }
        }
    }
}
