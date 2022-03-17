package Messages;


import Protocols.Client;
import Protocols.Server;
import Services.ServerLogger;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ListMessage extends ClientMessage {
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), ListMessage.class);
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
            logger.info("Room list request message sent to leader "+LeaderState.getInstance().getLeaderID());
            tempRoomList =ServerState.getInstance().getTempRoomList(channel.id().asShortText());
        }

        if (tempRoomList != null) {
            Server.send(channel,new RoomListReplyMessage(tempRoomList));
            logger.debug("Rooms in the system :" + tempRoomList);
        }
    }
}
