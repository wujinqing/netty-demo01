package com.jin.netty.chat.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * @author wu.jinqing
 * @date 2020年12月28日
 */
public class ChatHeartbeatChannelHandler extends SimpleChannelInboundHandler<Object> {
    // 存储连接空闲时间间隔，当连接超过指定时间间隔没有任何操作，
    public static final AttributeKey<Long> CHANNEL_IDLE_KEY = AttributeKey.valueOf(Long.class, "");

    /**
     * 当一个连接超过60秒没有和服务器有任何交互操作，则删除这个连接
     */
    public static final long MAX_IDLE_MS = 60000;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent)
        {
            IdleStateEvent e = (IdleStateEvent)evt;

            // 读空闲
            if(e.state() == IdleState.READER_IDLE)
            {
                ctx.writeAndFlush(new PingWebSocketFrame());

                if(ctx.channel().hasAttr(CHANNEL_IDLE_KEY))
                {
                    long  last = ctx.channel().attr(CHANNEL_IDLE_KEY).get();
                    long now = System.currentTimeMillis();

                    if((now - last) > MAX_IDLE_MS)
                    {
                        System.out.println("关闭一个连接id:(" + ctx.channel().id().asShortText() + ").");
                        ctx.writeAndFlush(new CloseWebSocketFrame());
                        ctx.channel().close();
                    }else {
                        ctx.channel().attr(CHANNEL_IDLE_KEY).set(System.currentTimeMillis());
                    }
                }else {
                    ctx.channel().attr(CHANNEL_IDLE_KEY).set(System.currentTimeMillis());
                }
            }
        }else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof CloseWebSocketFrame)
        {
            String id = ctx.channel().id().asShortText();

            System.out.println("连接id: " + id + ", 关闭。");
            ctx.close();
        }else if(msg instanceof PingWebSocketFrame)
        {
            ctx.writeAndFlush(new PongWebSocketFrame());
        }else if(msg instanceof PongWebSocketFrame)
        {
            ctx.channel().attr(CHANNEL_IDLE_KEY).set(System.currentTimeMillis());
        }else {
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
        }
    }
}
