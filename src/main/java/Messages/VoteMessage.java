package Messages;

import Protocols.Client;
import States.ServerState;

public class VoteMessage extends CoordinationMessage{

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
    }
}
