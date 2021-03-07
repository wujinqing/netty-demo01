package com.jin.netty.demo01;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.write(msg);
        ctx.flush();// 消息不会正真的写， 会被缓存起来， 需要调用flush方法

        /*
        1.Please note that we did not release the received message unlike we did in the DISCARD example. It is because Netty releases it for you when it is written out to the wire.

        2.ctx.write(Object) does not make the message written out to the wire. It is buffered internally and then flushed out to the wire by ctx.flush(). Alternatively, you could call ctx.writeAndFlush(msg) for brevity.

         */
        // 这里不需要release，因为write方法会执行release
//        ((ByteBuf)msg).release();
    }
}
