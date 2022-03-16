package Messages;

import States.ServerState;
import io.netty.channel.Channel;

public class AnswerMessage extends CoordinationMessage{

    private String id;

    public AnswerMessage(String id){
        this.id=id;
    }
    @Override
    public void handle() {
        String answerServerId = this.id;
        ServerState.getInstance().setTempCandidateServer(answerServerId);
        System.out.println("answer message from "+ answerServerId+" received.");
    }
}
