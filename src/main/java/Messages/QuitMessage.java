package Messages;

import Protocols.Client;
import Protocols.ClientServer;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuitMessage extends ClientMessage{

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
        String clientId= ServerState.getInstance().getIdMap().inverse().get(channel);
        String roomId=ServerState.getInstance().getMember(clientId).getRoom();
        if (ServerState.getInstance().getRoom(roomId).getOwner().equals(clientId)) {
            try {
                new DeleteRoomMessage(roomId).handle(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("INFO : Deleted room before " + clientId + " quit");
        }

        // update global list - Leader class
        // send quit message to leader if itself is not leader
        if (!LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
            Client.send(LeaderState.getInstance().getLeaderID(),new QuitRequestMessage(clientId, roomId,ServerState.getInstance().getServerId()),true);
        } else {
            LeaderState.getInstance().removeFromGlobalClientAndRoomList(clientId,ServerState.getInstance().getServerId(),roomId);
        }

        // update local server
        ServerState.getInstance().getIdMap().inverse().remove(channel);
        if(channel.isActive()){
            ClientServer.send(channel,new RoomChangeReplyMessage(clientId,roomId,""));
        }

        System.out.println("INFO : " + clientId + " quit");
    }
}
