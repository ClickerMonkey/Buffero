buffero
=======

![Stable](http://i4.photobucket.com/albums/y123/Freaklotr4/stage_stable.png)

A Java library used for efficiently working with ByteBuffers.

**Features**
- A way to write to ByteBuffers that avoids BufferOverflow and BufferUnderflow exceptions. This means the object you write to either resizes the underlying buffer or has a chain of buffers, and the object you read from is marked as invalid if there isn't enough data - and default values are returned.
- BufferFactory has several implementations to quickly allocate, cache, reuse, and dispose of ByteBuffers

**Documentation**
- [JavaDoc](http://gh.magnos.org/?r=http://clickermonkey.github.com/buffero/)

**Example**

```java
// A listener to the buffer stream when flush is invoked.
BufferStreamListener listener = ...;

// A factory to manage buffers
BufferFactory buffers = new BufferFactoryBinary(8, 14);

// A stream of data which can expand
BufferStream stream = new BufferStream(listener, buffers);

// Write data to the stream
ByteWriter out = new ByteWriter(stream);
out.putBoolean(true);
out.putUint(234L);
out.putString("Meow");
out.putObject(new BigDecimal("3.323215235123"));
out.putFloatArray(new float[] {4.5f, 1f});
out.putIntArray(null);

// Read data from the stream
ByteReader in = new ByteReader(stream);
boolean d0 = in.getBoolean();               // true
long d1 = in.getUint();                     // 234
String d2 = in.getString();                 // "Meow" 
BigDecimal d3 = in.getCastObject();         // 3.323215235123
float[] d4 = in.getFloatArray();            // {4.5f, 1f}
int[] d5 = in.getIntArray();                // null

// in.isValid() == true, read more than the stream has then check
long[] d6 = in.getUint(97);                 // null
if (!in.isValid()) {
    // this will execute!
}
```

**Builds**
- [buffero-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/buffero/blob/master/build/buffero-1.0.0.jar?raw=true)
- [buffero-src-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/buffero/blob/master/build/buffero-src-1.0.0.jar?raw=true) *- includes source code*
- [buffero-all-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/buffero/blob/master/build/buffero-1.0.0.jar?raw=true) *- includes all dependencies*
- [buffero-all-src-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/buffero/blob/master/build/buffero-src-1.0.0.jar?raw=true) *- includes all dependencies and source code*

**Projects using buffero:**
- [statastic](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/statastic)
- [daperz](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/daperz)
- [falcon](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/falcon)

**Dependencies**
- [curity](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/curity)
- [testility](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/testility) *for unit tests*

**Testing Examples**
- [Testing/org/magnos/io](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/buffero/tree/master/Testing/org/magnos/io)
