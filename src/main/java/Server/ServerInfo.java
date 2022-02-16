package Server;

public class ServerInfo {
    private String serverID;
    private String address;
    private int serverPort;
    private int clientPort;

    public ServerInfo(String serverID, String address, int serverPort, int clientPort) {
        this.serverID = serverID;
        this.address = address;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
}
