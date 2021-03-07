package com.jin.netty.demo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class TimeClientHandler2 extends ChannelInboundHandlerAdapter {
    private ByteBuf buf;

    // A ChannelHandler has two life cycle listener methods: handlerAdded() and handlerRemoved(). You can perform an arbitrary (de)initialization task as long as it does not block for a long time.
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        buf = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buf.release();
        buf = null;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf)msg;

        buf.writeBytes(m);
        m.release();

        if(buf.readableBytes() >= 4)
        {
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }
}
