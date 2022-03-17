package Messages;

import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;

public class QuitRequestMessage extends ClientMessage{

    String clientId;
    String former;
    String serverId;

    public QuitRequestMessage(String clientId,String former,String serverId){
        this.clientId=clientId;
        this.former=former;
        this.serverId=serverId;
    }
    @Override
    public void handle(Channel channel) {
        LeaderState.getInstance().removeFromGlobalClientAndRoomList(clientId, serverId,former);
    }
}
