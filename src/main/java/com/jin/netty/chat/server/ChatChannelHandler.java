package com.jin.netty.chat.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author wu.jinqing
 * @date 2020年12月28日
 */
public class ChatChannelHandler extends SimpleChannelInboundHandler<Object> {
    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String id = channelId(ctx);
        String msg = "用户id:(" +id+")已上线。";
        System.out.println(msg);
        TextWebSocketFrame text = new TextWebSocketFrame(msg);
        group.writeAndFlush(text, ChannelMatchers.isNot(ctx.channel()));

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String id = channelId(ctx);
        String msg = "用户id:(" +id+")已下线。";
        System.out.println(msg);
        TextWebSocketFrame text = new TextWebSocketFrame(msg);
        group.writeAndFlush(text, ChannelMatchers.isNot(ctx.channel()));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        group.add(ctx.channel());
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof TextWebSocketFrame)
        {
            TextWebSocketFrame text = ((TextWebSocketFrame) msg).retainedDuplicate();

            group.writeAndFlush(text, ChannelMatchers.isNot(ctx.channel()));

        }else {
            System.out.println("错误的信息类型");
            throw new UnsupportedOperationException();
        }
    }

    private String channelId(ChannelHandlerContext ctx)
    {
        return ctx.channel().id().asShortText();
    }
}
