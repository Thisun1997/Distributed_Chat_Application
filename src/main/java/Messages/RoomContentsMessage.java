package Messages;

import io.netty.channel.Channel;

import java.util.ArrayList;

public class RoomContentsMessage extends ClientMessage {
    private String roomid;
    private ArrayList<String> identities;
    private String owner;

    RoomContentsMessage(String room,ArrayList<String > members,String owner){
            this.owner=owner;
            this.identities=members;
            this.roomid=room;
    }

    @Override
    public void handle(Channel channel) {

    }
}
