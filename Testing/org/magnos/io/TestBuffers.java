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

package org.magnos.io;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;


import org.junit.Test;
import org.magnos.io.Buffers;


public class TestBuffers
{

	@Test
	public void testToBufferToString()
	{
		String s1 = "Hello World!";
		
		ByteBuffer a = Buffers.toBuffer(s1);
		
		assertEquals(a.capacity(), s1.length());

		String s2 = Buffers.toString(a);
		
		assertEquals(s1, s2);
		
		long t1, t2, t3, d1, d2;
		t1 = t2 = t3 = d1 = d2 = 0;
		final int TIMES = 1024;
		for (int i = 0; i < TIMES; i++)
		{
			t1 = System.nanoTime();
			Buffers.toBuffer("Hello World how are you yoday? Im fine thanks!");
			t2 = System.nanoTime();
			ByteBuffer.wrap("Hello World how are you yoday? Im fine thanks!".getBytes());
			t3 = System.nanoTime();

			d1 += (t2 - t1);
			d2 += (t3 - t2);
		}

		System.out.println("BufferUtil.toBuffer: " + (d1 / TIMES) + "ns.");
		System.out.println("ByteBuffer.wrap: " + (d2 / TIMES) + "ns.");
	}
	
	@Test
	public void testEquals1()
	{
		ByteBuffer a = Buffers.toBuffer("Hello World");
		ByteBuffer b = Buffers.toBuffer("Hello");
		
		// 'Hello World' != 'Hello'
		assertFalse(Buffers.equals(a, b));
		
		// 'Hello' == 'Hello'
		a.limit(5); 
		assertTrue(Buffers.equals(a, b));
		
		// 'llo' == 'llo'
		a.position(2);
		b.position(2);
		assertTrue(Buffers.equals(a, b));
		
		// 'o' == 'o'
		a.limit(8);
		a.position(7);
		b.position(4);
		assertTrue(Buffers.equals(a, b));
	}

	@Test
	public void testEquals2()
	{
		ByteBuffer a = Buffers.toBuffer("Hello World from java");
		ByteBuffer b = Buffers.toBuffer("Hello Java or World");
		
		// 'Hello' == 'Hello'
		assertTrue(Buffers.equals(a, 0, b, 0, 5));

		// 'Java' != 'java'
		assertFalse(Buffers.equals(a, 17, b, 6, 4));
		
		// ' World' == ' World'
		assertTrue(Buffers.equals(a, 6, b, 14, 5));
	}
	
	@Test
	public void testEqualsToEquals()
	{
		ByteBuffer a = Buffers.toBuffer("Hello World from java");
		ByteBuffer b = Buffers.toBuffer("Hello World from java");
		ByteBuffer c = Buffers.toBuffer("avaj morf dlroW olleH");
		ByteBuffer d = Buffers.toBuffer("Hello Java or World");
		ByteBuffer e = Buffers.toBuffer("Hello Java or World T");

		System.out.println("EQUAL");
		testEqualsToEquals(a, b);
		System.out.println("NOT EQUAL (different length)");
		testEqualsToEquals(a, d);
		System.out.println("NOT EQUAL (same length)");
		testEqualsToEquals(a, c);
		System.out.println("EQUAL (slice)");
		testEqualsToEquals(d, 6, e, 6, 12);
		System.out.println("NOT EQUAL (slice)");
		testEqualsToEquals(a, 0, e, 0, 8);
	}
	
	public void testEqualsToEquals(ByteBuffer a, ByteBuffer b)
	{
		long t1, t2, t3, d1, d2;
		t1 = t2 = t3 = d1 = d2 = 0;
		
		final int TIMES = 1024;
		for (int i = 0; i < TIMES; i++)
		{
			t1 = System.nanoTime();
			Buffers.equals(a, b);
			t2 = System.nanoTime();
			a.equals(b);
			t3 = System.nanoTime();

			d1 += (t2 - t1);
			d2 += (t3 - t2);
		}
		
		System.out.println("BufferUtil.equals: " + (d1 / TIMES) + "ns.");
		System.out.println("ByteBuffer.equals: " + (d2 / TIMES) + "ns.");
	}
	
	public void testEqualsToEquals(ByteBuffer a, int offsetA, ByteBuffer b, int offsetB, int length)
	{
		long t1, t2, t3, d1, d2;
		t1 = t2 = t3 = d1 = d2 = 0;
		
		final int TIMES = 1024;
		for (int i = 0; i < TIMES; i++)
		{
			t1 = System.nanoTime();

			Buffers.equals(a, offsetA, b, offsetB, length);
			
			t2 = System.nanoTime();
			
			a.limit(length + offsetA);
			a.position(offsetA);
			b.limit(length + offsetB);
			b.position(offsetB);
			a.equals(b);
			
			t3 = System.nanoTime();

			d1 += (t2 - t1);
			d2 += (t3 - t2);
		}
		
		System.out.println("BufferUtil.equals: " + (d1 / TIMES) + "ns.");
		System.out.println("ByteBuffer.equals: " + (d2 / TIMES) + "ns.");
	}
	
	@Test
	public void testDoubleSize()
	{
		ByteBuffer a = ByteBuffer.allocate(5);
		a.put("Hello".getBytes());
		
		ByteBuffer b = Buffers.doubleSize(a);
		
		assertEquals(5, b.position());
		assertEquals(10, b.capacity());
		
		// Check that b contains a in the beginning.
		assertTrue(Buffers.equals(a, 0, b, 0, 5));
	}
	
	@Test
	public void testFill()
	{
		ByteBuffer a = Buffers.toBuffer("Hello World,");
		ByteBuffer b = Buffers.toBuffer("Hi,");
		ByteBuffer c = Buffers.toBuffer("Your Welcome Ma!!!!");
		
		ByteBuffer d = ByteBuffer.allocate(30);

		assertEquals(a.capacity(), Buffers.fill(a, d));
		assertEquals(b.capacity(), Buffers.fill(b, d));
		assertEquals(d.remaining(), Buffers.fill(c, d));
		
		ByteBuffer result = Buffers.toBuffer("Hello World,Hi,Your Welcome Ma");
		
		assertTrue(result.equals(d.flip()));
	}
	
	@Test
	public void testReadWriteString()
	{
		ByteBuffer a = ByteBuffer.allocate(100);
		
		String s1 = "Hello World";
		assertTrue(Buffers.writeString(a, s1));
		
		a.flip();
		
		assertEquals(a.remaining(), Buffers.getStoredSpace(s1));
		
		String s2 = Buffers.readString(a);
		
		assertEquals(s1, s2);
	}
	
}
