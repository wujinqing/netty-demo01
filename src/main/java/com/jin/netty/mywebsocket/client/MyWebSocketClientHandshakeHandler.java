package com.jin.netty.mywebsocket.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;

/**
 * @author wu.jinqing
 * @date 2020年12月25日
 */
public class MyWebSocketClientHandshakeHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public MyWebSocketClientHandshakeHandler(WebSocketClientHandshaker handshaker)
    {
        this.handshaker = handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if(!handshaker.isHandshakeComplete())// 还没握手成功，先处理握手成功
        {
            try{
                handshaker.finishHandshake(ch, (FullHttpResponse)msg);
                System.out.println("WebSocket连接建立成功！");
                handshakeFuture.setSuccess();
            }catch (WebSocketHandshakeException e)
            {
                System.out.println("WebSocket连接建立失败！");
                handshakeFuture.setFailure(e);
            }
        }else {// 握手成功，让后续的处理器来处理消息
            ctx.fireChannelRead(msg);
        }
    }
}
