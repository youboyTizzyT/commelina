package com.commelina.niosocket;

import com.commelina.niosocket.proto.SocketASK;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * netty nio socket server
 *
 * @author @panyao
 * @date 2017/8/3
 */
public class NettyNioSocketServer {

    private final Logger LOGGER = LoggerFactory.getLogger(NettyNioSocketServer.class);
    private Channel serverChannel;

    /**
     * 获取服务器运行的端口
     *
     * @return
     */
    public int getPort() {
        if (serverChannel == null) {
            return -1;
        }
        SocketAddress socketAddress = serverChannel.localAddress();
        if (!(socketAddress instanceof InetSocketAddress)) {
            return -1;
        }
        return ((InetSocketAddress) socketAddress).getPort();
    }

    /**
     * 绑定并启动 server ， accept 连接
     *
     * @param host
     * @param port
     * @param socketEventHandler
     * @throws IOException
     */
    public void bindAndStart(String host, int port, final SocketEventHandler socketEventHandler) throws IOException {
        // 管理线程
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 默认线程数是 cpu 核数的两倍
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        //server启动管理配置
        final ServerBootstrap boot = new ServerBootstrap();
        //  闲置事件
//        final ChannelAcceptorIdleStateTrigger trigger = new ChannelAcceptorIdleStateTrigger();
        boot.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //最大客户端连接数为 0x7fffffff
                .option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // http://blog.csdn.net/linuu/article/details/51360609
                        // protocol 协议

                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        ch.pipeline().addLast("decoder", new ProtobufDecoder(SocketASK.getDefaultInstance()));

                        ch.pipeline().addLast("fieldPrepender", new ProtobufVarint32LengthFieldPrepender());
                        ch.pipeline().addLast("encoder", new ProtobufEncoder());

                        // http://blog.csdn.net/z69183787/article/details/52625095
                        // 心跳检查 5s 检查一次
//                        ch.pipeline().addLast("heartbeatHandler", new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                        // 闲置事件
                        //ch.pipeline().addLast("heartbeatTrigger", trigger);

                        final ChannelInboundHandlerRouterAdapter routerAdapter = new ChannelInboundHandlerRouterAdapter();
                        routerAdapter.setHandlers(socketEventHandler);
                        ch.pipeline().addLast("routerAdapter", routerAdapter);
                    }
                });

        // Bind and start to accept incoming connections.
        ChannelFuture future = boot.bind(host, port);
        try {
            future.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for bindAndStart");
        }
        if (!future.isSuccess()) {
            throw new IOException("Failed to bindAndStart", future.cause());
        }
        LOGGER.info("listen port:{} started.", port);
        serverChannel = future.channel();
    }

    /**
     * 停止 socket server
     */
    public void shutdown() {
        if (serverChannel == null || !serverChannel.isOpen()) {
            // Already closed.
            return;
        }
        serverChannel.close().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}

