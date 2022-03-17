package Messages;

import Services.ServerLogger;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class KickMessage  extends CoordinationMessage{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), KickMessage.class);
    private String id;
    public KickMessage(String id){
        this.id=id;
    }
    @Override
    public void handle() {
        ServerState serverState=ServerState.getInstance();
        serverState.removeSuspect(id);
        serverState.removeHeartbeat(id);
        serverState.downServer(id);
        logger.info("Server down notification received. Removing server " + this.id);
    }
}
