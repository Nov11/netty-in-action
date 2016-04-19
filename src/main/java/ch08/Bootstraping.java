package ch08;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * Created by Nov11 on 16-4-18.
 */
public class Bootstraping {
    public void bootOneClient() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                System.out.println("client recv : " + msg.toString());
            }
        });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 8765));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("connection established");
                } else {
                    System.err.println("connection failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }

    public void bootOneServer() {
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group).channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                System.out.println("server recv : " + msg.toString());
            }
        });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress("localhost", 8765));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("server bind suc");
                } else {
                    System.err.println("server bind failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }

    /**
     * useful when implementing a proxy server
     */
    public void bootOneClientInServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class).childHandler(new SimpleChannelInboundHandler<ByteBuf>() {
            ChannelFuture connectFuture;

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                if (connectFuture.isDone()) {
                    System.out.println("server recv :" + msg.toString());
                }
            }

            /**
             * this function is for a proxy server.
             * server accepted a new connection and allocate a socket for it. Say c1.
             * c1 make connection to proxied server say s2.(channelActive)
             * c1 continues its business logic when the connection to s2 established. (connectionFuture.isDone())
             */
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                super.channelActive(ctx);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.channel(NioSocketChannel.class).handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println("proxy recv :" + msg.toString());
                    }
                });
                //I think returned eventLoop is managed by the second parameter of serverbootstrap,
                //which is also the event loop assigned to this channel.
                //note here is a eventLoop, not eventLoopGroup.
                bootstrap.group(ctx.channel().eventLoop());
                connectFuture = bootstrap.connect(new InetSocketAddress("localhost", 9012));
            }
        });

        /**
         * same as previous server
         */
        ChannelFuture future = serverBootstrap.bind(new InetSocketAddress("localhost", 8765));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("proxy server bind suc");
                } else {
                    System.err.println("proxy server bind failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }

    public void bootWithMultiHandlersAndUsingAttributes() {
        ServerBootstrap bootstrap = new ServerBootstrap();

        /**
         * attribute
         */
        AttributeKey<Integer> id = new AttributeKey<Integer>("ID");


        bootstrap
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                System.out.println("recv " + msg.toString());
                            }

                            @Override
                            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//                        super.channelRegistered(ctx);
                                /**
                                 * how to retrieve attr set in bootstrap
                                 */
                                Integer i = ctx.channel().attr(id).get();
                                //do something with i
                            }
                        });
                    }
                });

        /**
         * add attribute and options here.
         * options and attr must be set before bind or connect for them to be effective.
         *
         * note that there is childoption and childattr method for setting these for channel created on accept new conn.
         */
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.attr(id, 1234567);


        ChannelFuture future = bootstrap.bind(new InetSocketAddress(9999)).syncUninterruptibly();
    }

    //this is for server.
    public void bootDatagramServer() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                System.out.println("recv msg: " + msg.toString());
            }
        });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(8801));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("datagram server bind suc");
                } else {
                    System.err.println("dategram server bind failed");
                    future.cause().printStackTrace();
                }
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    }

    public void bootDatagramClient() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        System.out.println("client recv:" + msg.toString());
                    }
                });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(8801));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("udp client connected to server, omit destination in further write call.");
                } else {
                    System.err.println("udp client connect failed");
                    future.cause().printStackTrace();
                }
                ByteBuf buf = Unpooled.copiedBuffer("from udp client", CharsetUtil.UTF_8);
                future.channel().writeAndFlush(buf);
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    }

    /**
     * note:
     * 1.shutting eventLoopGroup down is omitted.
     * 2.sync on closefuture is needed to run client and server. or thread will return after connect.
     *   Maybe before connection attempt is made.
     *   I think running thread in eventloopgropu might not be able to stop junit test shutting down.
     *   I guess it is daemon thread.
     */

    Bootstraping bootstraping;

    @Before
    public void before() {
        bootstraping = new Bootstraping();
    }

    @Test
    public void datagramClient() {
        bootstraping.bootDatagramClient();
    }

    @Test
    public void datagramServer() {
        bootstraping.bootDatagramServer();
    }

}
