package com.jin.netty.websocket.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.net.URI;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                new URI("ws://localhost:8080/websocket"), WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(WebSocketClientCompressionHandler.INSTANCE);
        p.addLast(new WebSocketClientHandler(handshaker));

    }
}
