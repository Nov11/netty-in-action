package ch13;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * Created by Nov11 on 16-4-19.
 */
public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket>{
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {

        ByteBuf data = msg.content();
        String msgBody = data.toString(CharsetUtil.UTF_8);
        int idx = msgBody.indexOf(LogEvent.SEPERATOR);
        if (idx == -1) {
            System.out.println(msgBody);
            return;
        }

        /**
         * xdi is not much reliable.
         * as when msg = msg1 1111222 111122233333, xdi is always 54
         * so finding separator is performed when msg converted to string
         */
//        int xdi = data.indexOf(0, data.readableBytes(), LogEvent.SEPERATOR);
//        System.out.println(data);
//        System.out.println(xdi);

        String fileName = msgBody.substring(0, idx);
        String logMsg = msgBody.substring(idx + 1);
        LogEvent logEvent = new LogEvent(msg.sender(), System.currentTimeMillis(), fileName, logMsg);
        out.add(logEvent);
    }
}
