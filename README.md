## Netty

### NioServerSocketChannel初始化流程
> 1.根据默认的SelectorProvider生成ServerSocketChannel。

> 2.设置parent为null。

> 3.设置默认的ChannelId id。

> 4.设置Unsafe unsafe。

> 5.设置DefaultChannelPipeline pipeline。

> 6.将ServerSocketChannel赋值给SelectableChannel ch。

> 7.将感兴趣的事件SelectionKey.OP_ACCEPT赋值给readInterestOp。

> 8.设置ch.configureBlocking(false)。

> 9.初始化ServerSocketChannelConfig config为NioServerSocketChannelConfig。

### ServerBootstrap.bind()方法执行流程

> 1.通过b.channel(NioServerSocketChannel.class)设置的class对象通过反射生成NioServerSocketChannel对象。

> 2.将b.option()和b.attr()的配置设置到NioServerSocketChannel对象上。

> 3.手动生成一个ChannelInitializer对象，然后1.将b.handler(new LoggingHandler(LogLevel.INFO))的handler添加到ChannelInitializer的pipeline中，2.生成一个ServerBootstrapAcceptor对象添加到ChannelInitializer的pipeline中，这些handler最终将会添加到NioServerSocketChannel的pipeline中(handler会被封装成ChannelHandlerContext对象添加到Channel的pipeline中)。

> 4.将ChannelInitializer对象添加到NioServerSocketChannel的pipeline上。

> 5.从EventLoopGroup bossGroup中取出一个NioEventLoop并用它的Selector unwrappedSelector将NioServerSocketChannel注册到Selector上。

> 6.注册完之后会调用DefaultChannelPipeline.invokeHandlerAddedIfNeeded()方法，执行初始化ChannelInitializer.initChannel()方法，并且会将ChannelInitializer对象从pipeline中删除。

> 7.调用NioServerSocketChannel.doBind()方法完成端口号绑定操作。


### ServerBootstrapAcceptor 接收器的初始化(它是一个ChannelInboundHandler)

> 1.是在ServerBootstrap.bind()方法中初始化并添加到NioServerSocketChannel中的。

> 2.初始化时会将NioServerSocketChannel, EventLoopGroup workerGroup, b.childHandler(new HttpHelloWorldServerInitializer(sslContext)), b.childOption(), b.childAttr()作为构造方法的参数传入。

> 3.在ServerBootstrapAcceptor的channelRead()方法中会将b.childHandler()添加到NioSocketChannel的pipeline中(handler会被封装成ChannelHandlerContext对象添加到Channel的pipeline中)。

> 4.从EventLoopGroup workerGroup中取出一个NioEventLoop并用它的Selector unwrappedSelector将NioSocketChannel注册到Selector上。

> 5.注册完之后会调用DefaultChannelPipeline.invokeHandlerAddedIfNeeded()方法，执行初始化ChannelInitializer.initChannel()方法，并且会将ChannelInitializer对象从pipeline中删除。


### ServerBootstrapAcceptor 接收器的的作用
> 将bossGroup的NioServerSocketChannel里面接收到的客户端连接NioSocketChannel注册到workerGroup中。

> 通过workerGroup中的NioEventLoop的Selector unwrappedSelector将NioSocketChannel注册到Selector上。

### EventLoopGroup事件循环组的初始化流程
> 1.根据构造方法指定的线程数量创建EventExecutor[] children数组用来存放执行器(即: NioEventLoop)。

> 2.调用NioEventLoopGroup.newChild()方法生成NioEventLoop对象，并放到children数组中。



### NioEventLoop事件循环
> 1.它是java.util.concurrent.Executor的子类。

> 2.它保存了Selector unwrappedSelector，用来注册NioServerSocketChannel和NioSocketChannel及相关的感兴趣事件。

> 3.它提供的NioEventLoop.run()方法里面死循环执行selector.selectNow()等待感兴趣的事件到来。

### 服务器：NioServerSocketChannel的NioEventLoop.run()方法执行逻辑

> 1.执行selector.selectNow()等待事件到来。

> 2.当接收到SelectionKey.OP_ACCEPT事件时，调用NioServerSocketChannel的NioMessageUnsafe unsafe对象的read()方法。

> 3.unsafe对象的read()方法会调用NioServerSocketChannel的doReadMessages()方法并创建一个代表客户端连接的NioSocketChannel对象。

> 4.调用ChannelPipeline.fireChannelRead(Object msg)方法，它从head处理器(handler被封装成了ChannelHandlerContext)开始调用自己的ChannelInboundHandler.channelRead()方法，并且可以通过ctx.fireChannelRead(msg)方法调用处理器链中的下一个处理器。

> 5.最终会执行到ServerBootstrapAcceptor处理器，它会将b.childHandler()设置到前面创建NioSocketChannel对象上，并把它注册到EventLoopGroup workerGroup上。

> 6.最后执行pipeline.fireChannelReadComplete()方法。


### 客户端连接：NioSocketChannel的NioEventLoop.run()方法执行逻辑

> 1.执行selector.selectNow()等待事件到来。

> 2.当接收到SelectionKey.OP_READ事件时，调用NioSocketChannel的NioByteUnsafe unsafe对象的read()方法。

> 3.unsafe对象的read()方法会调用NioSocketChannel的doReadBytes()方法将请求数据读到ByteBuf中。

> 4.将读到的数据通过pipeline.fireChannelRead(byteBuf)传递给处理器链，它从head处理器(handler被封装成了ChannelHandlerContext)开始调用自己的ChannelInboundHandler.channelRead()方法，并且可以通过ctx.fireChannelRead(msg)方法调用处理器链中的下一个处理器。

> 5.ByteBuf数据最终会经过我们自己添加的一个一个的Handler中。

> 6.最后执行pipeline.fireChannelReadComplete()方法。


### IdleStateHandler
> IdleStateHandler里面的定时任务默认是使用EventLoopGroup workerGroup里面的IO线程来执行的，当任务量很大的时候会影响IO线程。

```
ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }
```



### ChannelPipeline的HeadContext既是ChannelHandlerContext，也是ChannelInboundHandler同时还是ChannelOutboundHandler

### ChannelPipeline的TailContext既是ChannelHandlerContext，也是ChannelInboundHandler。

### ChannelHandlerContext的fireXXX方法与ChannelPipeline的fireXXX方法的区别
> ChannelHandlerContext的fireXXX方法从下一个ChannelHandler开始，ChannelPipeline的fireXXX方法从head开始。


> https://netty.io/4.1/xref/overview-summary.html




|||
|---|---|
|channelRead()|This method is called with the received message, whenever new data is received from a client. |
|channelActive()|the channelActive() method will be invoked when a connection is established and ready to generate traffic|
|exceptionCaught()|The exceptionCaught() event handler method is called with a Throwable when an exception was raised by Netty due to an I/O error or by a handler implementation due to the exception thrown while processing events. In most cases, the caught exception should be logged and its associated channel should be closed here, although the implementation of this method can be different depending on what you want to do to deal with an exceptional situation. For example, you might want to send a response message with an error code before closing the connection.|
|||
|||

### Handler方法调用时机

|方法|调用时机|
|---|---|
|handlerAdded()|当有新连接注册时, ChannelInitializer的initChannel方法里面执行pipeline.addLast()方法其内部会调用handlerAdded()方法|
|channelRegistered()|当新连接注册完成后，会调用pipeline.fireChannelRegistered()方法, AbstractChannel.AbstractUnsafe.register0()|
|channelActive()|在pipeline.fireChannelRegistered()之后紧接着会调用pipeline.fireChannelActive()方法|
|channelRead|当Selector监听到SelectionKey.OP_READ事件时触发|
|channelReadComplete|读取操作完成触发|
|exceptionCaught|当handler发生异常时触发|
|channelInactive()|当channel触发deregister时会先调用pipeline.fireChannelInactive()，再调用pipeline.fireChannelUnregistered()|
|channelUnregistered|当channel触发deregister时会先调用pipeline.fireChannelInactive()，再调用pipeline.fireChannelUnregistered()|



## ChannelPipeline

### Inbound event propagation methods(入站事件传播方法)
> 执行顺序: ChannelHandler.handlerAdded() -> ChannelInboundHandler.channelRegistered() -> ChannelInboundHandler.channelActive()。

|方法|说明|
|---|---|
|ChannelHandlerContext.fireChannelRegistered()|A Channel was registered to its EventLoop.|
|ChannelHandlerContext.fireChannelActive()|A Channel is active now, which means it is connected.|
|ChannelHandlerContext.fireChannelRead(Object)|A Channel received a message.|
|ChannelHandlerContext.fireChannelReadComplete()|Triggers an ChannelInboundHandler#channelReadComplete(ChannelHandlerContext) event to the next ChannelInboundHandler in the ChannelPipeline.|
|ChannelHandlerContext.fireExceptionCaught(Throwable)|A Channel received an Throwable in one of its inbound operations.|
|ChannelHandlerContext.fireUserEventTriggered(Object)|A Channel received an user defined event.|
|ChannelHandlerContext.fireChannelWritabilityChanged()|Triggers an ChannelInboundHandler#channelWritabilityChanged(ChannelHandlerContext) event to the next ChannelInboundHandler in the ChannelPipeline.|
|ChannelHandlerContext.fireChannelInactive()|A {Channel is inactive now, which means it is closed.|
|ChannelHandlerContext.fireChannelUnregistered()|A Channel was unregistered from its EventLoop.|

### Outbound event propagation methods:(出站事件传播方法)

|方法|说明|
|---|---|
|ChannelHandlerContext.bind(SocketAddress, ChannelPromise)||
|ChannelHandlerContext.connect(SocketAddress, SocketAddress, ChannelPromise)||
|ChannelHandlerContext.write(Object, ChannelPromise)||
|ChannelHandlerContext.flush()||
|ChannelHandlerContext.read()||
|ChannelHandlerContext.disconnect(ChannelPromise)||
|ChannelHandlerContext.close(ChannelPromise)||
|ChannelHandlerContext.deregister(ChannelPromise)||



## ChannelHandlerContext
> 同一个ChannelHandler可以添加到多个ChannelPipeline，因此同一个ChannelHandler可以有多个ChannelHandlerContext。





### io.netty.channel.DefaultChannelPipeline.fireChannelRead


```
public final ChannelPipeline fireChannelRead(Object msg) {
        AbstractChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }
```

### ChannelInitializer
> 1.调用HttpHelloWorldServerInitializer.initChannel()方法将ChannelHandler添加到ChannelPipeline。

> 2.将当前HttpHelloWorldServerInitializer从ChannelPipeline移除。


```
io.netty.channel.ChannelInitializer.initChannel(io.netty.channel.ChannelHandlerContext)


private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
        if (initMap.add(ctx)) { // Guard against re-entrance.
            try {
                // 调用HttpHelloWorldServerInitializer.initChannel()方法将ChannelHandler添加到ChannelPipeline。
                initChannel((C) ctx.channel());
            } catch (Throwable cause) {
                // Explicitly call exceptionCaught(...) as we removed the handler before calling initChannel(...).
                // We do so to prevent multiple calls to initChannel(...).
                exceptionCaught(ctx, cause);
            } finally {
                ChannelPipeline pipeline = ctx.pipeline();
                if (pipeline.context(this) != null) {
                    pipeline.remove(this);// 将当前HttpHelloWorldServerInitializer从ChannelPipeline移除
                }
            }
            return true;
        }
        return false;
    }
```


### io.netty.channel.AbstractChannel.AbstractUnsafe.register0()
> 执行顺序: ChannelHandler.handlerAdded() -> ChannelInboundHandler.channelRegistered() -> ChannelInboundHandler.channelActive()
```
io.netty.channel.AbstractChannel.AbstractUnsafe.register0()

private void register0(ChannelPromise promise) {
            try {
                // check if the channel is still open as it could be closed in the mean time when the register
                // call was outside of the eventLoop
                if (!promise.setUncancellable() || !ensureOpen(promise)) {
                    return;
                }
                boolean firstRegistration = neverRegistered;
                doRegister();
                neverRegistered = false;
                registered = true;

                // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
                // user may already fire events through the pipeline in the ChannelFutureListener.
                pipeline.invokeHandlerAddedIfNeeded();

                safeSetSuccess(promise);
                pipeline.fireChannelRegistered();
                // Only fire a channelActive if the channel has never been registered. This prevents firing
                // multiple channel actives if the channel is deregistered and re-registered.
                if (isActive()) {
                    if (firstRegistration) {
                        pipeline.fireChannelActive();
                    } else if (config().isAutoRead()) {
                        // This channel was registered before and autoRead() is set. This means we need to begin read
                        // again so that we process inbound data.
                        //
                        // See https://github.com/netty/netty/issues/4805
                        beginRead();
                    }
                }
            } catch (Throwable t) {
                // Close the channel directly to avoid FD leak.
                closeForcibly();
                closeFuture.setClosed();
                safeSetFailure(promise, t);
            }
        }

```



### NioServerSocketChannel
> 1.调用ServerSocketChannel的accept()方法接收来自客户端的连接。

> 2.将客户端连接SocketChannel封装成NioSocketChannel。

```
@Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = SocketUtils.accept(javaChannel());

        try {
            if (ch != null) {
                buf.add(new NioSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            logger.warn("Failed to create a new channel from an accepted socket.", t);

            try {
                ch.close();
            } catch (Throwable t2) {
                logger.warn("Failed to close a socket.", t2);
            }
        }

        return 0;
    }
```


### io.netty.channel.nio.AbstractNioMessageChannel.NioMessageUnsafe

```
    public void read() {
            assert eventLoop().inEventLoop();
            final ChannelConfig config = config();
            final ChannelPipeline pipeline = pipeline();
            final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
            allocHandle.reset(config);

            boolean closed = false;
            Throwable exception = null;
            try {
                try {
                    do {
                        int localRead = doReadMessages(readBuf);
                        if (localRead == 0) {
                            break;
                        }
                        if (localRead < 0) {
                            closed = true;
                            break;
                        }

                        allocHandle.incMessagesRead(localRead);
                    } while (allocHandle.continueReading());
                } catch (Throwable t) {
                    exception = t;
                }

                int size = readBuf.size();
                for (int i = 0; i < size; i ++) {
                    readPending = false;
                    pipeline.fireChannelRead(readBuf.get(i));
                }
                readBuf.clear();
                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (exception != null) {
                    closed = closeOnReadError(exception);

                    pipeline.fireExceptionCaught(exception);
                }

                if (closed) {
                    inputShutdown = true;
                    if (isOpen()) {
                        close(voidPromise());
                    }
                }
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }

```


### 将ServerSocketChannel注册到Selector上。

> io.netty.channel.AbstractChannel.AbstractUnsafe.register() -> io.netty.channel.AbstractChannel.AbstractUnsafe.register0() -> io.netty.channel.nio.AbstractNioChannel.doRegister()。


```
protected void doRegister() throws Exception {
        boolean selected = false;
        for (;;) {
            try {
                selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
                return;
            } catch (CancelledKeyException e) {
                if (!selected) {
                    // Force the Selector to select now as the "canceled" SelectionKey may still be
                    // cached and not removed because no Select.select(..) operation was called yet.
                    eventLoop().selectNow();
                    selected = true;
                } else {
                    // We forced a select operation on the selector before but the SelectionKey is still cached
                    // for whatever reason. JDK bug ?
                    throw e;
                }
            }
        }
    }    
```


### Channel(NioServerSocketChannel, NioSocketChannel)、ChannelPipeline和ChannelHandlerContext如何关联起来
> 通过NioServerSocketChannel和NioSocketChannel的构造方法关联。

```
io.netty.channel.AbstractChannel.AbstractChannel(io.netty.channel.Channel)
    // 这是NioServerSocketChannel或者NioSocketChannel
    protected AbstractChannel(Channel parent) {
        this.parent = parent;
        id = newId();
        unsafe = newUnsafe();
        // 这是ChannelPipeline
        pipeline = newChannelPipeline();
    }
 
    protected DefaultChannelPipeline newChannelPipeline() {
        return new DefaultChannelPipeline(this);
    } 
    
    protected DefaultChannelPipeline(Channel channel) {
        this.channel = ObjectUtil.checkNotNull(channel, "channel");
        succeededFuture = new SucceededChannelFuture(channel, null);
        voidPromise =  new VoidChannelPromise(channel, true);
        // 这是上下文ChannelHandlerContext
        tail = new TailContext(this);
        head = new HeadContext(this);

        head.next = tail;
        tail.prev = head;
    }  
```


### 调用selector.selectNow() 方法获取感兴趣的事件

> io.netty.channel.nio.NioEventLoop.run()

```
protected void run() {
        int selectCnt = 0;
        for (;;) {
            try {
                int strategy;
                try {
                    strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                    switch (strategy) {
                    case SelectStrategy.CONTINUE:
                        continue;

                    case SelectStrategy.BUSY_WAIT:
                        // fall-through to SELECT since the busy-wait is not supported with NIO

                    case SelectStrategy.SELECT:
                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                        if (curDeadlineNanos == -1L) {
                            curDeadlineNanos = NONE; // nothing on the calendar
                        }
                        nextWakeupNanos.set(curDeadlineNanos);
                        try {
                            if (!hasTasks()) {
                                strategy = select(curDeadlineNanos);
                            }
                        } finally {
                            // This update is just to help block unnecessary selector wakeups
                            // so use of lazySet is ok (no race condition)
                            nextWakeupNanos.lazySet(AWAKE);
                        }
                        // fall through
                    default:
                    }
                } catch (IOException e) {
                    // If we receive an IOException here its because the Selector is messed up. Let's rebuild
                    // the selector and retry. https://github.com/netty/netty/issues/8566
                    rebuildSelector0();
                    selectCnt = 0;
                    handleLoopException(e);
                    continue;
                }

                selectCnt++;
                cancelledKeys = 0;
                needsToSelectAgain = false;
                final int ioRatio = this.ioRatio;
                boolean ranTasks;
                if (ioRatio == 100) {
                    try {
                        if (strategy > 0) {
                            processSelectedKeys();
                        }
                    } finally {
                        // Ensure we always run tasks.
                        ranTasks = runAllTasks();
                    }
                } else if (strategy > 0) {
                    final long ioStartTime = System.nanoTime();
                    try {
                        processSelectedKeys();
                    } finally {
                        // Ensure we always run tasks.
                        final long ioTime = System.nanoTime() - ioStartTime;
                        ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                } else {
                    ranTasks = runAllTasks(0); // This will run the minimum number of tasks
                }

                if (ranTasks || strategy > 0) {
                    if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
                        logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                                selectCnt - 1, selector);
                    }
                    selectCnt = 0;
                } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
                    selectCnt = 0;
                }
            } catch (CancelledKeyException e) {
                // Harmless exception - log anyway
                if (logger.isDebugEnabled()) {
                    logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
                            selector, e);
                }
            } catch (Error e) {
                throw (Error) e;
            } catch (Throwable t) {
                handleLoopException(t);
            } finally {
                // Always handle shutdown even if the loop processing threw an exception.
                try {
                    if (isShuttingDown()) {
                        closeAll();
                        if (confirmShutdown()) {
                            return;
                        }
                    }
                } catch (Error e) {
                    throw (Error) e;
                } catch (Throwable t) {
                    handleLoopException(t);
                }
            }
        }
    }

```


### 将ServerBootstrapAcceptor添加到NioServerSocketChannel

> ServerBootstrap.bind() -> AbstractBootstrap.bind() -> AbstractBootstrap.doBind() -> AbstractBootstrap.initAndRegister() -> ServerBootstrap.init()。

> io.netty.bootstrap.AbstractBootstrap.bind(int)

```
void init(Channel channel) {
        setChannelOptions(channel, newOptionsArray(), logger);
        setAttributes(channel, attrs0().entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY));

        ChannelPipeline p = channel.pipeline();

        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Entry<ChannelOption<?>, Object>[] currentChildOptions;
        synchronized (childOptions) {
            currentChildOptions = childOptions.entrySet().toArray(EMPTY_OPTION_ARRAY);
        }
        final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);

        p.addLast(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(final Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                ChannelHandler handler = config.handler();
                if (handler != null) {
                    pipeline.addLast(handler);
                }

                ch.eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.addLast(new ServerBootstrapAcceptor(
                                ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                    }
                });
            }
        });
    }
```

### NioServerSocketChannel感兴趣的事件是: SelectionKey.OP_ACCEPT
```
public NioServerSocketChannel(ServerSocketChannel channel) {
        super(null, channel, SelectionKey.OP_ACCEPT);
        config = new NioServerSocketChannelConfig(this, javaChannel().socket());
    }
```

### 轮询感兴趣的事件(SelectionKey), 调用Selector.selectNow()方法

```
io.netty.channel.nio.NioEventLoop.run()

protected void run() {
        int selectCnt = 0;
        for (;;) {
            try {
                int strategy;
                try {
                    strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                    switch (strategy) {
                    case SelectStrategy.CONTINUE:
                        continue;

                    case SelectStrategy.BUSY_WAIT:
                        // fall-through to SELECT since the busy-wait is not supported with NIO

                    case SelectStrategy.SELECT:
                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                        if (curDeadlineNanos == -1L) {
                            curDeadlineNanos = NONE; // nothing on the calendar
                        }
                        nextWakeupNanos.set(curDeadlineNanos);
                        try {
                            if (!hasTasks()) {
                                strategy = select(curDeadlineNanos);
                            }
                        } finally {
                            // This update is just to help block unnecessary selector wakeups
                            // so use of lazySet is ok (no race condition)
                            nextWakeupNanos.lazySet(AWAKE);
                        }
                        // fall through
                    default:
                    }
                } catch (IOException e) {
                    // If we receive an IOException here its because the Selector is messed up. Let's rebuild
                    // the selector and retry. https://github.com/netty/netty/issues/8566
                    rebuildSelector0();
                    selectCnt = 0;
                    handleLoopException(e);
                    continue;
                }

                selectCnt++;
                cancelledKeys = 0;
                needsToSelectAgain = false;
                final int ioRatio = this.ioRatio;
                boolean ranTasks;
                if (ioRatio == 100) {
                    try {
                        if (strategy > 0) {
                            processSelectedKeys();
                        }
                    } finally {
                        // Ensure we always run tasks.
                        ranTasks = runAllTasks();
                    }
                } else if (strategy > 0) {
                    final long ioStartTime = System.nanoTime();
                    try {
                        processSelectedKeys();
                    } finally {
                        // Ensure we always run tasks.
                        final long ioTime = System.nanoTime() - ioStartTime;
                        ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                } else {
                    ranTasks = runAllTasks(0); // This will run the minimum number of tasks
                }

                if (ranTasks || strategy > 0) {
                    if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
                        logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                                selectCnt - 1, selector);
                    }
                    selectCnt = 0;
                } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
                    selectCnt = 0;
                }
            } catch (CancelledKeyException e) {
                // Harmless exception - log anyway
                if (logger.isDebugEnabled()) {
                    logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
                            selector, e);
                }
            } catch (Error e) {
                throw (Error) e;
            } catch (Throwable t) {
                handleLoopException(t);
            } finally {
                // Always handle shutdown even if the loop processing threw an exception.
                try {
                    if (isShuttingDown()) {
                        closeAll();
                        if (confirmShutdown()) {
                            return;
                        }
                    }
                } catch (Error e) {
                    throw (Error) e;
                } catch (Throwable t) {
                    handleLoopException(t);
                }
            }
        }
    }
```



### 查找入站处理器，从当前处理器开始一个一个往后找

```
private AbstractChannelHandlerContext findContextInbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = executor();
        do {
            ctx = ctx.next;
        } while (skipContext(ctx, currentExecutor, mask, MASK_ONLY_INBOUND));
        return ctx;
    }
```


### 查找出站处理器，从当前处理器开始一个一个往前找

```
private AbstractChannelHandlerContext findContextOutbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = executor();
        do {
            ctx = ctx.prev;
        } while (skipContext(ctx, currentExecutor, mask, MASK_ONLY_OUTBOUND));
        return ctx;
    }
```






### 坑

> 1.所有继承了SimpleChannelInboundHandler的处理器，如果想让后续处理器处理msg， 需要在channelRead0方法里面调用ReferenceCountUtil.retain(msg);


### Netty的future.channel().closeFuture().sync();到底有什么用？

> 主线程执行到这里就 wait 子线程结束，子线程才是真正监听和接受请求的，closeFuture()是开启了一个channel的监听器，负责监听channel是否关闭的状态，如果监听到channel关闭了，子线程才会释放，syncUninterruptibly()让主线程同步等待子线程结果






























