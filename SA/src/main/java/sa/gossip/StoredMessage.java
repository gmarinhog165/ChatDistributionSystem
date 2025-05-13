package sa.gossip;

public class StoredMessage{
        String topic;
        String username;
        String uuid;
        int ttl;

        public StoredMessage(String topic, String username, String uuid,int ttl){
            this.topic = topic;
            this.username = username;
            this.uuid = uuid;
            this.ttl = ttl;
        }

        String getTopic(){
            return this.topic;
        }

        String getUsername(){
            return this.username;
        }

        int getTTL(){
            return this.ttl;
        }

        String getuuid(){
            return this.uuid;
        }
    }