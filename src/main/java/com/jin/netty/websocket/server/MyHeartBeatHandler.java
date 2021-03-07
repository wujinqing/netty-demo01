package com.jin.netty.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author wu.jinqing
 * @date 2020年12月23日
 */
public class MyHeartBeatHandler extends SimpleChannelInboundHandler<Object> {
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof PingWebSocketFrame)
        {
            System.out.println("WebSocket Client received pong");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent)
        {
            IdleStateEvent e = (IdleStateEvent) evt;

            if(e.state() == IdleState.READER_IDLE)
            {
                ctx.writeAndFlush(new PingWebSocketFrame());
            }else if(e.state() == IdleState.WRITER_IDLE)
            {

            }else if(e.state() == IdleState.ALL_IDLE)
            {

            }
        }
    }
}
