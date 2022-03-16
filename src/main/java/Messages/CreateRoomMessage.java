package Messages;

import Core.Room;
import Protocols.Client;
import Protocols.ClientServer;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRoomMessage extends ClientMessage {

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
        if (validate()) {
            if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
                boolean roomIDTaken = LeaderState.getInstance().isRoomIDTaken(roomid);
                isRoomCreationApproved = roomIDTaken ? 0 : 1;
                System.out.println("INFO : Room '" + roomid +
                        "' creation request from client " + clientId +
                        " is" + (roomIDTaken ? "not" : " ") + "approved");

            } else {
                try {
                    Client.send(LeaderState.getInstance().getLeaderID(), new RoomCreateApprovalRequestMessage(clientId, roomid,formerRoomID, ServerState.getInstance().getServerId(), channel.id().asShortText()), false);
                    isRoomCreationApproved = ServerState.getInstance().getIsRoomCreationApproved(channel.id().asShortText());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (isRoomCreationApproved == 1) {
                System.out.println("INFO : Received correct room ID :" + roomid);


                // self client include in channel group
                ChannelGroup formerChannels = ServerState.getInstance().getRoom(formerRoomID).getMemberGroup();

                //update server state
                ServerState.getInstance().getRoom(formerRoomID).removeMember(clientId);
                ServerState.getInstance().getRoom(formerRoomID).removeMemberChannel(channel);

                Room newRoom = new Room(roomid, clientId);
                newRoom.setMember(clientId);
                newRoom.setMemberChannel(channel);
                ServerState.getInstance().setRoom(roomid, newRoom);

                ServerState.getInstance().getMember(clientId).setRoom(roomid);

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
                ServerState.getInstance().removeIsRoomCreationApproved(channel.id().asShortText());
            } else if (isRoomCreationApproved == 0) {
                System.out.println("WARN : Room id [" + roomid + "] already in use");
                ClientServer.send(channel, new CreateRoomReplyMessage(
                        roomid,
                        "false"
                ));
                ServerState.getInstance().removeIsRoomCreationApproved(channel.id().asShortText());
            }
            isRoomCreationApproved = -1;
        } else {
            System.out.println("WARN : Received wrong room ID type or client already owns a room [" + roomid + "]");
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
