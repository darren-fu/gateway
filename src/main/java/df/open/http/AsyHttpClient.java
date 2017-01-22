package df.open.http;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.asynchttpclient.*;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by darrenfu on 17-1-17.
 */
public class AsyHttpClient {

    static AsyncHttpClient asyncHttpClient;
    static int count = 0;

    static LongAdder longAdder;


    static {
        longAdder = new LongAdder();
        DefaultAsyncHttpClientConfig configBuild = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(5000)
                .setRequestTimeout(2000)
                .setReadTimeout(2000)
//                .setIoThreadsCount(2)
                .build();
        asyncHttpClient = new DefaultAsyncHttpClient(configBuild);
        System.out.println(configBuild.getRequestTimeout());
    }

    public static void main(String[] args) {
        System.out.println(AsyHttpClient.longAdder.longValue());
    }

    public static void request(ChannelHandlerContext ctx, FullHttpRequest req) {
        long start = System.currentTimeMillis();
        BoundRequestBuilder boundRequestBuilder;
        Long count = longAdder.longValue();
        longAdder.increment();

        //偶数
        if ((count & 1) == 0) {
            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9600/");
        }else{
            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/");

        }
//        if (req.uri().contains("hello")) {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/hello");
//        } else {
//            boundRequestBuilder = asyncHttpClient.prepareGet("http://localhost:9500/");
//        }

//        Request finalRequest = boundRequestBuilder.build();
        boundRequestBuilder.execute(new AsyncCompletionHandler<Response>() {

            @Override
            public Response onCompleted(Response response) throws Exception {
                // Do something with the Response
                // ...
                long end = System.currentTimeMillis();
//                System.out.println("asyncHttpClient response : " + response);

                System.out.println(response.getUri() + " : async http end - now : " + (end - start) + " ms, count : " + count);
                response.getResponseBodyAsByteBuffer();

                HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
                httpResponse.setEntity(new ByteArrayEntity(response.getResponseBodyAsBytes(), ContentType.create("text/html", Charset.defaultCharset())));
                httpResponse.addHeader("Content-Length", "22");
                httpResponse.addHeader("Content-Type", "text/html; charset=utf-8");
//                System.out.println("http response : " + httpResponse);
                if (HttpUtil.isKeepAlive(req)) {
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
    }


}
