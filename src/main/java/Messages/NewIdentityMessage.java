package Messages;

import Core.Member;
import Protocols.Client;
import Protocols.ClientServer;
import States.LeaderState;
import States.ServerState;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewIdentityMessage extends ClientMessage{

    private String identity;

    public NewIdentityMessage(String identity)  {
        this.identity=identity;

    }

    @Override
    public void handle(Channel channel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if(ServerState.getInstance().getLeaderUpdateComplete()){
            handleBase(channel);
        }
        else {
            executor.submit(()->{
                while (!ServerState.getInstance().getLeaderUpdateComplete()) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                handleBase(channel);
            });
        }
    }


     private void handleBase(Channel channel) {
         int isClientApproved=-1;
         if (validate()) {
             if (ServerState.getInstance().getServerId().equals(LeaderState.getInstance().getLeaderID())) {
                 boolean clientIDTaken = LeaderState.getInstance().isClientIDTaken(identity);
                 isClientApproved = clientIDTaken ? 0 : 1;
             } else {
                 Client.send(LeaderState.getInstance().getLeaderID(), new ClientIdApprovalRequestMessage(identity, ServerState.getInstance().getServerId(), channel.id().asShortText()), false);
                 isClientApproved = ServerState.getInstance().getClientIdApproved(channel.id().asShortText());
             }

             // if client is approved
             if (isClientApproved == 1) {
                 ServerState.getInstance().getIdMap().put(identity,channel);
                 String mainHallID = "MainHall-" + ServerState.getInstance().getServerId();
                 Member member = new Member(identity,mainHallID);
                 ServerState.getInstance().setMember(identity,member);
                 // If I am the leader update the global list.
                 if (ServerState.getInstance().getServerId().equals(LeaderState.getInstance().getLeaderID())) {
                     LeaderState.getInstance().addToGlobalClientAndRoomList(identity, ServerState.getInstance().getServerId(), mainHallID);
                 }
                 // add client to main-hall
                 ServerState.getInstance().getRoom(mainHallID).setMember(identity);
                 ServerState.getInstance().getRoom(mainHallID).setMemberChannel(channel);

                 // broadcast to all the clients in main-hall

                 ClientServer.send(channel,new NewIdentityReplyMessage(true));
                 ClientServer.broadcast(ServerState.getInstance().getRoom(mainHallID).getMemberGroup(),new RoomChangeReplyMessage(identity,"",mainHallID));
                 ServerState.getInstance().removeClientIdApproved(channel.id().asShortText());
             }
             else if (isClientApproved == 0) {
                 ClientServer.send(channel,new NewIdentityReplyMessage(false));
                 ServerState.getInstance().removeClientIdApproved(channel.id().asShortText());
            }
             else {
                 System.out.println("server communication fail");
             }
         }
         else {
             ClientServer.send(channel,new NewIdentityReplyMessage(false));
         }
     }
    public boolean validate(){
        if(Character.toString(this.identity.charAt(0)).matches("[a-zA-Z]+") && this.identity.matches("[a-zA-Z0-9]+") && this.identity.length() >= 3 && this.identity.length() <= 16){
            return true;
        }
        else {
            return false;
        }
    }


}
