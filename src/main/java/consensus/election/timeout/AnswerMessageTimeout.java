package consensus.election.timeout;

import Server.Server;
import consensus.election.FastBullyAlgorithm;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AnswerMessageTimeout extends MessageTimeout{
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (Server.getInstance().getAnswerMessageReceived() || interrupted.get()) {
            //answer messages were received
            FastBullyAlgorithm nominationFBA = new FastBullyAlgorithm("nomination");
            new Thread(nominationFBA).start();

        } else {
            // answer messages were not received
            // send coordinator message to lower priority servers
            FastBullyAlgorithm coordinatorFBA = new FastBullyAlgorithm("coordinator");
            new Thread(coordinatorFBA).start();
        }

    }
}
