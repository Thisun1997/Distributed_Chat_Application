package Messages;

import Core.Room;
import Protocols.ClientServer;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MessageMessage extends ClientMessage{

    private String content;
    public MessageMessage(String content){
        this.content=content;
    }

    @Override
    public void handle(Channel channel) {
        String clientID = ServerState.getInstance().getIdMap().inverse().get(channel);
        String roomID = ServerState.getInstance().getMember(clientID).getRoom();
        Room room= ServerState.getInstance().getRoom(roomID);
        ChannelGroup memberGroup = room.getMemberGroup();
        ChannelGroup messageGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        messageGroup.addAll(memberGroup);
        messageGroup.remove(channel);
        ClientServer.broadcast(messageGroup,new MessageReplyMessage(clientID,this.content));
    }
}
