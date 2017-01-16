package df.open.core.handler;

import df.open.core.NettyServer;
import df.open.core.remote.Requester;
import df.open.utils.HttpTools;
import df.open.utils.RespTools;
import df.open.utils.StringTools;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

/**
 * The type Http handler.
 */
@Component
@ChannelHandler.Sharable
public class CustomHttpHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(CustomHttpHandler.class);
    private WebSocketServerHandshaker handshaker;

    private String mac;

    private static int count = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {//如果是HTTP请求，进行HTTP操作

            FullHttpRequest httpRequest = (FullHttpRequest) msg;

//            logger.error("httpRequest: {}", httpRequest.toString());
            //logger.error("uri: {}", httpRequest.uri());
//            logger.error("content: {}", httpRequest.content().toString());
//            logger.error("headers: {}", httpRequest.headers().toString());
//            logger.error("method: {}", httpRequest.method().toString());
//            logger.error("decoderResult: {}", httpRequest.decoderResult());
//            logger.error("protocolVersion: {}", httpRequest.protocolVersion());

            HttpTools.sendCorrectResp(ctx, httpRequest, "SSSSSSS");
//            Requester.requestRemote(ctx, httpRequest);
            //  handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {//如果是Websocket请求，则进行websocket操作
            // handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }

//        ctx.fireChannelRead(msg);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

        System.out.println("##channelReadComplete..." + count++ + ",ctx:" + ctx.hashCode());
        ctx.flush();
//        ctx.close();
        //ctx.fireChannelReadComplete();

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + "/ws/join";
        logger.info(location);
        if (NettyServer.SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    private String wsBeforeHandler(ChannelHandlerContext ctx, FullHttpRequest req) {
        String mac = StringTools.getMac(req.uri());
        if (mac == null) {
            HttpTools.sendCorrectResp(ctx, req, "PARAERROR");
            return null;
        } else {
            return mac;
        }
    }

}
