package Messages;

import consensus.election.FastBullyAlgorithm;
import io.netty.channel.Channel;

public class NominationMessage extends CoordinationMessage{
    @Override
    public void handle() {
        FastBullyAlgorithm nominationFBA = new FastBullyAlgorithm("coordinatorFromNomination");
        new Thread(nominationFBA).start();
    }
}
