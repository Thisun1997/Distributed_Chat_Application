package Server;

import consensus.election.FastBullyAlgorithm;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerThread implements Runnable{

    private final ServerSocket serverSocket;
//    private LeaderStateUpdate leaderStateUpdate = new LeaderStateUpdate();

    public ServerThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
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
                String option = null;
                if (jsonObject != null && jsonObject.get("option") != null) {
                    FastBullyAlgorithm.receiveMessage(jsonObject);
                }

            }
        }catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
