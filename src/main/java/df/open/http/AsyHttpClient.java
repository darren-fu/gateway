package df.open.http;

import df.open.core.handler.CustomHttpHandler;
import df.open.utils.HttpTools;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.asynchttpclient.*;
import org.asynchttpclient.channel.ChannelPool;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by darrenfu on 17-1-17.
 */
public class AsyHttpClient {

    static AsyncHttpClient asyncHttpClient;

    public static LongAdder totalAccessCount;
    public static LongAdder count9500;
    public static LongAdder time9500;
    public static LongAdder count9600;
    public static LongAdder time9600;


    static {
        totalAccessCount = new LongAdder();
        count9500 = new LongAdder();
        time9500 = new LongAdder();
        count9600 = new LongAdder();
        time9600 = new LongAdder();
        DefaultAsyncHttpClientConfig configBuild = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(5000)
                .setMaxConnectionsPerHost(100)
                .setValidateResponseHeaders(false)
//                .setChannelPool(ChannelPool)

//                .setRequestTimeout(2000)
//                .setReadTimeout(2000)
//                .setIoThreadsCount(2)
                .build();
        asyncHttpClient = new DefaultAsyncHttpClient(configBuild);
        System.out.println(configBuild.getRequestTimeout());
    }

//    public static void main(String[] args) {
//        System.out.println(AsyHttpClient.totalAccessCount.longValue());
//    }

    public static void request(ChannelHandlerContext ctx, FullHttpRequest req) {
        long start = System.currentTimeMillis();
        BoundRequestBuilder boundRequestBuilder;
//        Long count = totalAccessCount.longValue();
        Long count = CustomHttpHandler.count;
        if (count > 100000) {
            totalAccessCount.reset();
            count9500.reset();
            time9500.reset();
            count9600.reset();
            time9600.reset();
        }
//        totalAccessCount.increment();
//        Long c9500 = count9500.longValue();
//        Long c9600 = count9600.longValue();
        //
        int i = (int) (count % 3);
        if (i == 0) {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9600/");
            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/hello");
//            count9600.increment();
        } else if (i == 1) {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/");
            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/hello");
//            count9500.increment();

        } else {
            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/hello");
        }
//        if (req.uri().contains("hello")) {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/hello");
//        } else {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/");
//        }

//        Request finalRequest = boundRequestBuilder.build();
        System.out.println("Call service from:" + Thread.currentThread().getName());
        boundRequestBuilder.addHeader(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_JSON);
        boundRequestBuilder.execute(new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {
                // Do something with the Response
                // ...
                System.out.println("get response from:" + Thread.currentThread().getName());
                long end = System.currentTimeMillis();
//                System.out.println("asyncHttpClient response : " + response);
                if (response.getUri().getPort() == 9500) {
                    time9500.add(end - start);
                } else {
                    time9600.add(end - start);
                }
                System.out.println(response.getUri() + " : async http: " +
                                (end - start) + " ms, tcount : "
                                + count
//                        + ";c95:" + c9500 + ";c96: " + c9600
                                + ";5avg:" + (time9500.longValue() * 2 / count)
                                + ";6avg:" + (time9600.longValue() * 2 / count)
                );
                response.getResponseBodyAsByteBuffer();
                byte[] res = response.getResponseBodyAsBytes();
                HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
                httpResponse.setEntity(new ByteArrayEntity(res, ContentType.create("text/html", Charset.defaultCharset())));
                httpResponse.addHeader("Content-Length", String.valueOf(res.length));
                httpResponse.addHeader("Content-Type", "text/html; charset=utf-8");
//                System.out.println("http response : " + httpResponse);
                if (HttpTools.isKeepAlive(req)) {
                    System.out.println("isKeepAlive###");
                    httpResponse.addHeader("Connection", "keep-alive");
                    ctx.write(httpResponse);
                    ctx.flush();
                } else {
                    ctx.channel().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                }
                long end2 = System.currentTimeMillis();


                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                // Something wrong happened.
                System.out.println("出错了！！！！！！" + t.getMessage());
                t.printStackTrace();
            }
        });

//        Request request = new RequestBuilder()

    }


}
