package Client;

import MessagePassing.MessagePassing;
import Server.Server;
import Server.ServerInfo;
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
import java.util.concurrent.locks.Lock;


public class ClientThread implements Runnable{
    private final Socket clientSocket;
    private String serverID;
    private Client client;
    private int isClientApproved = -1;
    private int isJoinRoomApproved = -1;

    private String approvedJoinRoomServerHostAddress;
    private String approvedJoinRoomServerPort;

    private boolean quit = false;

    final Object lock = new Object();

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
                //add client to mainhall
                Server.getInstance().getRoomList().get(mainHallID).addClient(client);
                //If I am the leader update the global list.
                if(Objects.equals(Server.getInstance().getServerID(), Leader.getInstance().getLeaderID())){
                    Leader.getInstance().addToGlobalClientAndRoomList(identity,Server.getInstance().getServerID(),mainHallID);
                }
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

    private void joinRoom(String roomid) throws IOException, InterruptedException {

        String formerRoomID = client.getRoomID();

        if(client.isRoomOwner()){
            System.out.println("WARN : Join room denied, Client" + client.getClientID() + " Owns a room");

            MessagePassing.sendClient(
                ClientMessage.roomChangeReply(
                    client.getClientID(),
                    formerRoomID,
                    formerRoomID
                ), 
                clientSocket);

        }else if(Server.getInstance().getRoomList().containsKey(roomid)){ //local room change
            
            client.setClientID(roomid);
            Server.getInstance().getRoomList().get(formerRoomID).removeClient(client.getClientID());
            Server.getInstance().getRoomList().get(roomid).addClient(client);

            System.out.println("INFO : client [" + client.getClientID() + "] joined room :" + roomid);
        

            // creating broadcast list
            HashMap<String, Client> newClientList = Server.getInstance().getRoomList().get(roomid).getClientList();
            HashMap<String, Client> oldClientList = Server.getInstance().getRoomList().get(formerRoomID).getClientList();
            HashMap<String, Client> clientList = new HashMap<>();
            clientList.putAll(oldClientList);
            clientList.putAll(newClientList);

            ArrayList<Socket> SocketList = new ArrayList<>();
            for (String each : clientList.keySet()) {
                SocketList.add(clientList.get(each).getSocket());
            }
            
            MessagePassing.sendClient(
                ClientMessage.roomChangeReply(
                    client.getClientID(),
                    formerRoomID,
                    roomid), 
                    clientSocket);

            while(!Server.getInstance().getLeaderUpdateComplete()) {
                Thread.sleep(1000);
            }
            
            // if self is leader update leader state directly
            if(Server.getInstance().getServerID() == Leader.getInstance().getLeaderID()){
                Leader.getInstance().localJoinRoomClient(client, formerRoomID);
                    
            }else {
                MessagePassing.sendToLeader(
                    ServerMessage.getJoinRoomRequest(
                        client.getClientID(), 
                        roomid, 
                        formerRoomID, 
                        Server.getInstance().getServerID(), 
                        String.valueOf(Thread.currentThread().getId()), 
                        String.valueOf(true))
                    );
            }

        } else {  // global room change

            while(!Server.getInstance().getLeaderUpdateComplete()) {
                Thread.sleep(1000);
            }

            isJoinRoomApproved = -1;

            //check if room id exist and if init route
            if(Leader.getInstance().getLeaderID() == Server.getInstance().getServerID()){
                String serverIDofTargetRoom = Leader.getInstance().getServerIdIfRoomExist(roomid);
            
                isJoinRoomApproved = serverIDofTargetRoom != null ? 1 : 0;

                if(isJoinRoomApproved == 1){

                    ServerInfo serverOfTargetRoom;

                    if(Server.getInstance().getCandidateServers().contains(serverIDofTargetRoom)){
                        serverOfTargetRoom = Server.getInstance().getCandidateServers().get(serverIDofTargetRoom);
                    }else if(Server.getInstance().getLowPriorityServers().contains(serverIDofTargetRoom)){
                        serverOfTargetRoom = Server.getInstance().getLowPriorityServers().get(serverIDofTargetRoom);
                    }else {
                        serverOfTargetRoom = Server.getInstance().getOtherServers().get(serverIDofTargetRoom);
                    }

                    approvedJoinRoomServerHostAddress = serverOfTargetRoom.getAddress();
                    approvedJoinRoomServerPort = String.valueOf(serverOfTargetRoom.getClientPort()); 
                }
                System.out.println("INFO : Received response for route request for join room (Self is Leader)");


            } else {
                MessagePassing.sendToLeader(
                    ServerMessage.getJoinRoomRequest(
                        client.getClientID(), 
                        roomid, 
                        formerRoomID, 
                        Server.getInstance().getServerID(), 
                        String.valueOf(Thread.currentThread().getId()), 
                        String.valueOf(false))
                );


                synchronized(lock){
                    while (isJoinRoomApproved == -1) {
                        System.out.println("INFO : Wait until server approve route on Join room request");
                        lock.wait(7000);
                        //wait for response
                    }
                }
                
                System.out.println("INFO : Received response for route request for join room");
            }

            if(isJoinRoomApproved == 1){
                //broadcast to former room
                Server.getInstance().removeClient(client.getClientID(), formerRoomID, Thread.currentThread().getId());
                System.out.println("INFO : client [" + client.getClientID() + "] left room :" + formerRoomID);
            
                //create broadcast list
                HashMap<String, Client> clientListOld = Server.getInstance().getRoomList().get(formerRoomID).getClientList();
                System.out.println("INFO : Send broadcast to former room in local server");

                ArrayList<Socket> SocketList = new ArrayList<>();
                for (String each : clientListOld.keySet()) {
                    SocketList.add(clientListOld.get(each).getSocket());
                }

                MessagePassing.sendClient(
                    ClientMessage.roomChangeReply(
                        client.getClientID(),
                        formerRoomID,
                        roomid)
                    , clientSocket);
    
                //server change : route
                MessagePassing.sendClient(
                    ClientMessage.routeReply(
                        roomid,
                        approvedJoinRoomServerHostAddress,
                        approvedJoinRoomServerPort)
                    , clientSocket);
    
                System.out.println("INFO : Route Message Sent to Client");
                quit = true;
            
            } else if(isJoinRoomApproved ==0) { // Room not found on system

                System.out.println("WARN : Received room ID ["+roomid + "] does not exist");

                MessagePassing.sendClient(
                    ClientMessage.roomChangeReply(
                        client.getClientID(),
                        formerRoomID, //same
                        formerRoomID) //same
                    , clientSocket);
                
                isJoinRoomApproved = -1;
            }

            


        } 

        

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
