package sa.sc;

public class SCInfo {
    public int port;
    public int numClients;
    public int numTopics;

    public SCInfo(int port,int numClients,int numTopics) {
        this.port = port;
        this.numClients = numClients;
        this.numTopics = numTopics;
    }

    @Override
    public String toString() {
        return port + "," + numClients + "," + numTopics;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNumClients() {
        return numClients;
    }

    public void setNumClients(int numClients) {
        this.numClients = numClients;
    }

    public int getNumTopics() {
        return numTopics;
    }

    public void setNumTopics(int numTopics) {
        this.numTopics = numTopics;
    }

    public void addClient() {
        numClients++;
    }
    public void addTopic() {
        numTopics++;
    }
}
