package Messages;

import consensus.election.FastBullyAlgorithm;


public class CoordinatorMessage extends CoordinationMessage{

    private String id;

    public CoordinatorMessage(String id){
        this.id=id;
    }

    public String getId() {
        return id;
    }
    @Override
    public void handle() {
        FastBullyAlgorithm updateLeaderFBA = new FastBullyAlgorithm("updateLeader",this);
        new Thread(updateLeaderFBA).start();
    }
}
