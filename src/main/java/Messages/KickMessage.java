package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class KickMessage  extends CoordinationMessage{
    private String id;
    public KickMessage(String id){
        this.id=id;
    }
    @Override
    public void handle() {
        ServerState serverState=ServerState.getInstance();
        serverState.removeSuspect(id);
        serverState.removeHeartbeat(id);
        serverState.downServer(id);

    }
}
