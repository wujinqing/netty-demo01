package com.jin.netty.demo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("hello");
//        // Discard the received data silently.
//        ((ByteBuf)msg).release();

        ByteBuf in = (ByteBuf)msg;

        try {
//            while (in.isReadable())
//            {
//                System.out.print((char)in.readByte());
//                System.out.flush();
//            }

            System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII));
        }finally {
//            ReferenceCountUtil.refCnt(msg);
            in.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
