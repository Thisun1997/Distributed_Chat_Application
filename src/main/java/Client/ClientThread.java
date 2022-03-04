package Client;

import MessagePassing.MessagePassing;
import Server.Server;
import Server.ServerMessage;
import Server.Room;
import consensus.Leader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ClientThread implements Runnable{
    private final Socket clientSocket;
    private String serverID;
    private Client client;
    private int isClientApproved = -1;

    private boolean quit = false;

    public ClientThread( Socket clientSocket){
        this.clientSocket = clientSocket;
        this.serverID = Server.getInstance().getServerID();
    }

    public int getIsClientApproved() {
        return isClientApproved;
    }

    public void setIsClientApproved(int isClientApproved) {
        this.isClientApproved = isClientApproved;
    }

    private void newIdentity(String identity) throws InterruptedException, IOException {
//        TODO - implement adding a new client
        if((Character.toString(identity.charAt(0)).matches("[a-zA-Z]+")
                && identity.matches("[a-zA-Z0-9]+") && identity.length() >= 3 && identity.length() <= 16)){
            while(!Server.getInstance().getLeaderUpdateComplete()) {
                Thread.sleep(1000);
            }
            ///////////////JUST A TEST - MUST REMOVE/////////////////////////////////
            System.out.println(Leader.getInstance().getLeaderID());
            ConcurrentHashMap<String, List<Room>> globalRoomList = Leader.getInstance().getGlobalRoomList(); //server_id, cliient_id list
            ConcurrentHashMap<String, List<String>> globalClientList = Leader.getInstance().getGlobalClientList(); //server_id, cliient_id list

            for(String key: globalRoomList.keySet()){
                List<Room> r_list = globalRoomList.get(key);
                for(Room r:r_list){
                    System.out.println(key);
                    System.out.println(r.getRoomID());
                }
            }
            for(String key: globalClientList.keySet()){
                List<String> c_list = globalClientList.get(key);
                for(String c:c_list){
                    System.out.println(key);
                    System.out.println(c);
                }
            }

            System.out.println(Server.getInstance().getLeaderUpdateComplete());
            ////////////////////////////////////////////////////////////////////////////
            if(Objects.equals(Server.getInstance().getServerID(), Leader.getInstance().getLeaderID())){
                boolean clientIDTaken = Leader.getInstance().isClientIDTaken(identity);
                isClientApproved = clientIDTaken ? 0 : 1;
            }
            else{
                MessagePassing.sendToLeader(
                        ServerMessage.clientIdApprovalRequest(identity, Server.getInstance().getServerID(), String.valueOf(Thread.currentThread().getId())));
                synchronized (this){
                    while (isClientApproved == -1){
                        this.wait(7000);
                    }
                }
            }
            //if client is approved
            if(isClientApproved == 1){
                String mainHallID = Server.getInstance().getMainHallID(Server.getInstance().getServerID());
                this.client = new Client(identity,mainHallID,clientSocket);
                //If I am the leader update the global list.
                if(Objects.equals(Server.getInstance().getServerID(), Leader.getInstance().getLeaderID())){
                    Leader.getInstance().addToGlobalClientAndRoomList(identity,Server.getInstance().getServerID(),mainHallID);
                }
                //add client to mainhall
                Server.getInstance().getRoomList().get(mainHallID).addClient(client);
                //broadcast to all the clients in mainhall
                HashMap<String, Client> mainHallClientList =  Server.getInstance().getRoomList().get(mainHallID).getClientList();
                ArrayList<Socket> socketList = new ArrayList<>();
                for(String clientID:mainHallClientList.keySet()){
                    socketList.add(mainHallClientList.get(clientID).getSocket());
                }

                synchronized (clientSocket){
                    MessagePassing.sendClient(ClientMessage.newIdentityReply("true"), clientSocket);
                    MessagePassing.sendBroadcast(ClientMessage.roomChangeReply(identity,"",mainHallID),socketList);
                }
            }
            //if not approved notify client
            else if(isClientApproved == 0){
                synchronized (clientSocket){
                    MessagePassing.sendClient(ClientMessage.newIdentityReply("false"), clientSocket);
                }
            }
            isClientApproved = -1;
        }
        //if client id format does not match notify user
        else{
            synchronized (clientSocket){
                MessagePassing.sendClient(ClientMessage.newIdentityReply("false"), clientSocket);
            }
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
                try {
                    String string = bufferedReader.readLine();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(string);
                    String type = null;
                    if (jsonObject != null && jsonObject.get("type") != null) {
                        type = (String) jsonObject.get("type");
                    } else {
                        //                    TODO - Add output string
                    }
                    if (Objects.equals(type, "newidentity") && jsonObject.get("identity") != null) {
                        newIdentity((String) jsonObject.get("identity"));
                    } else if (Objects.equals(type, "list")) {
                        list();
                    } else if (Objects.equals(type, "who")) {
                        who();
                    } else if (Objects.equals(type, "createroom") && jsonObject.get("roomid") != null) {
                        createRoom((String) jsonObject.get("roomid"));
                    } else if (Objects.equals(type, "joinroom") && jsonObject.get("roomid") != null) {
                        joinRoom((String) jsonObject.get("roomid"));
                    } else if (Objects.equals(type, "movejoin") && jsonObject.get("former") != null && jsonObject.get("roomid") != null && jsonObject.get("identity") != null) {
                        String formerRoomID = (String) jsonObject.get("former");
                        String newRoomID = (String) jsonObject.get("roomid");
                        String clientID = (String) jsonObject.get("identity");
                        moveJoin(formerRoomID, newRoomID, clientID);
                    } else if (Objects.equals(type, "deleteroom") && jsonObject.get("roomid") != null) {
                        deleteRoom((String) jsonObject.get("roomid"));
                    } else if (Objects.equals(type, "message") && jsonObject.get("content") != null) {
                        message((String) jsonObject.get("content"));
                    } else if (Objects.equals(type, "quit")) {
                        quit();
                    }
                }
                catch (NullPointerException e){
                    break;
                }
            }
        } catch (IOException | ParseException | InterruptedException e) {
//            TODO - Add output string
//            e.printStackTrace();
            System.out.println("quit exception!");
        }
    }


}
