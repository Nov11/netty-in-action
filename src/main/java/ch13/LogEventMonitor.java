package ch13;

import ch08.Bootstraping;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by Nov11 on 16-4-19.
 */
public class LogEventMonitor {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap
                .group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new LogEventDecoder());
                        ch.pipeline().addLast(new LogEventInboundHandler());
                    }
                }).localAddress(33333);
        ChannelFuture future = bootstrap.bind().sync();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
//                super.run();
                eventLoopGroup.shutdownGracefully();
            }
        });
        future.channel().closeFuture().sync();
    }
}
