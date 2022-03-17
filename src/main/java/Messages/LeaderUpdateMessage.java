package Messages;

import Core.Room;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class LeaderUpdateMessage extends CoordinationMessage{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), LeaderUpdateMessage.class);
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
        logger.info("Leader update request from server "+this.id+" received");
    }
}
