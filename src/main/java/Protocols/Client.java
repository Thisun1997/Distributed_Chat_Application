package Protocols;

import Messages.IAmUpMessage;
import Messages.Message;
import Services.Log4jLogger;
import States.ServerState;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayList;
import java.util.Hashtable;

public class Client {
    public static void send(String id, Message msg, boolean async) {
        ServerState serverState = ServerState.getInstance();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("string_decoder", new StringDecoder());
                    ch.pipeline().addLast("decoder", new ByteToJsonDecoder());
                    ch.pipeline().addLast("string_encoder", new StringEncoder());
                    ch.pipeline().addLast("encoder", new JsonToByteEncoder());
                    ch.pipeline().addLast("client", new ClientHandler(msg, async));
                }
            });

            String[] server = serverState.getServers().get(id);
            String host = server[0];
            int port = Integer.parseInt(server[1]);
            ChannelFuture f = b.connect(host, port).sync();
            if (msg.getClass().getSimpleName().substring(0, msg.getClass().getSimpleName().length() - "Message".length()).toLowerCase().equals("iamup")) {
                ServerState.getInstance().setUpServer(id);
            }
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            Log4jLogger.logTrace("Server " + id + " is down");
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    // broadcast message only for peers
    public static void broadcast(Message msg, ArrayList<String> subServers) {
        for (String serverId : subServers) {
            send(serverId, msg, true);

        }
    }
}
