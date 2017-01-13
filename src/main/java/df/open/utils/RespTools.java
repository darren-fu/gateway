package df.open.utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by jcala on 2016/5/4
 */
public class RespTools {

    public static void paraErrorBack(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "PARA_ERROR");
        get = null;
    }

    public static void operateErr(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "OPERATION_ERROR");
        get = null;
    }

    public static void invalidName(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "INVALID_NAME");
        get = null;
    }

    public static void offLone(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "OFFLONE");
        get = null;
    }

    public static void success(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "SUCCESS");
        get = null;
    }

    public static void needLogin(ChannelHandlerContext ctx, FullHttpRequest req, Object get) {
        HttpTools.sendCorrectResp(ctx, req, "SUCCESS");
        get = null;
    }
}
