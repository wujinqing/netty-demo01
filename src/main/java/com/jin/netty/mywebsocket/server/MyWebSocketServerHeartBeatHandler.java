package com.jin.netty.mywebsocket.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketServerHeartBeatHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 当某个链接超过MAX_IDLE_TIME_SECONDS没发消息时，关闭这个连接
     */
    public static final AttributeKey<Long> MAX_IDLE_TIME_SECONDS_KEY = AttributeKey.valueOf(Long.class, "MAX_IDLE_TIME_SECONDS_KEY");
    public static final long MAX_IDLE_TIME_SECONDS = 10 * 1000;

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof PongWebSocketFrame)
        {
            System.out.println("收到客户端的心跳检测响应。");
            Channel ch = ctx.channel();
            ch.attr(MAX_IDLE_TIME_SECONDS_KEY).set(System.currentTimeMillis());
        }else {
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent)
        {
            IdleStateEvent e = (IdleStateEvent)evt;
            Channel ch = ctx.channel();
            if(e.state() == IdleState.READER_IDLE)
            {
                if(ch.hasAttr(MAX_IDLE_TIME_SECONDS_KEY))
                {
                    long last = ch.attr(MAX_IDLE_TIME_SECONDS_KEY).get();
                    long now = System.currentTimeMillis();
                    if((now - last) > MAX_IDLE_TIME_SECONDS)
                    {
                        System.out.println("关闭客户端id: "+ctx.channel().id().asShortText()+"。");
                        ch.close();
                    }
                }else {
                    ch.attr(MAX_IDLE_TIME_SECONDS_KEY).set(System.currentTimeMillis());
                }

                System.out.println("心跳检测：读空闲");
                ctx.writeAndFlush(new PingWebSocketFrame());
            }


        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常");

        if(cause instanceof IOException)
        {
            IOException ioException = (IOException)cause;

            // 远程主机强迫关闭了一个现有的连接。
            String msg = ioException.getMessage();
            System.out.println(msg);
        }else {
            cause.printStackTrace();
        }

        ctx.channel().close();
    }
}
