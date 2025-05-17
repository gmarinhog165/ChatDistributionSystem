package pt.uminho.di;

/**
 * Class representing server information
 */
public class ServerInfo {
    private final String ip;
    private final int port;

    public ServerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerInfo) {
            ServerInfo other = (ServerInfo) obj;
            return ip.equals(other.ip) && port == other.port;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ip.hashCode() + 31 * port;
    }
}
