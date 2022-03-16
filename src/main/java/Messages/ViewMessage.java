package Messages;

import consensus.election.FastBullyAlgorithm;
import io.netty.channel.Channel;

public class ViewMessage extends CoordinationMessage{
    private String id;

    public ViewMessage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void handle() {
        FastBullyAlgorithm coordinatorFBA = new FastBullyAlgorithm("coordinator",this);
        new Thread(coordinatorFBA).start();
    }

}
