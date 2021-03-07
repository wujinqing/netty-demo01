package com.jin.netty.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Locale;

/**
 * @author wu.jinqing
 * @date 2020年12月23日
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ChannelId id = ((NioSocketChannel)ctx.channel()).id();
        System.out.println("id: " + id.asShortText());
        System.out.println("channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelId id = ((NioSocketChannel)ctx.channel()).id();
        System.out.println("id: " + id.asShortText());
        System.out.println("channelActive");
        super.channelActive(ctx);
    }

    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        ChannelId id = ((NioSocketChannel)ctx.channel()).id();
        System.out.println("id: " + id.asShortText());
        // ping and pong frames already handled
        if(frame instanceof TextWebSocketFrame)
        {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame)frame).text();
            ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase()));
        }else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        super.exceptionCaught(ctx, cause);
    }
}
