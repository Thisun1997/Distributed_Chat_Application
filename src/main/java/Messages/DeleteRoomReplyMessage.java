package Messages;

import io.netty.channel.Channel;

public class DeleteRoomReplyMessage extends ClientMessage{

        private String roomid;
        private String approved;

     public DeleteRoomReplyMessage(String roomId,boolean approved){
         this.roomid=roomId;
         this.approved=String.valueOf(approved);
     }
    @Override
    public void handle(Channel channel) {

    }
}
