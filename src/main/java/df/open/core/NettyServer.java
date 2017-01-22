package df.open.core;

import io.netty.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * author: fuliang
 * date: 2017/1/13
 */
@Component
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static final boolean SSL = System.getProperty("ssl") != null;


    @Autowired
    private ServerBootstrap serverBootstrap;

    int port = 9100;


    @PostConstruct
    public void startServer() {
        logger.info("Starting server at " + port);
        try {
            serverBootstrap.bind(port).sync().channel().closeFuture().sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
