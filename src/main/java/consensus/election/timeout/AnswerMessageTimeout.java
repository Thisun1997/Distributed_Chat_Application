package consensus.election.timeout;


import States.ServerState;
import consensus.election.FastBullyAlgorithm;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Collections;

@DisallowConcurrentExecution
public class AnswerMessageTimeout extends MessageTimeout{
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(ServerState.getInstance().getOngoingElection()){
            if (ServerState.getInstance().getAnswerMessageReceived() || interrupted.get()) {
                //answer messages received, send the nominator message
                FastBullyAlgorithm nominationFBA = new FastBullyAlgorithm("sendNominationAnswerTimeout");
                new Thread(nominationFBA).start();
//            nominationFBA.sendNominationMessage("sendNominationAnswerTimeout");

            } else {
                // answer messages were not received send coordinator message to lower priority servers
//                if( Integer.parseInt(Collections.max(ServerState.getInstance().getUpServers())) < Integer.parseInt(ServerState.getInstance().getServerId())){
                    FastBullyAlgorithm coordinatorFBA = new FastBullyAlgorithm("coordinatorAnswerTimeout");
                    new Thread(coordinatorFBA).start();
//                }
        }

    }
}
}
