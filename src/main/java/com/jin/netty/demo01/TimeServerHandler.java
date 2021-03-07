package com.jin.netty.demo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        // 和java标准的ByteBuffer不同netty的ByteBuf在读和写之间切换不需要调用flip()方法，因为ByteBuf does not have such a method because it has two pointers; one for read operations and the other for write operations.
        final ChannelFuture f = ctx.writeAndFlush(time);

        /*
        A ChannelFuture represents an I/O operation which has not yet occurred. It means, any requested operation might not have been performed yet because all operations are asynchronous in Netty. For example, the following code might close the connection even before a message is sent:

        Channel ch = ...;
        ch.writeAndFlush(message);
        ch.close();// 在netty中所有的操作都是异步的，ch.close()这个操作可能导致消息在发送到客户端之前连接就被关闭了。

         */
        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                assert f == future;

                ctx.close();// 这个操作也是异步的，不会直接关闭，close() also might not close the connection immediately, and it returns a ChannelFuture
            }
        });

//        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
