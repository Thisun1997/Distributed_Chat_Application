package Heartbeat;

import Messages.KickMessage;
import Messages.VoteMessage;
import Protocols.Client;
import States.LeaderState;
import States.ServerState;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.util.concurrent.TimeUnit;

public class Consensus  implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ServerState serverState=ServerState.getInstance();
        LeaderState leaderState=LeaderState.getInstance();
        if(!serverState.getOngoingConsensus()) {
            serverState.setOngoingConsensus(true);
            // This is a leader based Consensus.
            // If no leader elected at the moment then no consensus task to perform.
            if (serverState.getLeaderUpdateComplete()) {
                Long consensusVoteDuration = serverState.getConsensusVoteDuration();

                String suspectServerId = null;

                // initialise vote set
                serverState.initVotes();

                String leaderServerId = leaderState.getLeaderID();
                String localServerId = serverState.getServerId();

                // if I am leader, and suspect someone, I want to start voting to KICK him!
                if (leaderServerId.equals(localServerId)) {
                    // find the next suspect to vote and break the loop
                    for (String serverId : serverState.getSuspects().keySet()) {
                        if (serverState.getSuspect(serverId) == 1) {
                            suspectServerId = serverId;
                            break;
                        }
                    }
                    // got a suspect
                    if (suspectServerId != null) {

                        serverState.setVotes(serverState.getVotes() + 1);
                        ; // I suspect it already, so I vote yes.
                        Client.broadcast(new VoteMessage(localServerId, suspectServerId), serverState.getUpServers());

                        try {
                            TimeUnit.MILLISECONDS.sleep(consensusVoteDuration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // remove server or do nothing

                        if (serverState.getVotes() > ServerState.getInstance().getUpServers().size()/2) {

                            Client.broadcast(new KickMessage(suspectServerId), serverState.getUpServers());

                            //have to add some other logics according to the needs.
                            serverState.downServer(suspectServerId);
                            serverState.removeSuspect(suspectServerId);
                            serverState.removeHeartbeat(suspectServerId);
                            LeaderState.getInstance().getGlobalRoomList().remove(suspectServerId);
                            LeaderState.getInstance().getGlobalClientList().remove(suspectServerId);
                            System.out.println("Server "+suspectServerId+" removed from the distributed server");
                        }
                    }
                }
            }
            serverState.setOngoingConsensus(false);
        }
            else {
                System.out.println("[SKIP] There seems to be on going consensus at the moment, skip.");
            }
        }

}