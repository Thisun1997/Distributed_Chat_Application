package Messages;

import Core.Room;
import States.LeaderState;
import java.util.ArrayList;

public class LeaderUpdateMessage extends CoordinationMessage{
    private String id;
    private ArrayList<String> clients;
    private ArrayList<Room> chatRooms;
    public LeaderUpdateMessage(String id,ArrayList<String> clients,ArrayList<Room> chatRooms){
        this.id=id;
        this.clients=clients;
        this.chatRooms=chatRooms;
    }
    @Override
    public void handle() {
        LeaderState.getInstance().updateLeader(this.id,this.clients,this.chatRooms);
    }
}
