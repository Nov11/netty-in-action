package ch13;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Nov11 on 16-4-19.
 */
public class LogEventBroadcaster {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final File file;

    public LogEventBroadcaster(InetSocketAddress address, File file) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        this.file = file;

        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(address));
    }

    public void run() throws InterruptedException, IOException {
        Channel ch = bootstrap.bind(22222).sync().channel();
        long pointer = 0;
        while(true){
            long len = file.length();
            if (len < pointer) {
                pointer = len;
            }else if (len > pointer){
                //1.watch out for len == pointer
                //2.pointer = raf.getFilePointer may be a race condition as readline finished just before
                //  a new line appended, and then getFilePointer is called.
                //  In this case, input will not be broadcast.
                //3.condition : len == pointer triggers [2] more frequently as it update pointer every 1s.
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(pointer);
                String line;
                while ((line = raf.readLine()) != null) {
                    ch.writeAndFlush(new LogEvent(null, -1, file.getAbsolutePath(), line));
                }
                pointer = raf.getFilePointer();
                raf.close();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        URL url = LogEventBroadcaster.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = url.toURI().toString();
            System.out.println(filePath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        if (filePath.contains("file:")) {
            filePath = filePath.substring(5);
        }
        File f = new File(filePath + "ch13-log");
        System.out.println(f.getAbsolutePath());
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(new InetSocketAddress("255.255.255.255", 33333), f);
        try{
            broadcaster.run();
        }finally {
            broadcaster.stop();
        }
    }
}
