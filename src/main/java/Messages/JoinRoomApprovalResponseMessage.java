package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class JoinRoomApprovalResponseMessage extends ClientMessage{

    String serverId;
    String approved;
    String channelId;

    public JoinRoomApprovalResponseMessage(String serverId, boolean approved, String channelId) {
        this.serverId = serverId;
        this.approved = String.valueOf(approved);
        this.channelId = channelId;
    }

    @Override
    public void handle(Channel channel) {
//        System.out.println(serverId+" "+approved+channelId);
        ServerState.getInstance().setApprovedJoinRoomServer(channelId,serverId);
        int isJoinRoomApproved=Boolean.valueOf(approved)? 1 : 0;
//        System.out.println(approved+"===="+isJoinRoomApproved);
        ServerState.getInstance().setIsJoinRoomApproved(channelId,isJoinRoomApproved);

    }
}
