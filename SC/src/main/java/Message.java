import java.io.*;
import java.util.Map;

public class Message implements Serializable {
    public String topic;
    public String message;
    public Map<Integer, Integer> versionVector;
    public int senderId;

    public Message(String topic, String message, Map<Integer, Integer> versionVector, int senderId) {
        this.topic = topic;
        this.message = message;
        this.versionVector = versionVector;
        this.senderId = senderId;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao serializar a mensagem", e);
        }
    }

    public static Message fromBytes(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao desserializar a mensagem", e);
        }
    }

    public String toString() {
        return "Message{" +
                "topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", versionVector=" + versionVector +
                ", senderId=" + senderId +
                '}';
    }
}
