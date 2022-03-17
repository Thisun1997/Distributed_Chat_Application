package Messages;

import Core.Room;
import Protocols.Client;
import Protocols.ClientServer;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRoomMessage extends ClientMessage {
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), CreateRoomMessage.class);
    private String roomid;

    public CreateRoomMessage(String roomId) {
        this.roomid = roomId;
    }

    @Override
    public void handle(Channel channel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if(ServerState.getInstance().getLeaderUpdateComplete()){
            handleBase(channel);
        }
        else {
            executor.submit(()->{
                while (!ServerState.getInstance().getLeaderUpdateComplete()) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                handleBase(channel);
            });
        }
    }


    public void handleBase(Channel channel) {
        int isRoomCreationApproved = -1;
        String clientId = ServerState.getInstance().getIdMap().inverse().get(channel);
        String formerRoomID = ServerState.getInstance().getMember(clientId).getRoom();
        boolean isRoomOwner = ServerState.getInstance().getMember(clientId).getIsRoomOwner();
        if (validate() && !isRoomOwner) {
            if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
                boolean roomIDTaken = LeaderState.getInstance().isRoomIDTaken(roomid);
                isRoomCreationApproved = roomIDTaken ? 0 : 1;
                logger.info("Room '" + roomid +
                        "' creation request from client " + clientId +
                        " is" + (roomIDTaken ? "not" : " ") + "approved");

            } else {
                try {
                    Client.send(LeaderState.getInstance().getLeaderID(), new RoomCreateApprovalRequestMessage(clientId, roomid,formerRoomID, ServerState.getInstance().getServerId(), channel.id().asShortText()), false);
                    logger.info("Room "+roomid+" create request sent to leader "+LeaderState.getInstance().getLeaderID()+" by "+clientId);
                    isRoomCreationApproved = ServerState.getInstance().getIsRoomCreationApproved(channel.id().asShortText());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (isRoomCreationApproved == 1) {


                // self client include in channel group
                ChannelGroup formerChannels = ServerState.getInstance().getRoom(formerRoomID).getMemberGroup();

                //update server state
                ServerState.getInstance().getRoom(formerRoomID).removeMember(clientId);

                Room newRoom = new Room(roomid, clientId);
                newRoom.setMember(clientId);
                newRoom.setMemberChannel(channel);
                ServerState.getInstance().setRoom(roomid, newRoom);

                ServerState.getInstance().getMember(clientId).setRoom(roomid);
                ServerState.getInstance().getMember(clientId).setIsRoomOwner(true);

                if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
                    LeaderState.getInstance().addToRoomList(
                            clientId,
                            ServerState.getInstance().getServerId(),
                            roomid,
                            formerRoomID
                    );
                }

                ClientServer.send(channel, new CreateRoomReplyMessage(
                        roomid,
                        "true"
                ));

                ClientServer.broadcast(formerChannels, new RoomChangeReplyMessage(
                        clientId,
                        formerRoomID,
                        roomid
                ));
                ServerState.getInstance().getRoom(formerRoomID).removeMemberChannel(channel);
                ServerState.getInstance().removeIsRoomCreationApproved(channel.id().asShortText());
                logger.info(clientId + " successfully created the room "+roomid);
            } else if (isRoomCreationApproved == 0) {
                logger.warn("Room id " + roomid + " already in use");
                ClientServer.send(channel, new CreateRoomReplyMessage(
                        roomid,
                        "false"
                ));
                ServerState.getInstance().removeIsRoomCreationApproved(channel.id().asShortText());
            }
            isRoomCreationApproved = -1;
        } else {
            logger.warn("Room "+roomid+" is incorrect");
            ClientServer.send(channel, new CreateRoomReplyMessage(
                    roomid,
                    "false"
            ));
        }
    }

    public boolean validate() {
        if (Character.toString(this.roomid.charAt(0)).matches("[a-zA-Z]+") && this.roomid.matches("[a-zA-Z0-9]+") && this.roomid.length() >= 3 && this.roomid.length() <= 16) {
            return true;
        } else {
            return false;
        }
    }


}
