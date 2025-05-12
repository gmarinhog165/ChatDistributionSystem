package pt.uminho.di;

import java.io.*;
import java.util.Map;
import java.util.Set;

public class UserSetContainer implements Serializable {
    private final String topic;
    private final Map<String, Integer> causalContext;
    private final Map<String, Set<Dot>> dotMap;
    private final String senderId;

    public UserSetContainer(String topic, Map<String, Integer> causalContext,
                            Map<String, Set<Dot>> dotMap, String senderId) {
        this.topic = topic;
        this.causalContext = causalContext;
        this.dotMap = dotMap;
        this.senderId = senderId;
    }

    public String getTopic() {
        return topic;
    }

    public Map<String, Integer> getCausalContext() {
        return causalContext;
    }

    public Map<String, Set<Dot>> getDotMap() {
        return dotMap;
    }

    public String getSenderId() {
        return senderId;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize UserSetContainer", e);
        }
    }

    public static UserSetContainer fromBytes(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return (UserSetContainer) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize UserSetContainer", e);
        }
    }
}
