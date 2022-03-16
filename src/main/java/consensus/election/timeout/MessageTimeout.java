package consensus.election.timeout;

import States.ServerState;
import org.quartz.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MessageTimeout implements Job, InterruptableJob{

    protected ServerState serverState = ServerState.getInstance();
    protected AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted.set(true);
        System.out.println("Job was interrupted...");
    }
}
