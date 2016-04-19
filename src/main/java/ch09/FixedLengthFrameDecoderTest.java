package ch09;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Nov11 on 16-4-18.
 */
public class FixedLengthFrameDecoderTest {
    @Test
    public void testDecode() {
        ByteBuf byteBuf = Unpooled.buffer();

        /**
         * If use 10 instead of 9, this test also passes.
         * The last byte doesn't make up a frame, so readInbound will return null
         * as it does when we feed the test 9 bytes input.
         */
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(i);
        }

        ByteBuf input = byteBuf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        /**
         * I think input shares ref count with byteBuf.
         * 'retain' is called which increases reference count, not passing merely 'input' as argument.
         * Without this, 'input' will be released after writeInbound returning.
         * And it will not be valid to extract info to compare with bytes latter read from EmbeddedChannel.
         *
         * duplicate is better than copy in that memory coping of contents is not necessary.
         * i.e.:
         *      assertTrue(channel.writeInbound(input));
         *      ..finish
         *      bytebuf.readSlice();    <- throws IllegalReferenceCountException
         *
         */
        System.out.println("refCnt before writeInbound " + input.refCnt());
        assertTrue(channel.writeInbound(input.retain()));
        assertTrue(channel.finish());
        System.out.println("refCnt after writeInbound " + input.refCnt());
        ByteBuf read = (ByteBuf) channel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        read = (ByteBuf) channel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        read = (ByteBuf) channel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        assertNull(channel.readInbound());

        /**
         * omit releasing code for ByteBuf 'read'
         */
        byteBuf.release();
    }

    @Test
    public void testFramesDecode2() {
        ByteBuf byteBuf = Unpooled.buffer();
        for(int i = 0; i < 9; i++) {
            byteBuf.writeByte(i);
        }

        ByteBuf input = byteBuf.duplicate();

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));

        /**
         * input.readBytes returns a  newly allocated buf. The buf will be released after writeinbound.
         * refCnt of input is not affected. So retain is not needed here.
         */
        assertFalse(embeddedChannel.writeInbound(input.readBytes(2)));
        assertTrue(embeddedChannel.writeInbound(input.readBytes(7)));
        assertTrue(embeddedChannel.finish());

        ByteBuf read = (ByteBuf)embeddedChannel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        read = (ByteBuf)embeddedChannel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        read = (ByteBuf)embeddedChannel.readInbound();
        assertEquals(byteBuf.readSlice(3), read);

        assertNull(embeddedChannel.readInbound());

        byteBuf.release();
    }
}
