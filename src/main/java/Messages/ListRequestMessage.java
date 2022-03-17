package Messages;

import Protocols.CoordinationServer;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class ListRequestMessage extends ClientMessage{

    private String channelId;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), ListRequestMessage.class);

    public ListRequestMessage(String channelId){
        this.channelId=channelId;
    }
    @Override
    public void handle(Channel channel) {
        CoordinationServer.send(channel, new ListResponseMessage(channelId,LeaderState.getInstance().getRoomIDList()));
        logger.info("Room list fetched");
    }
}
