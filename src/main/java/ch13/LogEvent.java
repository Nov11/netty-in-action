package ch13;

import java.net.InetSocketAddress;

/**
 * Created by c0s on 16-4-19.
 */
public class LogEvent {
    public static final byte SEPERATOR= ':';
    private final InetSocketAddress source;
    private final String logfile;
    private final String msg;
    private final long received;

    public LogEvent(String logfile, String msg) {
        this(null, -1, logfile, msg);
    }

    public LogEvent(InetSocketAddress source, long received, String logfile, String msg) {
        this.source = source;
        this.received = received;
        this.logfile = logfile;
        this.msg = msg;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }

    public long getReceived() {
        return received;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "source=" + source +
                ", logfile='" + logfile + '\'' +
                ", msg='" + msg + '\'' +
                ", received=" + received +
                '}';
    }
}
