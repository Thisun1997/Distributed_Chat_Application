package Messages;

import States.ServerState;
import consensus.election.FastBullyAlgorithm;

public class IAmUpMessage extends CoordinationMessage{

    private String id;

    public IAmUpMessage(String id){
        this.id=id;
    }
    @Override
    public void handle() {
        FastBullyAlgorithm sendViewFBA = new FastBullyAlgorithm("sendView",this);
        ServerState.getInstance().setUpServer(this.id);
        new Thread(sendViewFBA).start();
    }

    public String getId() {
        return id;
    }
}
