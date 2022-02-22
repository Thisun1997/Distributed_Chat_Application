package consensus.election.timeout;

import Server.Server;
import consensus.election.FastBullyAlgorithm;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NominationMessageTimeout extends MessageTimeout {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(Server.getInstance().getOngoingElection()){
            if (!interrupted.get()) {
                // restart the election procedure
                FastBullyAlgorithm startFBA = new FastBullyAlgorithm("restart_election");
                new Thread(startFBA).start();
            }
        }
    }
}
