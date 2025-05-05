import java.io.Serializable;
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
}
