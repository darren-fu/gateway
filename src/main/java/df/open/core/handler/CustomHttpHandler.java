package df.open.core.handler;

import df.open.core.NettyServer;
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



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {//如果是HTTP请求，进行HTTP操作

            FullHttpRequest httpRequest = (FullHttpRequest) msg;

            logger.error("httpRequest: {}",httpRequest.toString());
            logger.error("uri: {}",httpRequest.uri());
            logger.error("content: {}",httpRequest.content().toString());
            logger.error("headers: {}",httpRequest.headers().toString());
            logger.error("method: {}",httpRequest.method().toString());
            logger.error("decoderResult: {}",httpRequest.decoderResult());
            logger.error("protocolVersion: {}",httpRequest.protocolVersion());

            HttpTools.sendCorrectResp(ctx, httpRequest, "SSSSSSS");
          //  handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {//如果是Websocket请求，则进行websocket操作
           // handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }


    public void messageReceived(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("##channelReadComplete...");
        ctx.flush();
        ctx.fireChannelReadComplete();

    }

    //处理HTTP的代码
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        logger.warn("uri:" + req.uri());
        if (req.uri().startsWith("/ws/join")) {//如果urL开头为/ws/join则升级为websocket
            mac = wsBeforeHandler(ctx, req);
            if (mac == null || mac.length() < 1) {
                RespTools.paraErrorBack(ctx, req, null);
                return;
            }
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        } else {
//            RouteResult<Action> routeResult = rs.getRouter().route(req.method(), req.uri());
//            Action action = routeResult.target();
//            action.act(ctx, req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            String json = ((TextWebSocketFrame) frame).text();
            return;
        }
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
