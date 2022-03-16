package Messages;

import Protocols.CoordinationServer;
import States.LeaderState;
import io.netty.channel.Channel;

public class ListRequestMessage extends ClientMessage{

    private String channelId;

    public ListRequestMessage(String channelId){
        this.channelId=channelId;
    }
    @Override
    public void handle(Channel channel) {
        CoordinationServer.send(channel, new ListResponseMessage(channelId,LeaderState.getInstance().getRoomIDList()));
    }
}
