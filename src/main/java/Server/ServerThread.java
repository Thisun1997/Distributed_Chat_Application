package Server;

import consensus.Leader;
import consensus.election.FastBullyAlgorithm;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerThread implements Runnable{

    private final ServerSocket serverSocket;
//    private LeaderStateUpdate leaderStateUpdate = new LeaderStateUpdate();

    public ServerThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    private void sendToLeaderUpdate(String serverID, JSONArray clientIDListJson, JSONArray chatRoomsListJson) {
        List<String> clientIDList = new ArrayList<>();
        List<Room> roomList = new ArrayList<>();

        for( Object clientID : clientIDListJson ) {
            clientIDList.add( clientID.toString() );
        }

        for( Object chatRoom : chatRoomsListJson ) {
            JSONObject j_room = (JSONObject)chatRoom;
            roomList.add ( new Room(j_room.get("roomid").toString(),
                    j_room.get("serverid").toString(), j_room.get("clientid").toString()) );
        }
        Leader.getInstance().handleRequest(serverID,clientIDList,roomList);
    }

    @Override
    public void run() {
        try{
            while(true){
                Socket serverSocket = this.serverSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONParser jsonParser = new JSONParser();
                String string = bufferedReader.readLine();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(string);
                if (jsonObject != null && jsonObject.get("option") != null) {
                    FastBullyAlgorithm.receiveMessage(jsonObject);
                }
                else if (jsonObject != null && jsonObject.get("type") != null) {
                    String type = (String) jsonObject.get("type");
                    if(Objects.equals(type, "leaderupdate")){
                        String serverID = (String) jsonObject.get("serverID");
                        JSONArray clientIDListJson = ( JSONArray ) jsonObject.get( "clients" );
                        JSONArray chatRoomsListJson = ( JSONArray ) jsonObject.get( "chatrooms" );
                        //System.out.println(chatRoomsList);
                        sendToLeaderUpdate(serverID, clientIDListJson, chatRoomsListJson);
                    }
                    else if(Objects.equals(type, "leaderstateupdatecomplete")){
                        String serverID = (String) jsonObject.get("serverID");
                        System.out.println("leader "+serverID+" update done..");
//                        FastBullyAlgorithm FBA = new FastBullyAlgorithm("");
//                        FBA.stopWaitingForUpdateCompleteMessage();
                        Thread.sleep(1000);
                        Server.getInstance().setLeaderUpdateComplete(true);
                    }
                }

            }
        }catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
