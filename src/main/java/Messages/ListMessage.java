package Messages;


import Protocols.Client;
import Protocols.Server;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ListMessage extends ClientMessage {

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
        ArrayList<String> tempRoomList = null;
        if (LeaderState.getInstance().getLeaderID().equals(ServerState.getInstance().getServerId())) {
            tempRoomList = LeaderState.getInstance().getRoomIDList();

        } else {
            Client.send(LeaderState.getInstance().getLeaderID(),new ListRequestMessage(channel.id().asShortText()),false);
            tempRoomList =ServerState.getInstance().getTempRoomList(channel.id().asShortText());
            System.out.println(tempRoomList);
        }

        if (tempRoomList != null) {
            System.out.println("INFO : Received rooms in the system :" + tempRoomList);
            Server.send(channel,new RoomListReplyMessage(tempRoomList));
        }
    }
}
