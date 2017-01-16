package df.open.core.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AsciiString;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.List;

import static io.netty.handler.codec.http.HttpConstants.CR;
import static io.netty.handler.codec.http.HttpConstants.LF;
import static io.netty.util.AsciiString.c2b;

/**
 * 说明:
 * <p/>
 * Copyright: Copyright (c)
 * <p/>
 * Company:
 * <p/>
 *
 * @author darren-fu
 * @version 1.0.0
 * @contact 13914793391
 * @date 2017/1/15
 */
public class HttpClientResponseEncoder<R extends HttpResponse> extends MessageToMessageEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        System.out.println("HttpRemoteResponseEncoder encode:" + msg);
        ByteBuf buf = null;
        buf = ctx.alloc().buffer();

        R response = (R) msg;

        encodeInitialLine(response, buf);
        encodeHeaderLine(response,buf);


        response.getEntity().getContent();

        buf.writeBytes(EntityUtils.toByteArray(response.getEntity()));
        out.add(buf);
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof HttpResponse;
    }

    static void encodeAscii0(CharSequence seq, ByteBuf buf) {
        int length = seq.length();
        for (int i = 0 ; i < length; i++) {
            buf.writeByte(c2b(seq.charAt(i)));
        }
    }



    static final byte[] CRLF = { CR, LF };

    private void encodeInitialLine(HttpResponse response,ByteBuf buf){

        encodeAscii0(response.getStatusLine().toString(), buf);
        buf.writeBytes(CRLF);
    }

    private void encodeHeaderLine(HttpResponse response, ByteBuf buf) throws Exception {
        for (Header header : response.getAllHeaders()) {
            HttpClientHeadersEncoder.encoderHeader(header.getName(), header.getValue(), buf);
        }
        buf.writeBytes(CRLF);
    }


    static class HttpClientHeadersEncoder {

        private HttpClientHeadersEncoder() {
        }

        public static void encoderHeader(CharSequence name, CharSequence value, ByteBuf buf) throws Exception {
            final int nameLen = name.length();
            final int valueLen = value.length();
            final int entryLen = nameLen + valueLen + 4;
            buf.ensureWritable(entryLen);
            int offset = buf.writerIndex();
            writeAscii(buf, offset, name, nameLen);
            offset += nameLen;
            buf.setByte(offset ++, ':');
            buf.setByte(offset ++, ' ');
            writeAscii(buf, offset, value, valueLen);
            offset += valueLen;
            buf.setByte(offset ++, '\r');
            buf.setByte(offset ++, '\n');
            buf.writerIndex(offset);
        }

        private static void writeAscii(ByteBuf buf, int offset, CharSequence value, int valueLen) {
            if (value instanceof AsciiString) {
                ByteBufUtil.copy((AsciiString) value, 0, buf, offset, valueLen);
            } else {
                writeCharSequence(buf, offset, value, valueLen);
            }
        }

        private static void writeCharSequence(ByteBuf buf, int offset, CharSequence value, int valueLen) {
            for (int i = 0; i < valueLen; ++i) {
                buf.setByte(offset ++, c2b(value.charAt(i)));
            }
        }
    }




}
