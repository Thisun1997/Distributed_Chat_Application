package Messages;

import io.netty.channel.Channel;

public class MessageReplyMessage extends ClientMessage{

    private String identity;
    private String content;
    public MessageReplyMessage(String identity,String content){
        this.identity=identity;
        this.content=content;
    }
    @Override
    public void handle(Channel channel) {

    }
}
