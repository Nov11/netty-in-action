package ch09;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Nov11 on 16-4-18.
 */
public class IntegerEncoderTest {
    @Test
    public void testEncoder() {
        /**
         * message to message encoder is a outbound encoder. so write/read outbound is used here.
         * note that although buf is passed to writeOutbound,
         * buf is not used to verify output of the handler.
         * So retain is not called and it will be released before writeOutbound returns.
         */
        ByteBuf buf = Unpooled.buffer();

        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }

        EmbeddedChannel channel = new EmbeddedChannel(new IntegerEncoder());
        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());

        for(int i = 1; i < 10; i++) {
            assertEquals(channel.readOutbound(), i);
        }

        assertNull(channel.readOutbound());
    }
}
