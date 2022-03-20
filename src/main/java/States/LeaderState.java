package States;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import Core.Member;
import Core.Room;
import Messages.LeaderStateUpdateCompleteMessage;
import Protocols.Client;
import io.netty.channel.Channel;



public class LeaderState {
    private String leaderID;

    private final ConcurrentHashMap<String, List<String>> globalClientList = new ConcurrentHashMap<>(); //server_id, cliient_id list
    private final ConcurrentHashMap<String, List<Room>> globalRoomList = new ConcurrentHashMap<>(); //server_id, room list
    private static LeaderState leaderInstance;

    private LeaderState(){

    }

    public static synchronized LeaderState getInstance(){
        if (leaderInstance == null){
            leaderInstance = new LeaderState();
        }
        return leaderInstance;
    }

    public synchronized String getLeaderID() {
        return leaderID;
    }

    public synchronized void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    public synchronized ConcurrentHashMap<String, List<String>> getGlobalClientList() {
        return globalClientList;
    }

    public synchronized ConcurrentHashMap<String, List<Room>> getGlobalRoomList() {
        return globalRoomList;
    }

    public void updateLeader(String serverID, List<String> clientIDList, List<Room> roomList) {
        synchronized (LeaderState.getInstance()){
            globalRoomList.put(serverID,roomList);
            globalClientList.put(serverID,clientIDList);

            if(!serverID.equals(ServerState.getInstance().getServerId())){
                try {
                    Client.send(serverID,new LeaderStateUpdateCompleteMessage(ServerState.getInstance().getServerId()),true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void reset() {
        synchronized (LeaderState.getInstance()){
            globalRoomList.clear();
            globalClientList.clear();
        }
    }

    public synchronized boolean isClientIDTaken(String identity){
        for(String serverID: globalClientList.keySet()){
            if(globalClientList.get(serverID).contains(identity)){
                return true;
            }
        }
        return false;
    }

    public synchronized void addToGlobalClientAndRoomList(String memberId, String serverID, String roomID){
        globalClientList.get(serverID).add(memberId);
        for(Room room: globalRoomList.get(serverID)){
            if(room.getId().equals(roomID)){
                room.setMember(memberId);
                break;
            }
        }
    }


    public synchronized String getServerIdIfRoomExist(String roomId){
        for(String serverId: globalRoomList.keySet()){
            List<Room> tempRoomList = globalRoomList.get(serverId);
//            System.out.println(tempRoomList);
            for(Room room: tempRoomList){
                if (Objects.equals(room.getId(), roomId)){
                    return serverId;
                }
            }
        }
        return null;
    }

    public synchronized void InServerJoinRoomClient(String clientID, String serverID, String formerRoomID, String roomID) {
        removeFromGlobalClientAndRoomList(clientID, serverID, formerRoomID);
        addToGlobalClientAndRoomList(clientID, serverID, roomID);
    }

    public synchronized ArrayList<String> getRoomIDList() {
        ArrayList<String> roomIDList = new ArrayList<>();
        for(String serverID: globalRoomList.keySet()){
            for(Room room: globalRoomList.get(serverID)){
                roomIDList.add(room.getId());
            }
        }
        return roomIDList;
    }

    public synchronized void removeFromGlobalClientAndRoomList(String clientID, String serverID, String roomID){
        globalClientList.get(serverID).remove(clientID);
        for(Room room: globalRoomList.get(serverID)){
            if(room.getId().equals(roomID)){
                room.removeMember(clientID);
                break;
            }
        }

    }


    public boolean isRoomIDTaken(String roomID) {
        return getRoomIDList().contains(roomID);
    }

    public synchronized void addToRoomList(String clientID, String serverID, String roomID, String former) {
        Room newRoom = new Room(roomID,clientID);
        globalRoomList.get(serverID).add(newRoom);
        for(Room room: globalRoomList.get(serverID)){
            if(room.getId().equals(former)){
                room.removeMember(clientID);
            }
            else if(Objects.equals(room.getId(), roomID)){
                room.setMember(clientID);
            }
        }

    }

    public synchronized void removeRoom(String serverID, String roomID, String mainHallRoomID, String ownerID) {
        List<Room> rooms = globalRoomList.get(serverID);
        ArrayList<String> formerClientList = null;
        for(Room room:rooms) {
            if (room.getId().equals(roomID)) {
                formerClientList = room.getMembers();
                break;
            }
        }
        globalRoomList.get(serverID).removeIf(room ->room.getId().equals(roomID));
        for(Room room:rooms) {
            if (room.getId().equals(mainHallRoomID)) {
                room.getMembers().addAll(formerClientList);
            }
        }
    }

}
