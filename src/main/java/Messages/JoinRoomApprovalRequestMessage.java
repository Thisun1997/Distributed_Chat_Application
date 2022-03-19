package Messages;

import Protocols.CoordinationServer;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

public class JoinRoomApprovalRequestMessage extends ClientMessage {

    private String clientId;
    private String formerServerId;
    private String formerRoomId;
    private String roomId;
    private String channelId;
    private boolean inServer;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), JoinRoomApprovalRequestMessage.class);

    public JoinRoomApprovalRequestMessage(String clientId, String formerServerId, String formerRoomId, String roomId, String channelId, boolean inServer) {
        this.channelId = channelId;
        this.clientId = clientId;
        this.formerRoomId = formerRoomId;
        this.roomId = roomId;
        this.formerServerId = formerServerId;
        this.inServer = inServer;

    }

    @Override
    public void handle(Channel channel) {
//        System.out.println(inServer+" "+roomId+" ");
        if (inServer) {
            LeaderState.getInstance().InServerJoinRoomClient(clientId, formerServerId, formerRoomId, roomId);
        } else {
            String serverIDofTargetRoom = LeaderState.getInstance().getServerIdIfRoomExist(roomId);
            try {
                boolean approved = serverIDofTargetRoom != null;
                String serverId = null;
                if (approved) {
                    if (!serverIDofTargetRoom.equals(ServerState.getInstance().getServerId())) {
                        serverId = serverIDofTargetRoom;
                    } else {
                        serverId = ServerState.getInstance().getServerId();
                    }
                   // LeaderState.getInstance().removeFromGlobalClientAndRoomList(clientId, formerServerId, formerRoomId);//remove before route, later add on move join
                    // move to movejoin message handler to avoid new client join with same identity of clientId
                }

                CoordinationServer.send(channel, new JoinRoomApprovalResponseMessage(
                        serverId,
                        approved,
                        channelId));
                logger.info("Join Room from " + formerRoomId +
                        " to " + roomId + " for client " + clientId +
                        " is" + (serverIDofTargetRoom != null ? " " : " not ") + "approved");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
