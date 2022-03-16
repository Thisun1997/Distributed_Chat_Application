package Messages;

import io.netty.channel.Channel;

public class RoomChangeReplyMessage extends ClientMessage{

    private String identity;
    private String former;
    private String roomid;

    public RoomChangeReplyMessage(String identity,String former,String roomId){
        this.identity=identity;
        this.former=former;
        this.roomid=roomId;
    }

    @Override
    public void handle(Channel channel) {

    }
}
