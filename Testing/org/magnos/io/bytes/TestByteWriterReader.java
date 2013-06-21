/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to pdiffenderfer@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via my website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 * 				Open Software License (OSL 3.0)
 */

package org.magnos.io.bytes;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.magnos.io.BufferStream;
import org.magnos.io.DynamicBufferStream;
import org.magnos.io.buffer.BufferFactory;
import org.magnos.io.buffer.BufferFactoryDirect;
import org.magnos.io.bytes.ByteReader;
import org.magnos.io.bytes.ByteWriter;
import org.magnos.io.bytes.ByteReader.ReadCallback;
import org.magnos.io.bytes.ByteWriter.WriteCallback;


public class TestByteWriterReader 
{

	private static final BufferFactory factory = new BufferFactoryDirect();

	@Test
	public void testIterator() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putInt(0x01020304);
		bw.putInt(0x05060708);

		ByteReader bq = new ByteReader(bs);
		ByteReader br = new ByteReader(bs);
		
		assertEquals( 0x01, br.getByte());
		assertEquals( 0x02, br.getByte());
		assertEquals( 0x0304, br.getShort());
		assertEquals( 0x05060708, br.getInt());

		assertEquals( 0x01, bq.getByte());
		assertEquals( 0x02, bq.getByte());
		assertEquals( 0x0304, bq.getShort());
		assertEquals( 0x05060708, bq.getInt());

		assertEquals( 0, br.size());
		assertEquals( 0, bq.size());

		bw.putInt(0x09101112);
		assertEquals( 0, br.size());
		assertEquals( 12, bw.size());
	}

	@Test
	public void testOrder() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.order(ByteOrder.BIG_ENDIAN);
		bw.putInt(3432);
		bw.order(ByteOrder.LITTLE_ENDIAN);
		bw.putInt(3431);

		ByteReader bq = new ByteReader(bs);
		
		bq.order(ByteOrder.BIG_ENDIAN);
		assertEquals( 3432, bq.getInt());
		
		bq.order(ByteOrder.LITTLE_ENDIAN);
		assertEquals( 3431, bq.getInt() );
	}

	@Test
	public void testUByte() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putUbyte((short)0);
		bw.putUbyte((short)127);
		bw.putUbyte((short)128);
		bw.putUbyte((short)255);
		
		ByteReader bq = new ByteReader(bs);

		assertEquals( 0, bq.getUbyte() );
		assertEquals( 127, bq.getUbyte() );
		assertEquals( 128, bq.getUbyte() );
		assertEquals( 255, bq.getUbyte() );
	}

	@Test
	public void testUByteArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		short[] item1 = null;
		short[] item2 = {};
		short[] item3 = {0, 127, 128, 255};

		bw.putUbyteArray(item1);
		bw.putUbyteArray(item2);
		bw.putUbyteArray(item3);

		ByteReader bq = new ByteReader(bs);
		
		assertArrayEquals(item1, bq.getUbyteArray() );
		assertArrayEquals(item2, bq.getUbyteArray() );
		assertArrayEquals(item3, bq.getUbyteArray() );
	}

	@Test
	public void testByte() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putByte((byte)-128);
		bw.putByte((byte)0);
		bw.putByte((byte)127);

		ByteReader bq = new ByteReader(bs);
		
		assertEquals( -128, bq.getByte() );
		assertEquals( 0, bq.getByte() );
		assertEquals( 127, bq.getByte() );
	}

	@Test
	public void testByteArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		byte[] item1 = null;
		byte[] item2 = {};
		byte[] item3 = {-128, 0, 127};

		bw.putByteArray(item1);
		bw.putByteArray(item2);
		bw.putByteArray(item3);

		ByteReader bq = new ByteReader(bs);
		
		assertArrayEquals(item1, bq.getByteArray() );
		assertArrayEquals(item2, bq.getByteArray() );
		assertArrayEquals(item3, bq.getByteArray() );
	}

	@Test
	public void testChar() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putChar((char)0);
		bw.putChar((char)65535);
		bw.putChar('a');

		ByteReader bq = new ByteReader(bs);
		
		assertEquals( (char)0, bq.getChar() );
		assertEquals( (char)65535, bq.getChar() );
		assertEquals( 'a', bq.getChar() );
	}

	@Test
	public void testCharArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		char[] item1 = null;
		char[] item2 = {};
		char[] item3 = {0, 65535, 'a'};

		bw.putCharArray(item1);
		bw.putCharArray(item2);
		bw.putCharArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getCharArray() );
		assertArrayEquals( item2, bq.getCharArray() );
		assertArrayEquals( item3, bq.getCharArray() );
	}

	@Test
	public void testUShort() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putUshort(0);
		bw.putUshort(32767);
		bw.putUshort(32768);
		bw.putUshort(65535);

		ByteReader bq = new ByteReader(bs);

		assertEquals( 0, bq.getUshort() );
		assertEquals( 32767, bq.getUshort() );
		assertEquals( 32768, bq.getUshort() );
		assertEquals( 65535, bq.getUshort() );
	}

	@Test
	public void testUShortArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		int[] item1 = null;
		int[] item2 = {};
		int[] item3 = {0, 32767, 32768, 65535};

		bw.putUshortArray(item1);
		bw.putUshortArray(item2);
		bw.putUshortArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getUshortArray() );
		assertArrayEquals( item2, bq.getUshortArray() );
		assertArrayEquals( item3, bq.getUshortArray() );
	}

	@Test
	public void testShort() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putShort((short)-32768);
		bw.putShort((short)0);
		bw.putShort((short)32767);

		ByteReader bq = new ByteReader(bs);

		assertEquals( -32768, bq.getShort() );
		assertEquals( 0, bq.getShort() );
		assertEquals( 32767, bq.getShort() );
	}

	@Test
	public void testShortArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		short[] item1 = null;
		short[] item2 = {};
		short[] item3 = {-32768, 0, 32767};

		bw.putShortArray(item1);
		bw.putShortArray(item2);
		bw.putShortArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getShortArray() );
		assertArrayEquals( item2, bq.getShortArray() );
		assertArrayEquals( item3, bq.getShortArray() );
	}

	@Test
	public void testUInt() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putUint(0L);
		bw.putUint(2147483647L);
		bw.putUint(2147483648L);
		bw.putUint(4294967295L);

		ByteReader bq = new ByteReader(bs);

		assertEquals( 0L, bq.getUint() );
		assertEquals( 2147483647L, bq.getUint() );
		assertEquals( 2147483648L, bq.getUint() );
		assertEquals( 4294967295L, bq.getUint() );
	}

	@Test
	public void testUIntArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		long[] item1 = null;
		long[] item2 = {};
		long[] item3 = {0L, 2147483647L, 2147483648L, 4294967295L};

		bw.putUintArray(item1);
		bw.putUintArray(item2);
		bw.putUintArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getUintArray() );
		assertArrayEquals( item2, bq.getUintArray() );
		assertArrayEquals( item3, bq.getUintArray() );
	}

	@Test
	public void testInt() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putInt(-2147483648);
		bw.putInt(0);
		bw.putInt(2147483647);

		ByteReader bq = new ByteReader(bs);

		assertEquals( -2147483648, bq.getInt() );
		assertEquals( 0, bq.getInt() );
		assertEquals( 2147483647, bq.getInt() );
	}

	@Test
	public void testIntArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		int[] item1 = null;
		int[] item2 = {};
		int[] item3 = {-2147483648, 0, 2147483647};

		bw.putIntArray(item1);
		bw.putIntArray(item2);
		bw.putIntArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getIntArray() );
		assertArrayEquals( item2, bq.getIntArray() );
		assertArrayEquals( item3, bq.getIntArray() );
	}

	@Test
	public void testFloat() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putFloat(Float.MAX_VALUE);
		bw.putFloat(-Float.MAX_VALUE);
		bw.putFloat(Float.MIN_NORMAL);
		bw.putFloat(Float.MIN_VALUE);
		bw.putFloat(-Float.MIN_VALUE);
		bw.putFloat(Float.NaN);
		bw.putFloat(Float.NEGATIVE_INFINITY);
		bw.putFloat(Float.POSITIVE_INFINITY);
		bw.putFloat(0.0f);
		bw.putFloat(-0.0f);

		ByteReader bq = new ByteReader(bs);

		assertEquals( Float.MAX_VALUE, bq.getFloat(), 0.00001 );
		assertEquals( -Float.MAX_VALUE, bq.getFloat(), 0.00001 );
		assertEquals( Float.MIN_NORMAL, bq.getFloat(), 0.00001 );
		assertEquals( Float.MIN_VALUE, bq.getFloat(), 0.00001 );
		assertEquals( -Float.MIN_VALUE, bq.getFloat(), 0.00001 );
		assertEquals( Float.NaN, bq.getFloat(), 0.00001 );
		assertEquals( Float.NEGATIVE_INFINITY, bq.getFloat(), 0.00001 );
		assertEquals( Float.POSITIVE_INFINITY, bq.getFloat(), 0.00001 );
		assertEquals( 0.0f, bq.getFloat(), 0.00001 );
		assertEquals( -0.0f, bq.getFloat(), 0.00001 );
	}

	@Test
	public void testFloatArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		float[] item1 = null;
		float[] item2 = {};
		float[] item3 = {-Float.MAX_VALUE, 0.0f, Float.NaN, -0.0f, Float.MAX_VALUE};

		bw.putFloatArray(item1);
		bw.putFloatArray(item2);
		bw.putFloatArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEqualsMine(item1, bq.getFloatArray() );
		assertArrayEqualsMine(item2, bq.getFloatArray() );
		assertArrayEqualsMine(item3, bq.getFloatArray() );
	}

	@Test
	public void testDouble() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		//			bq.putDouble(Double.MAX_VALUE);
		//			bq.putDouble(-Double.MAX_VALUE);
		bw.putDouble(Double.MIN_NORMAL);
		bw.putDouble(Double.MIN_VALUE);
		bw.putDouble(-Double.MIN_VALUE);
		bw.putDouble(Double.NaN);
		bw.putDouble(Double.NEGATIVE_INFINITY);
		bw.putDouble(Double.POSITIVE_INFINITY);
		bw.putDouble(0.0);
		bw.putDouble(-0.0);

		ByteReader bq = new ByteReader(bs);

		//			assertEquals( Double.MAX_VALUE, bq.getDouble() );
		//			assertEquals( -Double.MAX_VALUE, bq.getDouble() );
		assertEquals( Double.MIN_NORMAL, bq.getDouble(), 0.00001 );
		assertEquals( Double.MIN_VALUE, bq.getDouble(), 0.00001 );
		assertEquals( -Double.MIN_VALUE, bq.getDouble(), 0.00001 );
		assertEquals( Double.NaN, bq.getDouble(), 0.00001 );
		assertEquals( Double.NEGATIVE_INFINITY, bq.getDouble(), 0.00001 );
		assertEquals( Double.POSITIVE_INFINITY, bq.getDouble(), 0.00001 );
		assertEquals( 0.0, bq.getDouble(), 0.00001 );
		assertEquals( -0.0, bq.getDouble(), 0.00001 );
	}

	@Test
	public void testDoubleArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		double[] item1 = null;
		double[] item2 = {};
		double[] item3 = {-Double.MIN_VALUE, 0.0f, Double.NaN, -0.0f, Double.MIN_VALUE};

		bw.putDoubleArray(item1);
		bw.putDoubleArray(item2);
		bw.putDoubleArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEqualsMine(item1, bq.getDoubleArray() );
		assertArrayEqualsMine(item2, bq.getDoubleArray() );
		assertArrayEqualsMine(item3, bq.getDoubleArray() );
	}

	@Test
	public void testString() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putString(null);
		bw.putString("");
		bw.putString("Hello");
		bw.putString("World");

		ByteReader bq = new ByteReader(bs);

		assertNull(bq.getString() );
		assertEquals( "", bq.getString() );
		assertEquals( "Hello", bq.getString() );
		assertEquals( "World", bq.getString() );
	}

	@Test
	public void testStringArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		String[] item1 = null;
		String[] item2 = {};
		String[] item3 = {"hello", "", "WORLD!", null};

		bw.putStringArray(item1);
		bw.putStringArray(item2);
		bw.putStringArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getStringArray() );
		assertArrayEquals( item2, bq.getStringArray() );
		assertArrayEquals( item3, bq.getStringArray() );
	}

	@Test
	public void testUnicodeString() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		bw.putUnicode(null);
		bw.putUnicode("");
		bw.putUnicode("Hello");
		bw.putUnicode("World");

		ByteReader bq = new ByteReader(bs);

		assertNull(bq.getUnicode() );
		assertEquals( "", bq.getUnicode() );
		assertEquals( "Hello", bq.getUnicode() );
		assertEquals( "World", bq.getUnicode() );
	}

	@Test
	public void testUnicodeStringArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		String[] item1 = null;
		String[] item2 = {};
		String[] item3 = {"hello", "", "WORLD!", null};

		bw.putUnicodeArray(item1);
		bw.putUnicodeArray(item2);
		bw.putUnicodeArray(item3);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getUnicodeArray() );
		assertArrayEquals( item2, bq.getUnicodeArray() );
		assertArrayEquals( item3, bq.getUnicodeArray() );
	}

	enum TestEnum {
		Red, Yellow, Green;
	}

	@Test
	public void testEnum() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		TestEnum item1 = null;
		TestEnum item2 = TestEnum.Red;
		TestEnum item3 = TestEnum.Yellow;
		TestEnum item4 = TestEnum.Green;

		bw.putEnum(item1);
		bw.putEnum(item2);
		bw.putEnum(item3);
		bw.putEnum(item4);

		ByteReader bq = new ByteReader(bs);

		assertEquals( item1, bq.getEnum(TestEnum.class) );
		assertEquals( item2, bq.getEnum(TestEnum.class) );
		assertEquals( item3, bq.getEnum(TestEnum.class) );
		assertEquals( item4, bq.getEnum(TestEnum.class) );
	}

	@Test
	public void testObject() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		ArrayList<String> item1 = new ArrayList<String>();
		Collections.addAll(item1, null, "s", "HEll0", null, "world");

		InetSocketAddress item2 = new InetSocketAddress("google.com", 80);

		Object item3 = null;

		bw.putObject(item1);
		bw.putObject(item2);
		bw.putObject(item3);

		ByteReader bq = new ByteReader(bs);

		assertEquals( item1, bq.getObject() );
		assertEquals( item2, bq.getObject(InetSocketAddress.class) );
		assertEquals( item3, bq.getObject() );
	}

	@Test
	public void testItem() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		ReadCallback<Integer> readCallback = new ReadCallback<Integer>() {
			public Integer read(ByteReader reader) {
				return new Integer(reader.getInt() );
			}
		};
		WriteCallback<Integer> writeCallback = new WriteCallback<Integer>() {
			public void write(ByteWriter writer, Integer item) {
				writer.putInt(item);
			}
		};

		Integer item1 = null;
		Integer item2 = new Integer(0);
		Integer item3 = new Integer(37465);

		bw.putItem(item1, writeCallback);
		bw.putItem(item2, writeCallback);
		bw.putItem(item3, writeCallback);

		ByteReader bq = new ByteReader(bs);

		assertEquals( item1, bq.getItem(Integer.class, readCallback) );
		assertEquals( item2, bq.getItem(Integer.class, readCallback) );
		assertEquals( item3, bq.getItem(Integer.class, readCallback) );
	}

	@Test
	public void testArray() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);
		ReadCallback<Integer> readCallback = new ReadCallback<Integer>() {
			public Integer read(ByteReader reader) {
				return new Integer(reader.getInt() );
			}
		};
		WriteCallback<Integer> writeCallback = new WriteCallback<Integer>() {
			public void write(ByteWriter writer, Integer item) {
				writer.putInt(item);
			}
		};

		Integer[] item1 = null;
		Integer[] item2 = {};
		Integer[] item3 = {null, 0, -1, null, 982344589};

		bw.putArray(item1, writeCallback);
		bw.putArray(item2, writeCallback);
		bw.putArray(item3, writeCallback);

		ByteReader bq = new ByteReader(bs);

		assertArrayEquals( item1, bq.getArray(Integer.class, readCallback) );
		assertArrayEquals( item2, bq.getArray(Integer.class, readCallback) );
		assertArrayEquals( item3, bq.getArray(Integer.class, readCallback) );
	}

	@Test
	public void testSkip() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		ByteWriter bw = new ByteWriter(bs);

		bw.putLong(0x0102030405060708L);
		bw.putLong(0x0910111213141516L);
		bw.putLong(0x1718192021222324L);
		bw.putLong(0x2526272829303132L);
		bw.putLong(0x3334353637383940L);
		bw.putLong(0x4142434445464748L);
		bw.putLong(0x4950515253545556L);

		ByteReader bq = new ByteReader(bs);
		
		// skip byte
		assertEquals( 0x01, bq.getByte() );
		bq.skip(1);
//		System.out.format("0x%016x\n", bq.getLong());
		assertEquals( 0x03, bq.getByte() );
		
		// skip short
		bq.skip(2);
		assertEquals( 0x06, bq.getByte() );
		
		// skip short+byte
		bq.skip(3);
		assertEquals( 0x10, bq.getByte() );
		
		// skip int
		bq.skip(4);
		assertEquals( 0x15, bq.getByte() );
		
		// skip int+byte
		bq.skip(5);
		assertEquals( 0x21, bq.getByte() );
		
		// skip int+short
		bq.skip(6);
		assertEquals( 0x28, bq.getByte() );
		
		// skip int+short+byte
		bq.skip(7);
		assertEquals( 0x36, bq.getByte() );
		
		// skip long
		bq.skip(8);
		assertEquals( 0x45, bq.getByte() );
		
		// skip long+byte
		bq.skip(9);
		assertEquals( 0x55, bq.getByte() );
	}
	
	
	public static void assertArrayEqualsMine(float[] a, float[] b) 
	{
		if (a == b) {
			return;
		}
		assertEquals( a.length, b.length);
		for (int i = 0; i < a.length; i++) {
			assertEquals( a[i], b[i], 0.000001 );
		}
	}

	public static void assertArrayEqualsMine(double[] a, double[] b) 
	{
		if (a == b) {
			return;
		}
		assertEquals( a.length, b.length);
		for (int i = 0; i < a.length; i++) {
			assertEquals( a[i], b[i], 0.000001 );
		}
	}



}
