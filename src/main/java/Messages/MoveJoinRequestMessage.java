package Messages;

import States.LeaderState;
import io.netty.channel.Channel;

public class MoveJoinRequestMessage extends ClientMessage {

    private String serverId;
    private String roomId;
    private String formerRoomId;
    private String clientId;
    private String channelId;

    public MoveJoinRequestMessage(String serverId, String roomId, String formerRoomId, String clientId, String channelId) {
        this.serverId = serverId;
        this.roomId = roomId;
        this.formerRoomId = formerRoomId;
        this.clientId = clientId;
        this.channelId = channelId;
    }

    @Override
    public void handle(Channel channel) {

        LeaderState.getInstance().addToGlobalClientAndRoomList(clientId, serverId, roomId);

        System.out.println("INFO : Moved Client [" + clientId + "] to server s" + serverId
                + " and room [" + roomId + "] is updated as current room");
    }
}
