package Client;

import java.net.Socket;

public class Client {
    private String clientID;
    private String roomID;
    private Socket socket;

    public Client(String clientID, String roomID, Socket socket){
        this.clientID = clientID;
        this.roomID = roomID;
        this.socket = socket;
    }

    public String getClientID() {
        return clientID;
    }

    public String getRoomID() {
        return roomID;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isRoomOwner() {
        return false;
    }
}
