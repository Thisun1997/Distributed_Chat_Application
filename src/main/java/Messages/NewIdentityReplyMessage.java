package Messages;

import io.netty.channel.Channel;

public class NewIdentityReplyMessage extends ClientMessage{

    private String approved;
    public NewIdentityReplyMessage(boolean approved){
        this.approved= String.valueOf(approved);
    }

    @Override
    public void handle(Channel channel) {

    }
}
