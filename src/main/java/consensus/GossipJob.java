package consensus;

import MessagePassing.MessagePassing;
import Server.Server;
import Server.ServerInfo;
import Server.ServerMessage;
import consensus.election.FastBullyAlgorithm;
import org.json.simple.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GossipJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String aliveFactor = Server.getInstance().getAliveFactor();
        for(String serverID : Server.getInstance().getOtherServers().keySet()){

            String selfID = Server.getInstance().getServerID();
            Integer count = Server.getInstance().getHeartBeatCountList().get(serverID);

            Server.getInstance().getHeartBeatCountList().put(selfID,0);
            if(count == null){
                Server.getInstance().getHeartBeatCountList().put(serverID,1);
            }else{
                Server.getInstance().getHeartBeatCountList().put(serverID,count+1);
            }

            count = Server.getInstance().getHeartBeatCountList().get(serverID);

            if(count != null){
                if(count > Integer.parseInt(aliveFactor)){
                    Server.getInstance().getSuspectList().put(serverID,1);
                }else{
                    Server.getInstance().getSuspectList().put(serverID,0);
                }
            }
        }

        if(Server.getInstance().getLeaderUpdateComplete() && !Objects.equals(Server.getInstance().getServerID(), Leader.getInstance().getLeaderID())){
            if(Server.getInstance().getSuspectList().get(Leader.getInstance().getLeaderID()) == 1){
                if(!Server.getInstance().getOngoingElection()){
                    System.out.println("Leader "+Leader.getInstance().getLeaderID()+" is down");
                    FastBullyAlgorithm.initialize();
                }
            }
        }

        int numOfServers = Server.getInstance().getOtherServers().size();

        if (numOfServers >= 1) { // Gossip required at least 2 servers to be up

            // after updating the heartbeatCountList, randomly select a server and send
            Integer selectedServerID = ThreadLocalRandom.current().nextInt(numOfServers);


            ArrayList<String> remoteServers = new ArrayList<>();
            remoteServers.addAll(Server.getInstance().getOtherServers().keySet());
            Collections.shuffle(remoteServers, new Random(System.nanoTime())); // another way of randomize the list
            ServerInfo selectedServerInfo = Server.getInstance().getOtherServers().get(remoteServers.get(selectedServerID));
            // change concurrent hashmap to hashmap before sending
            HashMap<String, Integer> heartbeatCountList = new HashMap<>(Server.getInstance().getHeartBeatCountList());
            System.out.println(heartbeatCountList);
            try {
                MessagePassing.sendServer(ServerMessage.sendGossip(Server.getInstance().getServerID(), heartbeatCountList),selectedServerInfo);
                System.out.println("gossip sent to "+remoteServers.get(selectedServerID));
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }


    }

    public static void gossipHandler(JSONObject jsonObject) {
        HashMap<String, Long> gossipFromOthers = (HashMap<String, Long>) jsonObject.get("heartbeatCountList");
        String fromServerID = (String) jsonObject.get("serverID");
        //update the heartbeatcountlist by taking minimum
        System.out.println("gossip from "+fromServerID+" received");
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = Server.getInstance().getHeartBeatCountList().get(serverId);
            Integer remoteHeartbeatCount = gossipFromOthers.get(serverId).intValue();
            if (localHeartbeatCount != null && remoteHeartbeatCount < localHeartbeatCount) {
                Server.getInstance().getHeartBeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        // FIX
        // If this server is a leader and, remote heart beat has more suspects than me, leader will have to
        // check this condition. Because when a subordinate server come up, it will read server.tab
        // and get populated all servers again - in which some of them might be already kicked at leader.
        // That's why leader has to check this situation and put them in suspect list, so that this will
        // get pick up in next consensus run and voting cycle.
        // TODO Another way to fix this issue is, to change ServerState.heartbeatCountList data structure
        if (Server.getInstance().getLeaderUpdateComplete() && Objects.equals(Server.getInstance().getServerID(), Leader.getInstance().getLeaderID())) {
            if (Server.getInstance().getHeartBeatCountList().size() < gossipFromOthers.size()) {
                for (String serverId : gossipFromOthers.keySet()) {
                    if (!Server.getInstance().getHeartBeatCountList().containsKey(serverId)) {
                        Server.getInstance().getSuspectList().put(serverId, 1);
                    }
                }
            }
        }


    }

}
