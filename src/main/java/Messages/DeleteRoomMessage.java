package Messages;

import Core.Member;
import Core.Room;
import Protocols.Client;
import Protocols.ClientServer;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteRoomMessage extends ClientMessage{

    private  String roomid;

    DeleteRoomMessage(String roomId){
        this.roomid=roomId;
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

    private void handleBase(Channel channel){
        String mainHallId = "MainHall-" + ServerState.getInstance().getServerId();
        String clientId=ServerState.getInstance().getIdMap().inverse().get(channel);
        if(ServerState.getInstance().getRooms().containsKey(roomid)){
            //check sync
            String serverId = ServerState.getInstance().getServerId();
            Room room = ServerState.getInstance().getRoom(roomid);
            if(room.getOwner().equals(clientId)){

                if(LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())){
                    LeaderState.getInstance().removeRoom(serverId, roomid, mainHallId,clientId);
                }else{
                    //update leader server
                    Client.send(LeaderState.getInstance().getLeaderID(), new DeleteRoomRequestMessage(serverId, clientId, roomid, mainHallId),true);
                }
                System.out.println("INFO : room [" + roomid + "] was deleted by : " + clientId);


                ArrayList<String> formerClients = ServerState.getInstance().getRoom(roomid).getMembers();
                ChannelGroup formerChannels = ServerState.getInstance().getRoom(roomid).getMemberGroup();
                ArrayList<String> mainHallClients = ServerState.getInstance().getRoom(mainHallId).getMembers();
                ChannelGroup mainHallChannels = ServerState.getInstance().getRoom(mainHallId).getMemberGroup();

                //add clients in deleted room to main hall
                mainHallClients.addAll(formerClients);
                mainHallChannels.addAll(formerChannels);

                ServerState.getInstance().getRooms().remove(roomid);
                for(String formerClient: formerClients){
                    Member member=ServerState.getInstance().getMember(formerClient);
                    member.setRoom(mainHallId);

                    ClientServer.broadcast(mainHallChannels,new RoomChangeReplyMessage(clientId, roomid, mainHallId));
                }
                ClientServer.send(channel,new DeleteRoomReplyMessage(roomid,true));
            }else{
                ClientServer.send(channel,new DeleteRoomReplyMessage(roomid,false));
                System.out.println("WARN : Requesting client [" + clientId + "] does not own the room ID [" + roomid + "]");
            }
        }else{
            ClientServer.send(channel,new DeleteRoomReplyMessage(roomid,false));
            System.out.println("WARN : Received room ID [" + roomid + "] does not exist");
        }
    }



}