package Messages;

import Services.ServerLogger;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

public class AnswerVoteMessage extends CoordinationMessage{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), AnswerVoteMessage.class );
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
        logger.info(String.format("Vote of server %s to kick %s : %s received", this.id, this.suspectId, this.vote == 1 ? "YES":"NO"));
    }
}
