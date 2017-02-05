package df.open.http;

import df.open.core.remote.Requester;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
public class HttpClient {


    private static PoolingHttpClientConnectionManager cm;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";

    private static CloseableHttpClient httpClient;

    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(5000);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(2000);// 每路由最大连接数，默认值是2
//            cm.setMaxPerRoute(new HttpRoute(new HttpHost("localhost", 9500)), 1000);

        }
//        ConnectionConfig

//        SocketConfig socketConfig = SocketConfig.custom()
//                .setSoKeepAlive(true)
//                .setSoReuseAddress(true)
//                .build();
//        cm.setDefaultSocketConfig(socketConfig);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(100000)
                .build();
        cm.setDefaultConnectionConfig(connectionConfig);


//        cm.setValidateAfterInactivity();


    }

    static {
        init();
        httpClient = getHttpClient();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
//        HttpResponse result = HttpClient.requestWithGet("http://localhost:9500/hello");
//        HttpRoute route = new HttpRoute(new HttpHost("localhost", 9500));
//        ConnectionRequest connRequest = cm.requestConnection(route, null);
//        ConnectionRequest connRequest2 = cm.requestConnection(route, null);
//        HttpClientConnection httpClientConnection = connRequest.get(100, TimeUnit.SECONDS);
//        HttpClientConnection httpClientConnection2 = connRequest2.get(100, TimeUnit.SECONDS);


        MyRequestor myRequestor = new MyRequestor();
        MyRequestor myRequestor1 = new MyRequestor();
        MyRequestor myRequestor2 = new MyRequestor();
        MyRequestor myRequestor3 = new MyRequestor();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(myRequestor);
        executorService.submit(myRequestor1);
        executorService.submit(myRequestor2);
        executorService.submit(myRequestor3);


        Thread.sleep(1000);
        PoolStats totalStats = cm.getTotalStats();
        System.out.println("total-conns:" + totalStats);

//        System.out.println("result:" + result);

    }

    static class MyRequestor implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                requestWithGet("http://localhost:9500/hello");
            }

        }
    }


    private HttpClientConnection getClient() throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        HttpRoute route = new HttpRoute(new HttpHost("localhost", 9500));
        ConnectionRequest connRequest = cm.requestConnection(route, null);
        HttpClientConnection httpClientConnection = connRequest.get(100, TimeUnit.SECONDS);
        return httpClientConnection;
    }


    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .build();

        PoolStats totalStats = cm.getTotalStats();
        System.out.println("total-conns:" + totalStats);

        return HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new HttpRequestRetryHandler() {
                    @Override
                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                        return false;
                    }
                }).build();
    }

    /**
     * @param url
     * @return
     */
    public static String httpGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    public static HttpResponse requestWithGet(String url) {
        HttpGet httpGet = new HttpGet(url);
//        httpGet.completed();
        return geResponsetResult(httpGet);
    }


    private static CloseableHttpResponse geResponsetResult(HttpRequestBase request) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClient.httpClient;
        PoolStats totalStats = cm.getTotalStats();


        PoolStats cmStats = cm.getStats(new HttpRoute(new HttpHost("localhost", 9500)));
        System.out.printf("cmstates: " + cmStats);
        CloseableHttpResponse response = null;
        try {
            long http_start = System.currentTimeMillis();

            response = httpClient.execute(request);
            long http_end = System.currentTimeMillis();

            System.out.println("http_end - http_start : " + (http_end - http_start));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            request.releaseConnection();
        }

        return response;
    }


    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    public static String httpPostRequest(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPost);
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }

        return pairs;
    }


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            // response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                String result = EntityUtils.toString(entity);
                response.close();
                // httpClient.close();
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return EMPTY_STR;
    }

}
