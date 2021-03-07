package com.jin.netty.chat.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author wu.jinqing
 * @date 2020年12月28日
 */
public class ChatChannelInitialiazer extends ChannelInitializer<SocketChannel> {
    // 自定义业务用单独的线程池查询，如果你的业务的Handler是无序的可以使用UnorderedThreadPoolEventExecutor(效率更高)代替DefaultEventExecutorGroup
    static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);
    public static final String WEBSOCKET_PATH = "/websocket";

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new WebSocketServerCompressionHandler());
        p.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, 65536, false, false, false, 10000L));
        p.addLast(new IdleStateHandler(30, 0, 0));
        p.addLast(new ChatHeartbeatChannelHandler());
        p.addLast(group, new ChatChannelHandler());
    }
}
