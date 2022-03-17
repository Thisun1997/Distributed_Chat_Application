package consensus.election.timeout;

import Services.ServerLogger;
import States.ServerState;
import org.apache.log4j.Logger;
import org.quartz.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MessageTimeout implements Job, InterruptableJob{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), MessageTimeout.class);
    protected ServerState serverState = ServerState.getInstance();
    protected AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted.set(true);
        logger.debug("Job was interrupted");
    }
}
