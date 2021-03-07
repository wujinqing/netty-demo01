package com.jin.netty.demo01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author wu.jinqing
 * @date 2020年12月15日
 */
public class TimeDecoder extends ByteToMessageDecoder {
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4)
        {
            return;
        }

        // Please remember that you don't need to decode multiple messages. ByteToMessageDecoder will keep calling the decode() method until it adds nothing to out.
        out.add(in.readBytes(4));
    }
}
