package com.jin.netty.mywebsocket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final String WEBSOCKET_PATH = "/websocket";
    // 是否删除客户端发过来的PongWebSocketFrame
    public static final boolean DROP_PONG_FRAMES = false;

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new WebSocketServerCompressionHandler());
        p.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, 65536,false,  false ,DROP_PONG_FRAMES , 10000L));
        p.addLast(new IdleStateHandler(5, 0, 0));
        p.addLast(new MyWebSocketServerHeartBeatHandler());
        p.addLast(new MyWebSocketServerChannelHandler());
    }
}
