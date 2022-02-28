package Client;

import Server.Room;
import Server.Server;
import consensus.Leader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serial;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static Client.ClientMessage.*;


public class ClientThread implements Runnable{
    private Socket clientSocket;
    private String serverID;
    private Client client;


    private boolean quit = false;

    public ClientThread( Socket clientSocket){
        this.clientSocket = clientSocket;
        this.serverID = "";
    }

    private void newIdentity(String identity) {
//        TODO - implement adding a new client
        if(Server.getInstance().getLeaderUpdateComplete()){
            String approve = "true";
//        this.client = new Client(identity, ,this.clientSocket);
            JSONObject msgClient = newIdentityReply(approve);
//        JSONObject msgBroadcast = roomChangeReply(identity,"", );
            System.out.println("identity");
            System.out.println(Leader.getInstance().getLeaderID());
            ConcurrentHashMap<String, List<Room>> globalRoomList = Leader.getInstance().getGlobalRoomList(); //server_id, cliient_id list
            for(String key: globalRoomList.keySet()){
                List<Room> r_list = globalRoomList.get(key);
                for(Room r:r_list){
                    System.out.append(r.getRoomID());
                }
            }
            System.out.println(Server.getInstance().getLeaderUpdateComplete());
        }
    }

    private void list() {
//        TODO - implement listing the chatrooms in the connected server
    }

    private void who() {
//        TODO - implement listing the clients in the chatroom
    }

    private void createRoom(String roomid) {
//        TODO - implement creating a new chatroom
    }

    private void moveJoin(String roomid, String newRoomID, String clientID) {
//        TODO - implement joining a room in another server
    }

    private void deleteRoom(String roomid) {
//        TODO - implement delete room
    }

    private void message(String content) {
//        TODO - implement broadcasting the message to clients in the chatroom
    }

    private void quit() {
//        TODO - Quiting the server
    }

    private void joinRoom(String roomid) {
//        TODO - Quiting the server
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            JSONParser jsonParser = new JSONParser();
            while(!quit){
                String string = bufferedReader.readLine();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(string);
                String type = null;
                if (jsonObject != null && jsonObject.get("type") != null) {
                    type = (String) jsonObject.get("type");
                }
                else{
//                    TODO - Add output string
                }
                if(Objects.equals(type, "newidentity") && jsonObject.get("identity") != null){
                    newIdentity((String) jsonObject.get("identity"));
                }
                else if(Objects.equals(type, "list")){
                    list();
                }
                else if(Objects.equals(type, "who")){
                    who();
                }
                else if(Objects.equals(type, "createroom") && jsonObject.get("roomid") != null){
                    createRoom((String) jsonObject.get("roomid"));
                }
                else if(Objects.equals(type, "joinroom") && jsonObject.get("roomid") != null){
                    joinRoom((String) jsonObject.get("roomid"));
                }
                else if(Objects.equals(type, "movejoin") && jsonObject.get("former") != null && jsonObject.get("roomid") != null && jsonObject.get("identity") != null){
                    String formerRoomID = (String) jsonObject.get("former");
                    String newRoomID = (String) jsonObject.get("roomid");
                    String clientID = (String) jsonObject.get("identity");
                    moveJoin(formerRoomID, newRoomID, clientID);
                }
                else if(Objects.equals(type, "deleteroom") && jsonObject.get("roomid") != null){
                    deleteRoom((String) jsonObject.get("roomid"));
                }
                else if(Objects.equals(type, "message") && jsonObject.get("content") != null){
                    message((String) jsonObject.get("content"));
                }
                else if(Objects.equals(type, "quit")){
                    quit();
                }
            }
        } catch (IOException | ParseException e) {
//            TODO - Add output string
            e.printStackTrace();
        }
    }


}
