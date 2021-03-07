package com.jin.netty.demo01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception
    {
        // NioEventLoopGroup is a multithreaded event loop that handles I/O operation
        EventLoopGroup bossGroup = new NioEventLoopGroup();// The first one, often called 'boss', accepts an incoming connection.
        EventLoopGroup workerGroup = new NioEventLoopGroup();// The second one, often called 'worker', handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.

        try {
            // ServerBootstrap is a helper class that sets up a server. You can set up the server using a Channel directly. However, please note that this is a tedious process, and you do not need to do that in most cases.
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)// Here, we specify to use the NioServerSocketChannel class which is used to instantiate a new Channel to accept incoming connections.
                    .childHandler(new ChannelInitializer<SocketChannel>() {// The handler specified here will always be evaluated by a newly accepted Channel.
                        protected void initChannel(SocketChannel ch) throws Exception {
                           ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    // Did you notice option() and childOption()? option() is for the NioServerSocketChannel that accepts incoming connections. childOption() is for the Channels accepted by the parent ServerChannel, which is NioServerSocketChannel in this case.
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();


        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new DiscardServer(8080).run();
    }
}
