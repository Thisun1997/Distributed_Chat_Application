package Protocols;

import Messages.*;
import Services.ServerLogger;
import States.ServerState;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.*;
import org.apache.log4j.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = ServerLogger.getLogger(ServerState.getInstance().getServerId(), ServerHandler.class);
    private String baseHandler;
    ServerHandler(String baseHandler){
        this.baseHandler=baseHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(baseHandler.equals("Client")) {
            logger.info("A client connected to the chat server");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            handleBase(msg,ctx.channel());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            if(baseHandler.equals("Client")) {
                if(!ServerState.getInstance().getAlive(ctx.channel())) {
                    QuitMessage quitMessage = new QuitMessage();
                    quitMessage.handle(ctx.channel());
                    logger.warn("A client disconnected from the chat server");
                }
                else{
                    ServerState.getInstance().setAlive(ctx.channel(),false);
                }
            }
            ctx.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleBase(Object msg, Channel channel) {
        JsonObject jm = (JsonObject) msg;
        Gson gson=new Gson();
        if(this.baseHandler.equals("Coordination")) {
            if(jm.has("option")) {
                String option = jm.get("option").getAsString();
                switch (option) {
                    case "gossip":
                        GossipMessage gossipMessage = gson.fromJson(gson.toJson(jm), GossipMessage.class);
                        gossipMessage.handle();
                        break;
                    case "election":
                        ElectionMessage electionMessage = gson.fromJson(gson.toJson(jm), ElectionMessage.class);
                        electionMessage.handle();
                        break;
                    case "answer":
                        AnswerMessage answerMessage = gson.fromJson(gson.toJson(jm), AnswerMessage.class);
                        answerMessage.handle();
                        break;
                    case "nomination":
                        NominationMessage nominationMessage = gson.fromJson(gson.toJson(jm), NominationMessage.class);
                        nominationMessage.handle();
                        break;
                    case "iamup":
                        IAmUpMessage iAmUpMessage = gson.fromJson(gson.toJson(jm), IAmUpMessage.class);
                        iAmUpMessage.handle();
                        break;
                    case "view":
                        ViewMessage viewMessage = gson.fromJson(gson.toJson(jm), ViewMessage.class);
                        viewMessage.handle();
                        break;
                    case "coordinator":
                        CoordinatorMessage coordinatorMessage = gson.fromJson(gson.toJson(jm), CoordinatorMessage.class);
                        coordinatorMessage.handle();
                        break;
                    case "leaderupdate":
                        LeaderUpdateMessage leaderUpdateMessage = gson.fromJson(gson.toJson(jm), LeaderUpdateMessage.class);
                        leaderUpdateMessage.handle();
                        break;
                    case "leaderstateupdatecomplete":
                        LeaderStateUpdateCompleteMessage leaderStateUpdateCompleteMessage = gson.fromJson(gson.toJson(jm), LeaderStateUpdateCompleteMessage.class);
                        leaderStateUpdateCompleteMessage.handle();
                        break;
                    case "vote":
                        VoteMessage voteMessage = gson.fromJson(gson.toJson(jm), VoteMessage.class);
                        voteMessage.handle();
                        break;
                    case "kick":
                        KickMessage kickMessage = gson.fromJson(gson.toJson(jm), KickMessage.class);
                        kickMessage.handle();
                        break;
                    case "answervote":
                        AnswerVoteMessage answerVoteMessage = gson.fromJson(gson.toJson(jm), AnswerVoteMessage.class);
                        answerVoteMessage.handle();
                        break;
                    default:
                        logger.warn("wrong option coordination message");
                }
            }
            else{
                String type = jm.get("type").getAsString();
                switch (type) {
                    case "clientidapproval":
                        ClientIdApprovalRequestMessage clientIdApprovalRequestMessage=gson.fromJson(gson.toJson(jm),ClientIdApprovalRequestMessage.class);
                        clientIdApprovalRequestMessage.handle(channel);
                        break;
                    case "list":
                        ListRequestMessage listRequestMessage = gson.fromJson(gson.toJson(jm), ListRequestMessage.class);
                        listRequestMessage.handle(channel);
                        break;
                    case "roomcreateapproval":
                        RoomCreateApprovalRequestMessage roomCreateApprovalRequestMessage = gson.fromJson(gson.toJson(jm), RoomCreateApprovalRequestMessage.class);
                        roomCreateApprovalRequestMessage.handle(channel);
                        break;
                    case "joinroomapproval":
                        JoinRoomApprovalRequestMessage joinRoomApprovalRequestMessage = gson.fromJson(gson.toJson(jm), JoinRoomApprovalRequestMessage.class);
                        joinRoomApprovalRequestMessage.handle(channel);
                        break;
                    case "deleteroom":
                        DeleteRoomRequestMessage deleteRoomRequestMessage = gson.fromJson(gson.toJson(jm), DeleteRoomRequestMessage.class);
                        deleteRoomRequestMessage.handle(channel);
                        break;
                    case "movejoin":
                        MoveJoinRequestMessage moveJoinRequestMessage = gson.fromJson(gson.toJson(jm), MoveJoinRequestMessage.class);
                        moveJoinRequestMessage.handle(channel);
                        break;
                    case "quit":
                        QuitRequestMessage quitRequestMessage = gson.fromJson(gson.toJson(jm), QuitRequestMessage.class);
                        quitRequestMessage.handle(channel);
                        break;
                    default:
                        logger.warn(type+" : Wrong type coordination message");
                }
            }
        }
        else{
            String type = jm.get("type").getAsString();
            switch (type) {
                case "newidentity":
                    NewIdentityMessage newIdentityMessage=gson.fromJson(gson.toJson(jm),NewIdentityMessage.class);
                    newIdentityMessage.handle(channel);
                    break;
                case "who":
                    WhoMessage whoMessage=gson.fromJson(gson.toJson(jm),WhoMessage.class);
                    whoMessage.handle(channel);
                    break;
                case "list":
                    ListMessage listMessage=gson.fromJson(gson.toJson(jm),ListMessage.class);
                    listMessage.handle(channel);
                    break;
                case "createroom":
                    CreateRoomMessage createRoomMessage=gson.fromJson(gson.toJson(jm),CreateRoomMessage.class);
                    createRoomMessage.handle(channel);
                    break;
                case "deleteroom":
                    DeleteRoomMessage deleteRoomMessage=gson.fromJson(gson.toJson(jm),DeleteRoomMessage.class);
                    deleteRoomMessage.handle(channel);
                    break;
                case "joinroom":
                    JoinRoomMessage joinRoomMessage=gson.fromJson(gson.toJson(jm),JoinRoomMessage.class);
                    joinRoomMessage.handle(channel);
                    break;
                case "movejoin":
                    MoveJoinMessage moveJoinMessage=gson.fromJson(gson.toJson(jm),MoveJoinMessage.class);
                    moveJoinMessage.handle(channel);
                    break;
                case "message":
                    MessageMessage messageMessage=gson.fromJson(gson.toJson(jm),MessageMessage.class);
                    messageMessage.handle(channel);
                    break;
                case "quit":
                    QuitMessage quitMessage=gson.fromJson(gson.toJson(jm),QuitMessage.class);
                    quitMessage.handle(channel);
                    break;
                default:
                    logger.warn("Wrong type client message");
            }
        }
    }
}
