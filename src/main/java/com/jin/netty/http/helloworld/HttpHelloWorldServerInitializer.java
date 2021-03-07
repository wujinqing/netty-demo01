package com.jin.netty.http.helloworld;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.ssl.SslContext;

/**
 * @author wu.jinqing
 * @date 2020年12月21日
 */
public class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslContext;

    public HttpHelloWorldServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        if(sslContext != null)
        {
            p.addLast(sslContext.newHandler(ch.alloc()));
        }

        p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new HttpHelloWorldServerHandler());
    }
}
