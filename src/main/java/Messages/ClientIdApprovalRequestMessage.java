package Messages;

import Protocols.CoordinationServer;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class ClientIdApprovalRequestMessage extends ClientMessage{
    private String identity;
    private String serverId;
    private String channelId;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), ClientIdApprovalRequestMessage.class);

    public ClientIdApprovalRequestMessage(String identity, String serverId,String channelId){
        this.identity=identity;
        this.serverId=serverId;
        this.channelId=channelId;
    }

    @Override
    public void handle(Channel channel) {
        boolean clientIDTaken = LeaderState.getInstance().isClientIDTaken(identity);
        if(!clientIDTaken){
            LeaderState.getInstance().addToGlobalClientAndRoomList(identity, serverId,"MainHall-"+serverId);

        }
        CoordinationServer.send(channel,new ClientIdApprovalResponseMessage(channelId,clientIDTaken));
        logger.info("Client "+identity+ (!clientIDTaken ? " " : " not ") + "approved by the leader "+ ServerState.getInstance().getServerId());
    }
}
