package com.jin.netty.mywebsocket.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.net.URI;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    MyWebSocketClientHandshakeHandler handshakeHandler;

    public MyWebSocketClientChannelInitializer(MyWebSocketClientHandshakeHandler handshakeHandler) {
        this.handshakeHandler = handshakeHandler;
    }


    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(WebSocketClientCompressionHandler.INSTANCE);
        p.addLast(handshakeHandler);
        p.addLast(new MyWebSocketClientHeartBeatHandler());
        p.addLast(new MyWebSocketClientChannelHandler());
    }
}
