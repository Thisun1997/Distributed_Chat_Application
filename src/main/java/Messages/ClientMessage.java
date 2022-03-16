package Messages;

import io.netty.channel.Channel;

import java.util.Locale;

public abstract class ClientMessage extends Message{
    private String type;
    ClientMessage(){
        this.type=getTypeOrOption();
    }
    public abstract void handle(Channel channel);
}
