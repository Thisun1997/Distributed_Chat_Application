package Server;

import org.json.simple.JSONObject;

public class ServerMessage {

    public static JSONObject getElection(String serverID) {
        // {"option": "election", "source": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "election");
        jsonObject.put("serverID", serverID);
        return jsonObject;
    }

    public static JSONObject getAnswer(String serverID) {
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

    public static JSONObject setCoordinatorMessage(String serverID, String address, int serverPort, int clientPort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "coordinator");
        jsonObject.put("serverID", serverID);
        jsonObject.put("address", address);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("clientPort", clientPort);
        return jsonObject;
    }

    public static JSONObject iAmUpMessage(String serverID, String address, int serverPort, int clientPort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("option", "IamUp");
        jsonObject.put("serverID", serverID);
        jsonObject.put("address", address);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("clientPort", clientPort);
        return jsonObject;
    }
}
