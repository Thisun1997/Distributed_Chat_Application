package Messages;

import States.LeaderState;
import io.netty.channel.Channel;

public class DeleteRoomRequestMessage extends ClientMessage{

    private String serverId;
    private String ownerId;
    private String roomId;
    private String mainHallId;

    public DeleteRoomRequestMessage(String serverId,String ownerId,String roomId,String mainHallId){
                this.serverId=serverId;
                this.ownerId=ownerId;
                this.roomId=roomId;
                this.mainHallId=mainHallId;
    }
    @Override
    public void handle(Channel channel) {
        // leader removes client from global room list
        LeaderState.getInstance().removeRoom(serverId, roomId, mainHallId, ownerId);
    }
}
