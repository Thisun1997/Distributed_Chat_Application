package Messages;

import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;


import java.util.Hashtable;

public class GossipMessage extends CoordinationMessage{
    private String id;
    private Hashtable<String,Integer> heartbeats;

    public GossipMessage(String id, Hashtable<String,Integer> heartbeats ){
            this.id=id;
            this.heartbeats=heartbeats;

    }
    @Override
    public void handle() {
        ServerState serverState=ServerState.getInstance();
        LeaderState leaderState=LeaderState.getInstance();
        //update the heartbeats by taking minimum
        for (String serverId : this.heartbeats.keySet()) {
            int localHeartbeatCount = serverState.getHeartbeat(serverId);
            int remoteHeartbeatCount = this.heartbeats.get(serverId);
            if (remoteHeartbeatCount < localHeartbeatCount) {
                serverState.setHeartbeat(serverId,remoteHeartbeatCount);
            }
        }


        // If this server is a leader and, remote heart beat has more suspects than me, leader will have to
        // check this condition. Because when a subordinate server come up, it will read server.tab
        // and get populated all servers again - in which some of them might be already kicked at leader.
        // That's why leader has to check this situation and put them in suspect list, so that this will
        // get pick up in next consensus run and voting cycle.

        if (serverState.getServerId().equals(leaderState.getLeaderID())) {
            if (serverState.getHeartbeats().size() < this.heartbeats.size()) {
                for (String serverId : this.heartbeats.keySet()) {
                    if (serverState.getHeartbeat(serverId)!=0) {
                        serverState.getSuspects().put(serverId,1);
                    }
                }
            }
        }
    }

}
