package ch13;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by Nov11 on 16-4-19.
 */
public class LogEventInboundHandler extends SimpleChannelInboundHandler<LogEvent>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent msg) throws Exception {
        System.out.println(msg);
    }
}
