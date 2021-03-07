package com.jin.netty.mywebsocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketClientChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(msg instanceof TextWebSocketFrame)
        {
            TextWebSocketFrame textFrame = (TextWebSocketFrame)msg;

            String text = textFrame.text();

            System.out.println("收到来自服务器的信息:" + text);
        }
    }
}
