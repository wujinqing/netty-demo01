package com.jin.netty.mywebsocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketClient {
    public static void main(String[] args) throws Exception {
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                new URI("ws://localhost:8080/websocket"), WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        MyWebSocketClientHandshakeHandler handshakeHandler = new MyWebSocketClientHandshakeHandler(handshaker);

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new MyWebSocketClientChannelInitializer(handshakeHandler));

            Channel ch = b.connect("localhost", 8080).sync().channel();

            // 等待握手成功
            handshakeHandler.handshakeFuture().sync();

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                } else if ("bye".equals(msg.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                }else {
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    ch.writeAndFlush(frame);
                }
            }

        }finally {
            group.shutdownGracefully();
        }
    }
}
