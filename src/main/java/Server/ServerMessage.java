package Server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class ServerMessage {

    public static JSONObject electionMessage(String serverID) {
        // {"option": "election", "source": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "election");
        jsonObject.put("serverID", serverID);
        return jsonObject;
    }

    public static JSONObject answerMessage(String serverID) {
        // {"option": "ok", "sender": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "answer");
        jsonObject.put("serverID", serverID);
        return jsonObject;
    }

    public static JSONObject nominationMessage() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "nomination");
        return jsonObject;
    }

    public static JSONObject setCoordinatorMessage(String serverID, String address, Integer serverPort, Integer clientPort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "coordinator");
        jsonObject.put("serverID", serverID);
        jsonObject.put("address", address);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("clientPort", clientPort);
        return jsonObject;
    }

    public static JSONObject iAmUpMessage(String serverID, String address, Integer serverPort, Integer clientPort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "IamUp");
        jsonObject.put("serverID", serverID);
        jsonObject.put("address", address);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("clientPort", clientPort);
        return jsonObject;
    }

    public static JSONObject viewMessage(String serverID, String address, Integer serverPort, Integer clientPort) {
        // {"type":"viewelection", "currentcoordinatorid":"1", "currentcoordinatoraddress":"localhost",
        //      "currentcoordinatorport":"4444", "currentcoordinatormanagementport":"5555"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "view");
        jsonObject.put("serverID", serverID);
        jsonObject.put("address", address);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("clientPort", clientPort);
        return jsonObject;
    }

    public static JSONObject leaderUpdate(String serverID, List<String> clientIDList, List<Room> roomList) {
        JSONArray clients = new JSONArray();
        clients.addAll( clientIDList );

        JSONArray chatRooms = new JSONArray();
        for( Room room : roomList ) {
            // {"clientid" : "Adel", "roomid" : "jokes", "serverid" : "s1"}
            JSONObject chatRoom = new JSONObject();
            chatRoom.put( "clientID", room.getRoomID());
            chatRoom.put( "roomID", room.getRoomID());
            chatRoom.put( "serverID", room.getServerID() );
            chatRooms.add( chatRoom );
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "leaderupdate");
        jsonObject.put("serverID",serverID);
        jsonObject.put("clients", clients);
        jsonObject.put("chatrooms", chatRooms);
        return jsonObject;
    }

    public static JSONObject leaderStateUpdateComplete(String serverID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "leaderstateupdatecomplete");
        jsonObject.put("serverID", serverID);
        return jsonObject;
    }

    public static JSONObject clientIdApprovalRequest(String identity, String serverID, String threadID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "clientidapprovalrequest");
        jsonObject.put("clientID", identity);
        jsonObject.put("serverID", serverID);
        jsonObject.put("threadID", threadID);
        return jsonObject;
    }

    public static JSONObject clientIdApprovalReply(String reply, String threadID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "clientidapprovalreply");
        jsonObject.put("reply", reply);
        jsonObject.put("threadID", threadID);
        return jsonObject;
    }

    public static JSONObject listRequest(String clientID, String threadID, String serverID){
        // {"type" : "listrequest", "clientid" : "Adel", "sender" : 1, "threadid" : 12 }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "listrequest");
        jsonObject.put("serverID", serverID);
        jsonObject.put("clientID", clientID);
        jsonObject.put("threadID", threadID);
        return jsonObject;
    }

    public static JSONObject listResponse(List<String> roomIDList, String threadID) {
        // {"type" : "listresponse", "rooms" : ["room-1","MainHall-s1","MainHall-s2"], "threadid" : 12 }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "listresponse");
        jsonObject.put("threadID", threadID);
        jsonObject.put("rooms", roomIDList);
        return jsonObject;
    }

    //quit message
    public static JSONObject quitMessage(String clientID, String formerRoomID, String serverID) {
        // {"type" : "quit", "clientid" : "Adel"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "quit");
        jsonObject.put("clientID", clientID);
        jsonObject.put("former", formerRoomID);
        jsonObject.put("serverID", serverID);
        return jsonObject;
    }
}
