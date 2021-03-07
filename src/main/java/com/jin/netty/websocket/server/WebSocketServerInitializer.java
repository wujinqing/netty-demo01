package com.jin.netty.websocket.server;

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
 * @date 2020年12月23日
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final String WEBSOCKET_PATH = "/websocket";

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();


        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        // 处理通用的Close, Ping, Pong请求(frame)，并将Text和Binary请求(frame)传递给自定义的处理器处理。
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));// 里面会自动添加WebSocketServerProtocolHandshakeHandler
        pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
//        pipeline.addLast(new IdleStateHandler(3, 0, 0));
//        pipeline.addLast(new MyHeartBeatHandler());
        pipeline.addLast(new WebSocketFrameHandler());

    }
}
