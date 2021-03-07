## ByteBuf

### 创建ByteBuf：使用Unpooled里面的方法创建

> io.netty.buffer.Unpooled

### 衍生Buf(独立的readerIndex, writerIndex; 共享content)
> duplicate和slice。

```
duplicate()
slice()
slice(int, int)
readSlice(int)

retainedDuplicate()
retainedSlice()
retainedSlice(int, int)
readRetainedSlice(int)
```
> A derived buffer will have an independent readerIndex, writerIndex and marker indexes, while it shares other internal data representation, just like a NIO buffer does.


### copy方法 (独立的readerIndex, writerIndex; 独立的content)


### Non-retained and retained derived buffers (非保留和保留衍生Buf)非保留衍生Buf不会使reference count增加。


> Note that the duplicate(), slice(), slice(int, int) and readSlice(int) does NOT call retain() on the returned derived buffer, and thus its reference count will NOT be increased. If you need to create a derived buffer with increased reference count, consider using retainedDuplicate(), retainedSlice(), retainedSlice(int, int) and readRetainedSlice(int) which may return a buffer implementation that produces less garbage.



### ByteBuf转换成字节数组

```
if(hasArray())
{
    byte[] bytes = byteBuf.array();
}
```

### ByteBuf转换NIO的ByteBuffer

```
if(nioBufferCount())
{
    ByteBuffer nioBuffer = byteBuf.nioBuffer();
}
```

### ByteBuf转换成字符串(不能用toString())


```
String str = byteBuf.toString(Charset);
```


### ByteBuf转换成I/O Streams
> Please refer to ByteBufInputStream and ByteBufOutputStream.














