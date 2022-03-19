package Messages;

import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class MoveJoinRequestMessage extends ClientMessage {

    private String serverId;
    private String roomId;
    private String formerRoomId;
    private String clientId;
    private String channelId;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), MoveJoinRequestMessage.class);

    public MoveJoinRequestMessage(String serverId, String roomId, String formerRoomId, String clientId, String channelId) {
        this.serverId = serverId;
        this.roomId = roomId;
        this.formerRoomId = formerRoomId;
        this.clientId = clientId;
        this.channelId = channelId;
    }

    @Override
    public void handle(Channel channel) {

        //added ro find serverId from former clientId
        String formerServerId = null;

        for (String serverId : LeaderState.getInstance().getGlobalClientList().keySet()) {
            if (LeaderState.getInstance().getGlobalClientList().get(serverId).contains(clientId)) {
                formerServerId = serverId;
                break;
            }
        }
        //moved from joinroomrequest
        synchronized (LeaderState.getInstance()) {
            LeaderState.getInstance().removeFromGlobalClientAndRoomList(clientId, formerServerId, formerRoomId);
            LeaderState.getInstance().addToGlobalClientAndRoomList(clientId, serverId, roomId);
        }
        logger.info("Moved Client " + clientId + " to server " + serverId
                + " and room " + roomId + " is updated as current room");
    }
}
