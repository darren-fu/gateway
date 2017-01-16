package df.open.core.config;

import df.open.core.encoder.HttpClientResponseEncoder;
import df.open.core.handler.CustomHttpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * author: fuliang
 * date: 2017/1/13
 */
@Configuration
public class ServerConfiguration {
    int bossCount = 1;
    int workerCount = 1;
    boolean keepAlive = true;

    @Autowired
    private CustomHttpHandler customHttpHandler;


    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossCount);
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    @Bean
    public Map<ChannelOption<?>, Object> channelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.SO_BACKLOG, 128);
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        return options;
    }

    @Bean
    public ServerBootstrap serverBootstrap() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())
//                                .addLast("respDecoder-reqEncoder", new HttpServerCodec())
                                .addLast("http-aggregator", new HttpObjectAggregator(65536))
                                .addLast("encoder", new HttpClientResponseEncoder<>())
                                .addLast("base-encoder", new HttpResponseEncoder())
                                .addLast(new ChunkedWriteHandler())
                                .addLast("action-handler", customHttpHandler);

//
//                        ch.pipeline().addLast("decoder", new HttpRequestDecoder());
//                        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
//                        ch.pipeline().addLast("encoder", new HttpResponseEncoder());
//                        ch.pipeline().addLast("chunkedWriter", new ChunkedWriteHandler());
                    }
                });
        Map<ChannelOption<?>, Object> channelOptions = channelOptions();
        Set<ChannelOption<?>> keySet = channelOptions.keySet();
        for (ChannelOption option : keySet) {
            b.option(option, channelOptions.get(option));
        }
        return b;
    }
}
