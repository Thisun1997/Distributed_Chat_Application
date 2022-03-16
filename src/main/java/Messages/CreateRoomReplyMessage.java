package Messages;

import io.netty.channel.Channel;

public class CreateRoomReplyMessage extends ClientMessage{

    private String roomid;
    private String approved;

    public CreateRoomReplyMessage(String roomId, String approved) {
        this.roomid = roomId;
        this.approved = approved;
    }

    @Override
    public void handle(Channel channel) {

    }
}
