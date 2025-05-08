package pt.uminho.di;

import pt.uminho.di.proto.ChatMessage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MessageContainer implements Serializable {

    private final String topic;
    private final ChatMessage message;
    private final Map<String, Integer> vectorClock;
    private final String senderId;

    public MessageContainer(String topic, ChatMessage message, Map<String, Integer> vectorClock, String senderId) {
        this.topic = topic;
        this.message = message;
        this.vectorClock = new HashMap<>(vectorClock);
        this.senderId = senderId;
    }

    public String getTopic() {
        return topic;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public Map<String, Integer> getVectorClock() {
        return new HashMap<>(vectorClock);
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
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public static MessageContainer fromBytes(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return (MessageContainer) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }

    @Override
    public String toString() {
        return String.format("MessageContainer{topic='%s', senderId='%s', message=%s}",
                topic, senderId, message.getContent());
    }
}