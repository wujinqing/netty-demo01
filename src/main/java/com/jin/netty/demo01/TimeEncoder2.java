package com.jin.netty.demo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class TimeEncoder2 extends MessageToByteEncoder<UnixTime> {
    protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) throws Exception {
        out.writeInt((int)msg.value());
    }
}
