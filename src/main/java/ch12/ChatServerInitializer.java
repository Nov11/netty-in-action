package ch12;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * Created by c0s on 16-4-18.
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {
    private final ChannelGroup group;
    private final SslContext context;

    public ChatServerInitializer(ChannelGroup group) {
        this.group = group;

        /**
         * add ssl support
         */
        SslContext c = null;
//        try {
//            SelfSignedCertificate cert = new SelfSignedCertificate();
//            c = SslContext.newServerContext(cert.certificate(), cert.privateKey());
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (SSLException e) {
//            e.printStackTrace();
//        }

        this.context = c != null ? c : null;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new HttpRequestHandler("/ws"));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new TextWebSocketFrameHandler(group));

        /**
         * add ssl support
         */
//        if (context != null) {
//            pipeline.addFirst(new SslHandler(context.newEngine(ch.alloc())));
//        }
    }
}
