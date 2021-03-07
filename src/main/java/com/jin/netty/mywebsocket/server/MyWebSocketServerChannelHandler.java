package com.jin.netty.mywebsocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketServerChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if(frame instanceof TextWebSocketFrame)
        {
            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
            String text = textFrame.text();

            System.out.println("收到来自客户端id: "+ctx.channel().id().asShortText()+", 的消息: " + text);
            ctx.writeAndFlush(new TextWebSocketFrame(text.toUpperCase()));
        }else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
}
