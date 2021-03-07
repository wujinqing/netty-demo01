package com.jin.netty.mywebsocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketClientHeartBeatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(msg instanceof PingWebSocketFrame)// 如果是服务器端发来的ping，则响应一个pong消息
        {
            System.out.println("收到服务器发来的心跳检测。");
            ctx.writeAndFlush(new PongWebSocketFrame());
        }else if(msg instanceof CloseWebSocketFrame){// 如果收到服务器发来的关闭连接的请求，则关闭连接
            ctx.channel().close();
        } else {// 其他类型的消息，让后续的处理器处理
            ctx.fireChannelRead(msg);
        }
    }
}
