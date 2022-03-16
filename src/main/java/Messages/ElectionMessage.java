package Messages;

import consensus.election.FastBullyAlgorithm;
import io.netty.channel.Channel;

public class ElectionMessage extends CoordinationMessage{

    private String id;

    public ElectionMessage(String id){
        this.id=id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void handle() {
        FastBullyAlgorithm electionFBA = new FastBullyAlgorithm("election",this);
        new Thread(electionFBA).start();
    }
}
