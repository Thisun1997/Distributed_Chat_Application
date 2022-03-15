package consensus;

import MessagePassing.MessagePassing;
import Server.Server;
import Server.ServerInfo;
import Server.ServerMessage;
import org.json.simple.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.Objects;

public class ConsensusJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(!Server.getInstance().getOngoingConsensus()){
            if(Server.getInstance().getLeaderUpdateComplete()){
                Server.getInstance().setOngoingConsensus(true);
                performConsensus(jobExecutionContext);
                Server.getInstance().setOngoingConsensus(false);
            }
        }
    }

    private void performConsensus(JobExecutionContext jobExecutionContext) {
        String voteDuration = Server.getInstance().getConsensusVoteDuration().toString();
        String LeaderID = Leader.getInstance().getLeaderID();
        String selfID = Server.getInstance().getServerID();

        String suspectServerID = null;
        ArrayList<ServerInfo> serverList = new ArrayList<>();
        boolean suspectedFound = false;

        Server.getInstance().getVoteList().put("1",0);
        Server.getInstance().getVoteList().put("0",0);

        if(Objects.equals(selfID, LeaderID)){
            for(String serverID: Server.getInstance().getOtherServers().keySet()){
                if(Server.getInstance().getSuspectList().get(serverID) == 1 && !suspectedFound){
                    suspectServerID = serverID;
                    suspectedFound = true;
                }
                else if(Server.getInstance().getSuspectList().get(serverID) == 0){
                    serverList.add(Server.getInstance().getOtherServers().get(serverID));
                }
            }

            if(suspectServerID != null){
                Server.getInstance().getVoteList().put("1",1);
                MessagePassing.sendServerBroadcast(ServerMessage.voteMessage(selfID,suspectServerID), serverList);
                System.out.println("INFO : Leader "+selfID+" suspect "+suspectServerID+" as down");
                //wait for consensus vote duration period
                try {
                    Thread.sleep(Integer.parseInt(voteDuration) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(Server.getInstance().getVoteList().get("1") > Server.getInstance().getVoteList().get("0")){
                try{
                    MessagePassing.sendServerBroadcast(ServerMessage.notifyServerDownMessage(suspectServerID), serverList);
                    System.out.println("INFO : Leader "+selfID+" decided to kick "+suspectServerID);
                    Leader.getInstance().getGlobalRoomList().remove(suspectServerID);
//                    Leader.getInstance().getGlobalClientList().remove(suspectServerID);
                    Server.getInstance().getSuspectList().remove(suspectServerID);
                    Server.getInstance().getHeartBeatCountList().remove(suspectServerID);
                    Server.getInstance().getOtherServers().remove(suspectServerID);
                }catch (Exception e) {
                    System.out.println("ERROR : " + suspectServerID + "Removing is failed");
                }
            }
        }
    }

    public static void startVoteMessageHandler(JSONObject jsonObject){

        String suspectServerId = jsonObject.get("suspectServerID").toString();
        String serverID = jsonObject.get("serverID").toString();

        if (Server.getInstance().getSuspectList().containsKey(suspectServerId)) {
            String vote = null;
            if (Server.getInstance().getSuspectList().get(suspectServerId).equals(1)) {
                vote = "1";
            } else {
                vote = "0";
            }
            try {
                MessagePassing.sendServer(ServerMessage.answerVoteMessage(suspectServerId,vote,Server.getInstance().getServerID()), Server.getInstance().getOtherServers().get(serverID));
                System.out.println(String.format("INFO : Voting on suspected server: [%s] vote: %s", suspectServerId, Integer.parseInt(vote) == 1 ? "YES":"NO"));
            } catch (Exception e) {
                System.out.println("ERROR : Voting on suspected server is failed");
            }
        }

    }

    public static void answerVoteHandler(JSONObject jsonObject){

        String suspectServerID = jsonObject.get("suspectServerID").toString();
        String vote = jsonObject.get("vote").toString();
        String votedBy = jsonObject.get("votedBy").toString();

        Integer voteCount = Server.getInstance().getVoteList().get(vote);

        System.out.println(String.format("Receiving voting to kick [%s]: [%s] voted by server: [%s]", suspectServerID, vote, votedBy));

        if (voteCount == null) {
            Server.getInstance().getVoteList().put(vote, 1);
        } else {
            Server.getInstance().getVoteList().put(vote, voteCount + 1);
        }

    }

    public static void notifyServerDownMessageHandler(JSONObject jsonObject){
        String serverID = jsonObject.get("serverID").toString();

        System.out.println("INFO : Server down notification received. Removing server: " + serverID);

//        serverState.removeServer(serverId);
        Server.getInstance().getSuspectList().remove(serverID);
        Server.getInstance().getHeartBeatCountList().remove(serverID);
        Server.getInstance().getOtherServers().remove(serverID);
    }

}
