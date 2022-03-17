package Messages;

import Protocols.Client;
import Services.ServerLogger;
import States.ServerState;
import org.apache.log4j.Logger;

public class VoteMessage extends CoordinationMessage{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), VoteMessage.class);
    private String id;
    private String suspectId;

    public VoteMessage(String id, String suspectId){
        this.id=id;
        this.suspectId=suspectId;

    }
    @Override
    public void handle() {
        ServerState serverState=ServerState.getInstance();
        String localServerId = serverState.getServerId();
        Client.send(this.id,new AnswerVoteMessage(localServerId,this.suspectId,serverState.getSuspect(suspectId)),true);
        logger.info(String.format("Voting %s to kick the suspected server %s", serverState.getSuspect(suspectId)== 1 ? "YES":"NO", this.suspectId));
    }
}
