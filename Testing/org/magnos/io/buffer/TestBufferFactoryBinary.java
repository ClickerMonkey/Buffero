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

package org.magnos.io.buffer;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.magnos.io.buffer.BufferFactory;
import org.magnos.io.buffer.BufferFactoryBinary;


public class TestBufferFactoryBinary
{

	@Test
	public void testAllocate()
	{
		// Creates DirectByteBuffers at sizes 8,16,32
		BufferFactory bf = new BufferFactoryBinary(3, 5);
		ByteBuffer b;
		
		// Below minPower
		b = bf.allocate(6);
		assertEquals(6, b.capacity());
		assertEquals(6, b.remaining());
		assertFalse(b.isDirect());
		
		// At minPower
		b = bf.allocate(8);
		assertEquals(8, b.capacity());
		assertEquals(8, b.remaining());
		assertTrue(b.isDirect());
		
		// Between minPower and maxPower
		b = bf.allocate(24);
		assertEquals(32, b.capacity());
		assertEquals(24, b.remaining());
		assertTrue(b.isDirect());
		
		// At maxPower
		b = bf.allocate(32);
		assertEquals(32, b.capacity());
		assertEquals(32, b.remaining());
		assertTrue(b.isDirect());
		
		// Above maxPower
		b = bf.allocate(33);
		assertEquals(33, b.capacity());
		assertEquals(33, b.remaining());
		assertFalse(b.isDirect());
	}
	
	@Test
	public void testDispose()
	{
		// Creates DirectByteBuffers at sizes 8,16,32
		BufferFactory bf = new BufferFactoryBinary(3, 5);

		// A non direct buffer
		assertFalse( bf.free(ByteBuffer.allocate(16)) );
		
		// A direct buffer with a non power-of-2 size
		assertFalse( bf.free(ByteBuffer.allocateDirect(11)) );

		// A direct buffer with power-of-2 below minPower
		assertFalse( bf.free(ByteBuffer.allocateDirect(4)) );
		
		// A direct buffer with power-of-2 at minPower
		assertTrue( bf.free(ByteBuffer.allocateDirect(8)) );
		
		// A direct buffer with power-of-2 between minPower and maxPower
		assertTrue( bf.free(ByteBuffer.allocateDirect(16)) );
		
		// A direct buffer with power-of-2 at maxPower
		assertTrue( bf.free(ByteBuffer.allocateDirect(32)) );
		
		// A direct buffer with power-of-2 above maxPower
		assertFalse( bf.free(ByteBuffer.allocateDirect(64)) );
	}
	
	@Test
	public void testDisposeFull()
	{
		// Creates DirectByteBuffers at sizes 8,16,32
		BufferFactory bf = new BufferFactoryBinary(3, 5);
		bf.setCapacity(92); // 88 <= x < 104
		
		ByteBuffer a, b, c, d, e, f;
		a = ByteBuffer.allocateDirect(8);
		b = ByteBuffer.allocateDirect(16);
		c = ByteBuffer.allocateDirect(32);
		d = ByteBuffer.allocateDirect(16);
		e = ByteBuffer.allocateDirect(16);
		f = ByteBuffer.allocateDirect(16); // The fourth-16

		assertTrue( bf.free(a) );
		assertTrue( bf.free(b) );
		assertTrue( bf.free(c) );
		assertTrue( bf.free(d) );
		assertTrue( bf.free(e) );
		assertFalse( bf.free(f) );
		
		// Retrieve one buffer and try disposing again
		ByteBuffer buffer = bf.allocate(14);
		assertEquals( 16, buffer.capacity() );
		assertEquals( 14, buffer.remaining() );
		assertTrue( buffer.isDirect() );
		assertTrue( buffer == e );
		
		assertTrue( bf.free(f) );
	}
	
	
}
