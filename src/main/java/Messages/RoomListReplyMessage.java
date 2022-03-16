package Messages;

import io.netty.channel.Channel;

import java.util.ArrayList;

public class RoomListReplyMessage extends ClientMessage{

    ArrayList<String> rooms;


    public RoomListReplyMessage(ArrayList<String> roomIds)  {
        this.rooms=roomIds;

    }

    @Override
    public void handle(Channel channel) {

    }
}
