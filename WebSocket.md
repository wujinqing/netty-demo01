## WebSocket

## 客户端

### 客户端握手
> 1.握手请求是基于http请求的。

> 2.发起握手请求的时候会自动添加WebSocket13FrameEncoder处理器。

> 3.收到握手响应会自动添加WebSocket13FrameDecoder处理器。

> 4.删除HttpContentDecompressor、HttpObjectAggregator、HttpResponseDecoder、HttpClientCodec、HttpRequestEncoder等处理器。

> 5.握手成功后，后续请求都是通过websocket进行通信。

### 客户端向服务端发起握手请求

```
    // 创建客户端握手处理器
    WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
                    
    // 发送握手请求
    handshaker.handshake(ctx.channel());
                   
```

### 根据不同的WebSocket协议版本创建不同的处理器(目前都是V13版本)

```
public static WebSocketClientHandshaker newHandshaker(
            URI webSocketURL, WebSocketVersion version, String subprotocol,
            boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength,
            boolean performMasking, boolean allowMaskMismatch, long forceCloseTimeoutMillis) {
        if (version == V13) {
            return new WebSocketClientHandshaker13(
                    webSocketURL, V13, subprotocol, allowExtensions, customHeaders,
                    maxFramePayloadLength, performMasking, allowMaskMismatch, forceCloseTimeoutMillis);
        }
        if (version == V08) {
            return new WebSocketClientHandshaker08(
                    webSocketURL, V08, subprotocol, allowExtensions, customHeaders,
                    maxFramePayloadLength, performMasking, allowMaskMismatch, forceCloseTimeoutMillis);
        }
        if (version == V07) {
            return new WebSocketClientHandshaker07(
                    webSocketURL, V07, subprotocol, allowExtensions, customHeaders,
                    maxFramePayloadLength, performMasking, allowMaskMismatch, forceCloseTimeoutMillis);
        }
        if (version == V00) {
            return new WebSocketClientHandshaker00(
                    webSocketURL, V00, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis);
        }

        throw new WebSocketClientHandshakeException("Protocol version " + version + " not supported.");
    }
```


### 创建握手请求对象

```
     * GET /chat HTTP/1.1
     * Host: server.example.com
     * Upgrade: websocket
     * Connection: Upgrade
     * Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
     * Origin: http://example.com
     * Sec-WebSocket-Protocol: chat, superchat
     * Sec-WebSocket-Version: 13
     
WebSocketClientHandshaker13.newHandshakeRequest()

protected FullHttpRequest newHandshakeRequest() {
        URI wsURL = uri();

        // Get 16 bit nonce and base 64 encode it
        byte[] nonce = WebSocketUtil.randomBytes(16);
        String key = WebSocketUtil.base64(nonce);

        String acceptSeed = key + MAGIC_GUID;
        byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        expectedChallengeResponseString = WebSocketUtil.base64(sha1);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "WebSocket version 13 client handshake key: {}, expected response: {}",
                    key, expectedChallengeResponseString);
        }

        // Format request
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, upgradeUrl(wsURL),
                Unpooled.EMPTY_BUFFER);
        HttpHeaders headers = request.headers();

        if (customHeaders != null) {
            headers.add(customHeaders);
            if (!headers.contains(HttpHeaderNames.HOST)) {
                // Only add HOST header if customHeaders did not contain it.
                //
                // See https://github.com/netty/netty/issues/10101
                headers.set(HttpHeaderNames.HOST, websocketHostValue(wsURL));
            }
        } else {
            headers.set(HttpHeaderNames.HOST, websocketHostValue(wsURL));
        }

        headers.set(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET)
               .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
               .set(HttpHeaderNames.SEC_WEBSOCKET_KEY, key);

        if (!headers.contains(HttpHeaderNames.ORIGIN)) {
            headers.set(HttpHeaderNames.ORIGIN, websocketOriginValue(wsURL));
        }

        String expectedSubprotocol = expectedSubprotocol();
        if (expectedSubprotocol != null && !expectedSubprotocol.isEmpty()) {
            headers.set(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, expectedSubprotocol);
        }

        headers.set(HttpHeaderNames.SEC_WEBSOCKET_VERSION, version().toAsciiString());
        return request;
    }

```

### 客户端发送握手请求
> 1.判断必须要存在HttpResponseDecoder或者HttpClientCodec处理器。

> 2.创建一个WebSocket13FrameEncoder对象插入到HttpRequestEncoder或者HttpClientCodec处理器的后面。

```
WebSocketClientHandshaker.handshake()

public final ChannelFuture handshake(Channel channel, final ChannelPromise promise) {
        ChannelPipeline pipeline = channel.pipeline();
        HttpResponseDecoder decoder = pipeline.get(HttpResponseDecoder.class);
        if (decoder == null) {
            HttpClientCodec codec = pipeline.get(HttpClientCodec.class);
            if (codec == null) {
               promise.setFailure(new IllegalStateException("ChannelPipeline does not contain " +
                       "an HttpResponseDecoder or HttpClientCodec"));
               return promise;
            }
        }

        FullHttpRequest request = newHandshakeRequest();

        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ChannelPipeline p = future.channel().pipeline();
                    ChannelHandlerContext ctx = p.context(HttpRequestEncoder.class);
                    if (ctx == null) {
                        ctx = p.context(HttpClientCodec.class);
                    }
                    if (ctx == null) {
                        promise.setFailure(new IllegalStateException("ChannelPipeline does not contain " +
                                "an HttpRequestEncoder or HttpClientCodec"));
                        return;
                    }
                    p.addAfter(ctx.name(), "ws-encoder", newWebSocketEncoder());

                    promise.setSuccess();
                } else {
                    promise.setFailure(future.cause());
                }
            }
        });
        return promise;
    }

```


### 客户端完成握手
> 1.对服务端的握手响应进行校验。

> 2.是否有HttpContentDecompressor处理器，有则删掉。

> 3.是否有HttpObjectAggregator处理器，有则删掉。

> 4.创建一个WebSocket13FrameDecoder对象插入到HttpResponseDecoder或者HttpClientCodec处理器的后面。

> 5.是否有HttpResponseDecoder处理器，有则删掉。

> 6.是否有HttpClientCodec处理器，有则删掉。

> 7.是否有HttpRequestEncoder处理器，有则删掉。



```
 public final void finishHandshake(Channel channel, FullHttpResponse response) {
        verify(response);

        // Verify the subprotocol that we received from the server.
        // This must be one of our expected subprotocols - or null/empty if we didn't want to speak a subprotocol
        String receivedProtocol = response.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        receivedProtocol = receivedProtocol != null ? receivedProtocol.trim() : null;
        String expectedProtocol = expectedSubprotocol != null ? expectedSubprotocol : "";
        boolean protocolValid = false;

        if (expectedProtocol.isEmpty() && receivedProtocol == null) {
            // No subprotocol required and none received
            protocolValid = true;
            setActualSubprotocol(expectedSubprotocol); // null or "" - we echo what the user requested
        } else if (!expectedProtocol.isEmpty() && receivedProtocol != null && !receivedProtocol.isEmpty()) {
            // We require a subprotocol and received one -> verify it
            for (String protocol : expectedProtocol.split(",")) {
                if (protocol.trim().equals(receivedProtocol)) {
                    protocolValid = true;
                    setActualSubprotocol(receivedProtocol);
                    break;
                }
            }
        } // else mixed cases - which are all errors

        if (!protocolValid) {
            throw new WebSocketClientHandshakeException(String.format(
                    "Invalid subprotocol. Actual: %s. Expected one of: %s",
                    receivedProtocol, expectedSubprotocol), response);
        }

        setHandshakeComplete();

        final ChannelPipeline p = channel.pipeline();
        // Remove decompressor from pipeline if its in use
        HttpContentDecompressor decompressor = p.get(HttpContentDecompressor.class);
        if (decompressor != null) {
            p.remove(decompressor);
        }

        // Remove aggregator if present before
        HttpObjectAggregator aggregator = p.get(HttpObjectAggregator.class);
        if (aggregator != null) {
            p.remove(aggregator);
        }

        ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
        if (ctx == null) {
            ctx = p.context(HttpClientCodec.class);
            if (ctx == null) {
                throw new IllegalStateException("ChannelPipeline does not contain " +
                        "an HttpRequestEncoder or HttpClientCodec");
            }
            final HttpClientCodec codec =  (HttpClientCodec) ctx.handler();
            // Remove the encoder part of the codec as the user may start writing frames after this method returns.
            codec.removeOutboundHandler();

            p.addAfter(ctx.name(), "ws-decoder", newWebsocketDecoder());

            // Delay the removal of the decoder so the user can setup the pipeline if needed to handle
            // WebSocketFrame messages.
            // See https://github.com/netty/netty/issues/4533
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    p.remove(codec);
                }
            });
        } else {
            if (p.get(HttpRequestEncoder.class) != null) {
                // Remove the encoder part of the codec as the user may start writing frames after this method returns.
                p.remove(HttpRequestEncoder.class);
            }
            final ChannelHandlerContext context = ctx;
            p.addAfter(context.name(), "ws-decoder", newWebsocketDecoder());

            // Delay the removal of the decoder so the user can setup the pipeline if needed to handle
            // WebSocketFrame messages.
            // See https://github.com/netty/netty/issues/4533
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    p.remove(context.handler());
                }
            });
        }
    }
```


## 服务端


> 1.在ChannelInitializer中会添加WebSocketServerProtocolHandler处理器。

> 2.在WebSocketServerProtocolHandler处理器的handlerAdded方法中会在它前面添加WebSocketServerProtocolHandshakeHandler处理器，这个处理器是用来处理客户端发来的基于http的握手请求的。


### 处理收到的握手请求
> 1.请求路径必须与websocketPath配置相同，如果checkStartsWith配置是true也可以以websocketPath开头。

> 2.握手请求只能是GET请求。

> 3.创建WebSocketServerHandshaker13对象。

> 4.将上面创建的WebSocketServerHandshaker13对象关联到当前Channel的HANDSHAKER_ATTR_KEY属性上。

> 5.删除当前处理器(WebSocketServerProtocolHandshakeHandler)。

> 6.返回握手响应。

> 7.通过fireUserEventTriggered发送HANDSHAKE_COMPLETE握手完成事件。

```
io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandshakeHandler.channelRead()

public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final FullHttpRequest req = (FullHttpRequest) msg;
        if (!isWebSocketPath(req)) {
            ctx.fireChannelRead(msg);
            return;
        }

        try {
            if (!GET.equals(req.method())) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, ctx.alloc().buffer(0)));
                return;
            }

            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(ctx.pipeline(), req, serverConfig.websocketPath()),
                    serverConfig.subprotocols(), serverConfig.decoderConfig());
            final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            final ChannelPromise localHandshakePromise = handshakePromise;
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                // Ensure we set the handshaker and replace this handler before we
                // trigger the actual handshake. Otherwise we may receive websocket bytes in this handler
                // before we had a chance to replace it.
                //
                // See https://github.com/netty/netty/issues/9471.
                WebSocketServerProtocolHandler.setHandshaker(ctx.channel(), handshaker);
                ctx.pipeline().remove(this);

                final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
                handshakeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (!future.isSuccess()) {
                            localHandshakePromise.tryFailure(future.cause());
                            ctx.fireExceptionCaught(future.cause());
                        } else {
                            localHandshakePromise.trySuccess();
                            // Kept for compatibility
                            ctx.fireUserEventTriggered(
                                    WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
                            ctx.fireUserEventTriggered(
                                    new WebSocketServerProtocolHandler.HandshakeComplete(
                                            req.uri(), req.headers(), handshaker.selectedSubprotocol()));
                        }
                    }
                });
                applyHandshakeTimeout();
            }
        } finally {
            req.release();
        }
    }
```

### 处理握手

> 1.校验握手请求。

> 2.创建握手响应。

> 3.是否有HttpObjectAggregator处理器，有则删掉。

> 4.是否有HttpContentCompressor处理器，有则删掉。

> 5.是否存在HttpRequestDecoder处理器，如果存在删除并替换成WebSocket13FrameDecoder处理器; 并在HttpResponseEncoder处理器前面添加WebSocket13FrameEncoder。

> 6.如果不存在HttpRequestDecoder处理器，则判断是否存在HttpServerCodec，如果存在，则在它前面添加WebSocket13FrameDecoder和WebSocket13FrameEncoder处理器，不存在就报错。

> 7.发送握手响应。

> 8.响应发送成功后删除HttpResponseEncoder或者HttpServerCodec处理器。

```
WebSocketServerHandshaker.handshake()

public final ChannelFuture handshake(Channel channel, FullHttpRequest req,
                                            HttpHeaders responseHeaders, final ChannelPromise promise) {

        if (logger.isDebugEnabled()) {
            logger.debug("{} WebSocket version {} server handshake", channel, version());
        }
        FullHttpResponse response = newHandshakeResponse(req, responseHeaders);
        ChannelPipeline p = channel.pipeline();
        if (p.get(HttpObjectAggregator.class) != null) {
            p.remove(HttpObjectAggregator.class);
        }
        if (p.get(HttpContentCompressor.class) != null) {
            p.remove(HttpContentCompressor.class);
        }
        ChannelHandlerContext ctx = p.context(HttpRequestDecoder.class);
        final String encoderName;
        if (ctx == null) {
            // this means the user use an HttpServerCodec
            ctx = p.context(HttpServerCodec.class);
            if (ctx == null) {
                promise.setFailure(
                        new IllegalStateException("No HttpDecoder and no HttpServerCodec in the pipeline"));
                return promise;
            }
            p.addBefore(ctx.name(), "wsencoder", newWebSocketEncoder());
            p.addBefore(ctx.name(), "wsdecoder", newWebsocketDecoder());
            encoderName = ctx.name();
        } else {
            p.replace(ctx.name(), "wsdecoder", newWebsocketDecoder());

            encoderName = p.context(HttpResponseEncoder.class).name();
            p.addBefore(encoderName, "wsencoder", newWebSocketEncoder());
        }
        channel.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ChannelPipeline p = future.channel().pipeline();
                    p.remove(encoderName);
                    promise.setSuccess();
                } else {
                    promise.setFailure(future.cause());
                }
            }
        });
        return promise;
    }
```

### 握手请求和响应

```
Browser request to the server:
       GET /chat HTTP/1.1
       Host: server.example.com
       Upgrade: websocket
       Connection: Upgrade
       Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
       Origin: http://example.com
       Sec-WebSocket-Protocol: chat, superchat
       Sec-WebSocket-Version: 13
       
Server response:
       HTTP/1.1 101 Switching Protocols
       Upgrade: websocket
       Connection: Upgrade
       Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
       Sec-WebSocket-Protocol: chat
```

### 创建握手响应

```
protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers) {
        CharSequence key = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_KEY);
        if (key == null) {
            throw new WebSocketServerHandshakeException("not a WebSocket request: missing key", req);
        }

        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS,
                req.content().alloc().buffer(0));
        if (headers != null) {
            res.headers().add(headers);
        }

        String acceptSeed = key + WEBSOCKET_13_ACCEPT_GUID;
        byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        String accept = WebSocketUtil.base64(sha1);

        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket version 13 server handshake key: {}, response: {}", key, accept);
        }

        res.headers().set(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET)
                     .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
                     .set(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT, accept);

        String subprotocols = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        if (subprotocols != null) {
            String selectedSubprotocol = selectSubprotocol(subprotocols);
            if (selectedSubprotocol == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Requested subprotocol(s) not supported: {}", subprotocols);
                }
            } else {
                res.headers().set(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, selectedSubprotocol);
            }
        }
        return res;
    }

```


















































































































































































































































































































































