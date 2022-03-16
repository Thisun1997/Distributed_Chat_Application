package Messages;

import Protocols.CoordinationServer;
import States.LeaderState;
import io.netty.channel.Channel;

public class ClientIdApprovalRequestMessage extends ClientMessage{
    private String identity;
    private String serverId;
    private String channelId;

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
    }
}
