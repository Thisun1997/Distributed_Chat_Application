package Messages;

import Protocols.Client;
import Protocols.ClientServer;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JoinRoomMessage extends ClientMessage {
    private String roomid;
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), JoinRoomMessage.class);

    public JoinRoomMessage(String roomId) {
        this.roomid = roomId;
    }

    @Override
    public void handle(Channel channel) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (ServerState.getInstance().getLeaderUpdateComplete()) {
            handleBase(channel);
        } else {
            executor.submit(() -> {
                while (!ServerState.getInstance().getLeaderUpdateComplete()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handleBase(channel);
            });
        }
    }

    public void handleBase(Channel channel) {

        String clientId = ServerState.getInstance().getIdMap().inverse().get(channel);
        String formerRoomId = ServerState.getInstance().getMember(clientId).getRoom();
        String serverId = ServerState.getInstance().getServerId();

        if (ServerState.getInstance().getMember(clientId).getIsRoomOwner()) {
            logger.warn("Join room denied, Client" + clientId + " Owns a room");

            ClientServer.send(channel, new RoomChangeReplyMessage(clientId, formerRoomId, formerRoomId));

        } else if (ServerState.getInstance().getRooms().containsKey(this.roomid)) { //local room change
            // if self is leader update leader state directly
            if (ServerState.getInstance().getServerId().equals(LeaderState.getInstance().getLeaderID())) {
                LeaderState.getInstance().InServerJoinRoomClient(clientId, serverId, formerRoomId, roomid);

            } else {
                Client.send(LeaderState.getInstance().getLeaderID(), new JoinRoomApprovalRequestMessage(clientId, serverId, formerRoomId, roomid, channel.id().asShortText(), true), true);
            }

            ServerState.getInstance().getMember(clientId).setRoom(roomid);
            ServerState.getInstance().getRoom(formerRoomId).removeMember(clientId);
            ServerState.getInstance().getRoom(formerRoomId).removeMemberChannel(channel);
            ServerState.getInstance().getRoom(roomid).setMember(clientId);
            ServerState.getInstance().getRoom(roomid).setMemberChannel(channel);

            logger.info("client " + clientId + " joined room :" + roomid);


            // creating broadcast list
            ChannelGroup newClientChannels = ServerState.getInstance().getRoom(this.roomid).getMemberGroup();
            ChannelGroup oldClientChannels = ServerState.getInstance().getRoom(formerRoomId).getMemberGroup();
            ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

            clientChannels.addAll(newClientChannels);
            clientChannels.addAll(oldClientChannels);


            ClientServer.broadcast(clientChannels,
                    new RoomChangeReplyMessage(clientId, formerRoomId, roomid));

        } else {  // global room change

            int isJoinRoomApproved = -1;
            String approvedJoinRoomServer = null;

            //check if room id exist and if init route
            if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
                String serverIDofTargetRoom = LeaderState.getInstance().getServerIdIfRoomExist(roomid);
                isJoinRoomApproved = serverIDofTargetRoom != null ? 1 : 0;

                if (isJoinRoomApproved == 1) {
                    approvedJoinRoomServer = serverIDofTargetRoom;
                }
                logger.info("Received response for route request for join room in leader " + LeaderState.getInstance().getLeaderID());

            } else {
                Client.send(LeaderState.getInstance().getLeaderID(),
                        new JoinRoomApprovalRequestMessage(clientId, serverId, formerRoomId, roomid, channel.id().asShortText(), false), false);
                isJoinRoomApproved = ServerState.getInstance().getIsJoinRoomApproved(channel.id().asShortText());
                approvedJoinRoomServer = ServerState.getInstance().getApprovedJoinRoomServer(channel.id().asShortText());
                logger.info("Received response for route request for join room");
            }

            if (isJoinRoomApproved == 1) {
                //broadcast to former room
                ServerState.getInstance().getRoom(formerRoomId).removeMember(clientId);
                ServerState.getInstance().getRoom(formerRoomId).removeMemberChannel(channel);
                ServerState.getInstance().getIdMap().inverse().remove(channel);
                logger.info(" client " + clientId + " left room :" + formerRoomId);


                ChannelGroup oldClientChannels = ServerState.getInstance().getRoom(formerRoomId).getMemberGroup();

                ClientServer.broadcast(oldClientChannels,
                        new RoomChangeReplyMessage(clientId, formerRoomId, roomid));

                logger.info("Send broadcast to former room in local server");

                //server change : route
                ClientServer.send(channel,
                        new RouteReplyMessage(
                                roomid,
                                ServerState.getInstance().getServers().get(approvedJoinRoomServer)[0],
                                ServerState.getInstance().getServers().get(approvedJoinRoomServer)[2]));

                ServerState.getInstance().setAlive(channel, true);
                logger.info("Route Message Sent to Client");
                ServerState.getInstance().removeApprovedJoinRoomServer(channel.id().asShortText());

            } else if (isJoinRoomApproved == 0) { // Room not found on system
                logger.warn("Received room ID [" + roomid + "] does not exist");
                ClientServer.send(channel, new RoomChangeReplyMessage(clientId, formerRoomId, formerRoomId));
            }
            ServerState.getInstance().removeIsJoinRoomApproved(channel.id().asShortText());
        }

    }
}
