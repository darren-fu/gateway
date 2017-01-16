package df.open.core.remote;

import df.open.http.HttpClient;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.internal.StringUtil;
import org.apache.http.HttpResponse;

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
public class Requester {


    public static void requestRemote(ChannelHandlerContext ctx, FullHttpRequest originRequest) {
        System.out.println("#@!#@!#@#!@# requestRemote");
        HttpResponse response = HttpClient.requestWithGet("http://localhost:9500/hello");
        System.out.println("response:" + response);
        execute(ctx, originRequest, response);
    }


    private static void execute(
            ChannelHandlerContext ctx, FullHttpRequest req, HttpResponse res) {
        if (HttpUtil.isKeepAlive(req)) {
            System.out.println("isKeepAlive###");
            res.addHeader("Connection", "keep-alive");
            ctx.write(res);
            ctx.flush();
        } else {
            System.out.println("not keep alive ################");
            ctx.write(res);
            ctx.flush();
            ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        }
    }


    private static StringBuilder responseToString(HttpResponse response) {
        StringBuilder buf = new StringBuilder(256);
        buf.append(StringUtil.simpleClassName(response));
        buf.append("(decodeResult: ");
//        buf.append(response.getEntity().d);
        buf.append(", version: ");
        buf.append(response.getProtocolVersion());
        buf.append(", content: ");
        buf.append(response.getEntity());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
        return buf;
    }
}
