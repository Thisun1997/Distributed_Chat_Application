package Messages;

import Core.Member;
import Core.Room;
import Protocols.ClientServer;
import Protocols.Server;
import States.ServerState;
import io.netty.channel.Channel;

import java.util.ArrayList;

public class WhoMessage extends ClientMessage{

    private String id;
    public WhoMessage(String id){
        this.id=id;
    }

    @Override
    public void handle(Channel channel) {
        ServerState serverState=ServerState.getInstance();
        String id=serverState.getIdMap().inverse().get(channel);
        Member member =serverState.getMember(id);
        String roomId = member.getRoom();
        Room room=serverState.getRoom(roomId);
        ArrayList<String> members =room.getMembers();
        String owner = room.getOwner();
        ClientServer.send(channel,new RoomContentsMessage(roomId,members,owner));
    }
}
