package Heartbeat;

import Messages.GossipMessage;
import Protocols.Client;
import States.LeaderState;
import States.ServerState;
import consensus.election.FastBullyAlgorithm;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Gossip implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ServerState serverState = ServerState.getInstance();
        LeaderState leaderState = LeaderState.getInstance();
        int aliveErrorFactor =Integer.parseInt(serverState.getAliveErrorFactor());
        String localServerId = serverState.getServerId();
        ArrayList<String> heartbeatServers= new ArrayList<String>();
        heartbeatServers.addAll(serverState.getUpServers());
        heartbeatServers.add(ServerState.getInstance().getServerId());
        for (String serverId : heartbeatServers) {
            // get current heartbeat count of a server
            int heartbeatCount = serverState.getHeartbeat(serverId);
            // first update heartbeat count vector
            if (serverId.equals(localServerId)) {
                serverState.setHeartbeat(serverId, 0); // reset my own vector always
            } else {
                // up count all others
                heartbeatCount++;
                serverState.setHeartbeat(serverId, heartbeatCount);
            }
            // FIX get the fresh updated current count again
            heartbeatCount = serverState.getHeartbeat(serverId);
                // if heart beat count is more than error factor
            if (heartbeatCount > aliveErrorFactor) {
                serverState.setSuspect(serverId,1); // 1 = true = suspected
            } else {
                serverState.setSuspect(serverId,0); // 0 = false = not-suspected
            }
        }
            if (ServerState.getInstance().getLeaderUpdateComplete() && !ServerState.getInstance().getOngoingElection()) {

                String leaderId = leaderState.getLeaderID();

                // if the leader/coordinator server is in suspect list, start the election process

                if (serverState.getSuspect(leaderId) == 1) {
                    FastBullyAlgorithm.initialize();
                }
            }
            int numServers = serverState.getUpServers().size()+1; //peers+local server
            if (numServers > 1) { // Gossip required at least 2 servers to be up

                // change concurrent hashmap to hashmap before sending
                Hashtable<String, Integer> heartbeatCounts = new Hashtable<String, Integer>();
                heartbeatCounts.putAll(serverState.getHeartbeats());
                Client.send(getRoundRobinServer(),new GossipMessage(localServerId,heartbeatCounts),true);
            }
    }

    public String getRoundRobinServer(){
        ArrayList<String> remoteServers=ServerState.getInstance().getRoundRobinServers();
        int numRemoteServers = remoteServers.size(); //peers
        int serverIndex = ThreadLocalRandom.current().nextInt(numRemoteServers);
        Collections.shuffle(remoteServers, new Random(System.nanoTime()));
        String roundRobinServer=remoteServers.get(serverIndex);
        ServerState.getInstance().setRoundRobinServers(roundRobinServer);
        return roundRobinServer;
    }
}