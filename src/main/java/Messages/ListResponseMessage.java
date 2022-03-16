package Messages;

import States.ServerState;
import io.netty.channel.Channel;

import java.util.ArrayList;


public class ListResponseMessage extends ClientMessage{

    private String channelId;
    private ArrayList<String> rooms;

    ListResponseMessage(String channelId,ArrayList<String> rooms){
        this.channelId=channelId;
        this.rooms=rooms;
    }

    @Override
    public void handle(Channel channel) {
        ServerState.getInstance().setTempRoomList(channelId,rooms);
        System.out.println(ServerState.getInstance().getTempRoomList(channelId));

    }
}
