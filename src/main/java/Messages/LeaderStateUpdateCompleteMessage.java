package Messages;

import Services.ServerLogger;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class LeaderStateUpdateCompleteMessage extends CoordinationMessage{
    private String id;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), LeaderStateUpdateCompleteMessage.class);

    public LeaderStateUpdateCompleteMessage(String id){
        this.id=id;
    }

    @Override
    public void handle() {
        String serverID = this.id;
        ServerState.getInstance().setLeaderUpdateComplete(true);
        logger.info("Leader server "+serverID+" update completed");
    }
}
