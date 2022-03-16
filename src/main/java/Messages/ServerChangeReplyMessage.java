package Messages;

import io.netty.channel.Channel;

public class ServerChangeReplyMessage extends ClientMessage{
    private String approved;
    private String serverid;

    public ServerChangeReplyMessage(boolean approved, String serverId) {
        this.approved = String.valueOf(approved);
        this.serverid = serverId ;
    }

    @Override
    public void handle(Channel channel) {

    }
}
