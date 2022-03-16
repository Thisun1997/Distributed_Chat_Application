package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class RoomIdApprovalResponseMessage extends ClientMessage{

    String status;
    String channelId;

    public RoomIdApprovalResponseMessage(boolean status, String channelId) {
        this.status = String.valueOf(status);
        this.channelId = channelId;
    }

    @Override
    public void handle(Channel channel) {
        int IsRoomApproved = Boolean.parseBoolean(status) ? 0 : 1;

        ServerState.getInstance().setIsRoomCreationApproved(channelId,IsRoomApproved);

    }
}
