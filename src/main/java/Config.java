import States.ServerState;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Config {

    public static void setup() {
//  config properties are static data:include in built image
//  server info are dynamic data:include in mount volume config

        InputStream inputStream=null;
        Scanner sc = null;
        try {
            ServerState serverState = ServerState.getInstance();
            String localServerId=System.getenv("localServerId").replace("s", "");
            serverState.setServerId(localServerId);
            File configFile = new File("src/main/resources/config.properties");
            inputStream = new FileInputStream(configFile);
            Properties props = new Properties();
            props.load(inputStream);
            serverState.setElectionAnswerTimeout(Long.parseLong(props.getProperty("electionAnswerTimeout")));
            serverState.setElectionCoordinatorTimeout(Long.parseLong(props.getProperty("electionCoordinatorTimeout")));
            serverState.setElectionNominationTimeout(Long.parseLong(props.getProperty("electionNominationTimeout")));
            serverState.setConsensusVoteDuration(Long.parseLong(props.getProperty("consensusVoteDuration")));
            File file = new File("src/main/resources/serverInfo.txt");
            sc = new Scanner(file);
            String line=null;
            int serverCount=0;
            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                serverCount++;
                String[] serverInfo=line.split("\\s+");
                String serverId=serverInfo[0].trim().replace("s", "");
                String serverAddress=serverInfo[1].trim();
                String clientPort=serverInfo[2].trim();
                String coordinationPort=serverInfo[3].trim();
                serverState.setServer(serverId,serverAddress,coordinationPort,clientPort);
                if(localServerId.equals(serverId)){
                    serverState.setServerAddress(serverAddress);
                    serverState.setCoordinationPort(coordinationPort);
                    serverState.setClientPort(clientPort);
                }
            }
            serverState.setAliveErrorFactor(serverCount+1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
                try {
                    if (inputStream!=null) inputStream.close();
                    if (sc != null) sc.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
        }
    }

}
