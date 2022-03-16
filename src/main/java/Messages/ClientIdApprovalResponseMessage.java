package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class ClientIdApprovalResponseMessage extends ClientMessage{

    private String status;
    private String channelId;

    public ClientIdApprovalResponseMessage(String channelId,boolean status){
        this.channelId=channelId;
        this.status=String.valueOf(status);
    }

    @Override
    public void handle(Channel channel) {
        int isClientApproved = Boolean.valueOf(status) ? 0 : 1;
        ServerState.getInstance().setClientIdApproved(channelId,isClientApproved);
    }
}
