package Protocols;


import Messages.Message;
import States.ServerState;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


abstract public class Server extends Thread{
    private String port;

    public Server(String port) {
            this.port=port;

    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        String baseHandler=this.getClass().getSimpleName().substring(0,this.getClass().getSimpleName().length() - "Server".length());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("string_decoder_"+baseHandler,new StringDecoder());
                            ch.pipeline().addLast("decoder_"+baseHandler,new ByteToJsonDecoder());
                            ch.pipeline().addLast("string_encoder_"+baseHandler,new StringEncoder());
                            ch.pipeline().addLast("encoder_"+baseHandler,new JsonToByteEncoder());
                            ch.pipeline().addLast("server_"+baseHandler,new ServerHandler(baseHandler));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            int port= Integer.parseInt(this.port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        }
        catch (Exception e){
            System.out.println(e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void send(Channel channel,Message msg){
        ChannelFuture f=channel.writeAndFlush(msg);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {

            }
        });

    }
    public static void broadcast(ChannelGroup channelGroup, Message msg){
        ChannelGroupFuture f=channelGroup.writeAndFlush(msg);
        f.addListener(new ChannelGroupFutureListener() {
            @Override
            public void operationComplete(ChannelGroupFuture channelGroupFutureFuture) throws Exception {

            }
        });

    }



}
