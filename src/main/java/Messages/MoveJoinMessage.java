package Messages;

import Core.Member;
import Core.Room;
import Protocols.Client;
import Protocols.ClientServer;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MoveJoinMessage extends ClientMessage {

    private String former;
    private String roomid;
    private String identity;

    public MoveJoinMessage(String former, String roomId, String identity) {
        this.former = former;
        this.roomid = roomId;
        this.identity = identity;
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

    private void handleBase(Channel channel) {
        String roomID = roomid;
        if (!ServerState.getInstance().getRooms().containsKey(this.roomid)) {
            roomID = "MainHall-" + ServerState.getInstance().getServerId();
        }


        //if self is leader update leader state directly
        if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
            //added ro find serverId from former clientId
            String formerServerId = null;
            for (String serverId : LeaderState.getInstance().getGlobalClientList().keySet()) {
                if (LeaderState.getInstance().getGlobalClientList().get(serverId).contains(identity)) {
                    formerServerId = serverId;
                    break;
                }
            }
            //moved from joinroomrequest
            synchronized (LeaderState.getInstance()) {
                LeaderState.getInstance().removeFromGlobalClientAndRoomList(identity, formerServerId, former);
                LeaderState.getInstance().addToGlobalClientAndRoomList(identity, ServerState.getInstance().getServerId(), roomID);
            }
        } else {
            //update leader server
            Client.send(LeaderState.getInstance().getLeaderID(),
                    new MoveJoinRequestMessage(
                            ServerState.getInstance().getServerId(),
                            roomID,
                            former,
                            identity,
                            channel.id().asShortText())
                    , true);
        }

        Member member = new Member(identity, roomID);
        ServerState.getInstance().setMember(identity, member);
        ServerState.getInstance().getRoom(roomID).setMember(identity);
        ServerState.getInstance().getRoom(roomID).setMemberChannel(channel);
        ServerState.getInstance().getIdMap().put(identity, channel);

        ChannelGroup newClientChannels = ServerState.getInstance().getRoom(roomID).getMemberGroup();

        ClientServer.send(channel, new ServerChangeReplyMessage(true, "s" + ServerState.getInstance().getServerId()));
        ClientServer.broadcast(newClientChannels, new RoomChangeReplyMessage(identity, former, roomID));

    }

}
