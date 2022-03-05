package consensus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Client.Client;
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

    public synchronized ConcurrentHashMap<String, List<String>> getGlobalClientList() {
        return globalClientList;
    }

    public synchronized ConcurrentHashMap<String, List<Room>> getGlobalRoomList() {
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
                            ServerMessage.leaderStateUpdateComplete(String.valueOf(Server.getInstance().getServerID())),
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

    public boolean isClientIDTaken(String identity){
        for(String clientID: globalClientList.keySet()){
            if(globalClientList.get(clientID).contains(identity)){
                return true;
            }
        }
        return false;
    }

    public synchronized void addToGlobalClientAndRoomList(String clientID, String serverID, String roomID){
        globalClientList.get(serverID).add(clientID);
        for(Room room: globalRoomList.get(serverID)){
            if(room.getRoomID() == roomID){
                room.addClient(new Client(clientID, roomID, null));
            }
        }
    }

    // --------newly added
    public String getServerIdIfRoomExist(String roomId){
        for(String serverId: globalRoomList.keySet()){
            List<Room> tempRoomList = globalRoomList.get(serverId);
            if(tempRoomList.contains(roomId)){
                return serverId;
            }
        }
        return null;
    }

    public void localJoinRoomClient(Client clinet, String formerRoomID) { // ----need to check----
        this.globalClientList.get(this.getLeaderID()).remove(clinet.getClientID());
        addToGlobalClientAndRoomList(clinet.getClientID(),this.getLeaderID(),formerRoomID);
    }
}
