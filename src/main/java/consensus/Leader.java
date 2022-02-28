package consensus;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import MessagePassing.MessagePassing;
import Server.Room;
import Server.Server;
import Server.ServerInfo;
import Server.ServerMessage;

public class Leader {
    private String leaderID;

    private final ConcurrentHashMap<String, List<String>> globalClientList = new ConcurrentHashMap<>(); //server_id, cliient_id list
    private final ConcurrentHashMap<String, List<Room>> globalRoomList = new ConcurrentHashMap<>(); //server_id, room list
    private static Leader leaderInstance;

    private Leader(){

    }

    public static synchronized Leader getInstance(){
        if (leaderInstance == null){
            leaderInstance = new Leader();
        }
        return leaderInstance;
    }

    public String getLeaderID() {
        return leaderID;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    public ConcurrentHashMap<String, List<String>> getGlobalClientList() {
        return globalClientList;
    }

    public ConcurrentHashMap<String, List<Room>> getGlobalRoomList() {
        return globalRoomList;
    }

    public void updateLeader(String serverID, List<String> clientIDList, List<Room> roomList) {
        synchronized (Leader.getInstance()){
            globalRoomList.put(serverID,roomList);
            globalClientList.put(serverID,clientIDList);

            if(!serverID.equals(Server.getInstance().getServerID())){
                ServerInfo destServer = Server.getInstance().getOtherServers().get(serverID);
                try {
                    MessagePassing.sendServer(
                            ServerMessage.getLeaderStateUpdateComplete(String.valueOf(Server.getInstance().getServerID())),
                            destServer
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleRequest(String serverID, List<String> clientIDList, List<Room> roomList){
        updateLeader(serverID, clientIDList, roomList);
    }

    public void reset() {
        synchronized (Leader.getInstance()){
            globalRoomList.clear();
            globalClientList.clear();
        }
    }
}
