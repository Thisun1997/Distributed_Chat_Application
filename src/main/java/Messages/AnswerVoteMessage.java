package Messages;

import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class AnswerVoteMessage extends CoordinationMessage{
    private String id;
    private String suspectId;
    private int vote;

    AnswerVoteMessage(String id,String suspectId,int vote){
            this.id=id;
            this.suspectId=suspectId;
            this.vote=vote;
    }
    @Override
    public void handle() {
        ServerState serverState=ServerState.getInstance();
        serverState.setVotes(serverState.getVotes()+this.vote);
    }
}
