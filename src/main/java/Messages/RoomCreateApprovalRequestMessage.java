package Messages;

import Protocols.CoordinationServer;
import States.LeaderState;
import io.netty.channel.Channel;

import java.awt.font.TextHitInfo;

public class RoomCreateApprovalRequestMessage extends ClientMessage{

    private String clientId;
    private String roomId;
    private String former;
    private String serverId;
    private String channelId;

    public RoomCreateApprovalRequestMessage(String clientId, String roomId, String former, String serverId, String channelId) {
        this.clientId = clientId;
        this.roomId = roomId;
        this.former = former;
        this.serverId = serverId;
        this.channelId = channelId;
    }


    @Override
    public void handle(Channel channel) {

        boolean roomIDTaken = LeaderState.getInstance().isRoomIDTaken(roomId);
        if(!roomIDTaken){
            LeaderState.getInstance().addToRoomList(clientId, serverId, roomId, former);
        }
        CoordinationServer.send(channel,new RoomIdApprovalResponseMessage(roomIDTaken,channelId));
    }
}
