package Protocols;

import Messages.*;

import States.ServerState;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Message message;
    private boolean async;

    ClientHandler(Message message,boolean async){
        this.message=message;
        this.async=async;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        try {
            ChannelFuture f = ctx.channel().writeAndFlush(this.message);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(async){
                        ctx.close();
                    }
                    if (f.isSuccess()) {
//                        System.out.println("coordination send message success");

                    } else {
//                        System.out.println("coordination send message fail");
                    }
                }
            });
        }
        catch (Exception e) {
                e.printStackTrace();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JsonObject jm = (JsonObject) msg;
        Gson gson = new Gson();
        String type = jm.get("type").getAsString();
        switch (type) {
            case "clientidapproval":
                ClientIdApprovalResponseMessage clientIdApprovalResponseMessage = gson.fromJson(gson.toJson(jm), ClientIdApprovalResponseMessage.class);
                clientIdApprovalResponseMessage.handle(ctx.channel());
                ctx.close();
                break;
            case "list":
                ListResponseMessage listResponseMessage=gson.fromJson(gson.toJson(jm),ListResponseMessage.class);
                listResponseMessage.handle(ctx.channel());
                ctx.close();
                break;
            case "roomidapproval":
                RoomIdApprovalResponseMessage roomIdApprovalResponseMessage=gson.fromJson(gson.toJson(jm),RoomIdApprovalResponseMessage.class);
                roomIdApprovalResponseMessage.handle(ctx.channel());
                ctx.close();
                break;
            case "joinroomapproval":
                JoinRoomApprovalResponseMessage joinRoomApprovalResponseMessage=gson.fromJson(gson.toJson(jm),JoinRoomApprovalResponseMessage.class);
                joinRoomApprovalResponseMessage.handle(ctx.channel());
                ctx.close();
                break;
            default:
                System.out.println("default");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        try {
            ctx.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}


