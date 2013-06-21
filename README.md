buffero
=======

A Java library used for efficiently working with ByteBuffers.

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

https://github.com/ClickerMonkey/buffero/tree/master/build

**Projects using buffero:**
- [statastic](https://github.com/ClickerMonkey/statastic)
- [daperz](https://github.com/ClickerMonkey/daperz)
- [falcon](https://github.com/ClickerMonkey/falcon)

**Dependencies**
- [curity](https://github.com/ClickerMonkey/curity)
- [testility](https://github.com/ClickerMonkey/testility) *for unit tests*

**Testing Examples**

https://github.com/ClickerMonkey/buffero/tree/master/Testing/org/magnos/io
