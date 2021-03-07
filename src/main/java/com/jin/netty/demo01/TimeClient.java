package com.jin.netty.demo01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();// If you specify only one EventLoopGroup, it will be used both as a boss group and as a worker group. The boss worker is not used for the client side though.

        try {
            Bootstrap b = new Bootstrap();

            b.group(workerGroup);
            b.channel(NioSocketChannel.class);// Instead of NioServerSocketChannel, NioSocketChannel is being used to create a client-side Channel.
            b.option(ChannelOption.SO_KEEPALIVE, true);// Note that we do not use childOption() here unlike we did with ServerBootstrap because the client-side SocketChannel does not have a parent.
            b.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new TimeClientHandler());
                    ch.pipeline().addLast(new TimeDecoder2(), new TimeClientHandler3());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }

    }
}
