package Messages;

import io.netty.channel.Channel;

public class RouteReplyMessage extends ClientMessage{

    private String roomid;
    private String host;
    private String port;

    public RouteReplyMessage(String roomId,String host,String port){
        this.roomid=roomId;
        this.host=host;
        this.port=port;
    }
    @Override
    public void handle(Channel channel) {

    }
}
