package consensus.election.timeout;

import Server.Server;
import Server.ServerInfo;
import consensus.Leader;
import consensus.election.FastBullyAlgorithm;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ViewMessageTimeout extends MessageTimeout {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(!Server.getInstance().getViewMessageReceived() && !interrupted.get()){
            FastBullyAlgorithm stopFBA = new FastBullyAlgorithm("stopViewTimeout");
            stopFBA.stopElection();
            if(Leader.getInstance() == null){
                FastBullyAlgorithm coordinatorFBA = new FastBullyAlgorithm("coordinatorViewTimeout");
                new Thread(coordinatorFBA).start();
            }
            Server.getInstance().setViewMessageReceived(false);
        }
    }
}
