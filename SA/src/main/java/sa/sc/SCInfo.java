package sa.sc;

public class SCInfo {
    public String address;
    public int port;
    public int numClients;
    public int numTopics;

    public SCInfo(String address, int port,int numClients,int numTopics) {
        this.address = address;
        this.port = port;
        this.numClients = numClients;
        this.numTopics = numTopics;
    }

    @Override
    public String toString() {
        return address + ":" + port + "," + numClients + "," + numTopics;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
