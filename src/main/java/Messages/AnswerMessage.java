package Messages;

import Services.ServerLogger;
import States.ServerState;
import org.apache.log4j.Logger;

public class AnswerMessage extends CoordinationMessage{
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), AnswerMessage.class);
    private String id;

    public AnswerMessage(String id){
        this.id=id;
    }
    @Override
    public void handle() {
        String answerServerId = this.id;
        ServerState.getInstance().setTempCandidateServer(answerServerId);
        ServerState.getInstance().setAnswerMessageReceived(true);
        logger.info("Answer message from "+ answerServerId+" received");
    }
}
