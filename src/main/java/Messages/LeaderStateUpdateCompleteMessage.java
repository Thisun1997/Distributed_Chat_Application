package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class LeaderStateUpdateCompleteMessage extends CoordinationMessage{
    private String id;

    public LeaderStateUpdateCompleteMessage(String id){
        this.id=id;
    }

    @Override
    public void handle() {
        String serverID = this.id;
        System.out.println("leader "+serverID+" update done..");
        ServerState.getInstance().setLeaderUpdateComplete(true);
    }
}
