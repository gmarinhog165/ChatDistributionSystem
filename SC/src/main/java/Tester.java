import java.util.List;

public class Tester {
    public static void main(String[] args) {
        ChatServer server1 = new ChatServer(5555);
        server1.joinTopic("MEI", List.of(5560, 5563));
        server1.start();

        ChatServer server2 = new ChatServer(5558);
        server2.joinTopic("MEI", List.of(5557, 5563));
        server2.start();

        ChatServer server3 = new ChatServer(5561);
        server3.joinTopic("MEI", List.of(5557, 5560));
        server3.start();
    }
}
