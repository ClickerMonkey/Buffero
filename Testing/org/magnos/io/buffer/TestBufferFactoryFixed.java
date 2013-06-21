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
import org.magnos.io.buffer.BufferFactoryFixed;


public class TestBufferFactoryFixed
{

	@Test
	public void testAllocate()
	{
		// Creates DirectByteBuffers at size 32
		BufferFactory bf = new BufferFactoryFixed(32, 8);
		ByteBuffer b;
		
		// Below minBufferSize
		b = bf.allocate(6);
		assertEquals( 6, b.capacity() );
		assertEquals( 6, b.remaining() );
		assertFalse( b.isDirect() );
		
		// At minBufferSize
		b = bf.allocate(8);
		assertEquals( 32, b.capacity() );
		assertEquals( 8, b.remaining() );
		assertTrue( b.isDirect() );
		
		// Between minBufferSize and maxBufferSize
		b = bf.allocate(24);
		assertEquals( 32, b.capacity() );
		assertEquals( 24, b.remaining() );
		assertTrue( b.isDirect() );
		
		// At maxBufferSize
		b = bf.allocate(32);
		assertEquals( 32, b.capacity() );
		assertEquals( 32, b.remaining() );
		assertTrue( b.isDirect() );
		
		// Above maxBufferSize
		b = bf.allocate(33);
		assertEquals( 33, b.capacity() );
		assertEquals( 33, b.remaining() );
		assertFalse( b.isDirect() );
	}
	
	@Test
	public void testDispose()
	{
		// Creates DirectByteBuffers at sizes 8,16,32
		BufferFactory bf = new BufferFactoryFixed(32, 8);

		// A non direct buffer
		assertFalse( bf.free(ByteBuffer.allocate(16)) );
		
		// A direct buffer below minBufferSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(7)) );

		// A direct buffer at minBufferSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(8)) );

		// A direct buffer between minBufferSize and maxBufferSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(24)) );
		
		// A direct buffer at maxBufferSize
		assertTrue( bf.free(ByteBuffer.allocateDirect(32)) );
		
		// A direct buffer above maxBufferSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(33)) );
	}
	
	@Test
	public void testFreeFull()
	{
		// Creates DirectByteBuffers at sizes 8,16,32
		BufferFactory bf = new BufferFactoryFixed(32, 8);
		bf.setCapacity(96);
		
		ByteBuffer a, b, c, d;
		a = ByteBuffer.allocateDirect(32);
		b = ByteBuffer.allocateDirect(32);
		c = ByteBuffer.allocateDirect(32);
		d = ByteBuffer.allocateDirect(32);

		assertTrue( bf.free(a) );
		assertTrue( bf.free(b) );
		assertTrue( bf.free(c) );
		assertFalse( bf.free(d) );
		
		// Retrieve one buffer and try disposing again
		ByteBuffer buffer = bf.allocate(24);
		assertEquals( 32, buffer.capacity() );
		assertEquals( 24, buffer.remaining() );
		assertTrue( buffer.isDirect() );
		assertTrue( buffer == c );
		
		assertTrue( bf.free(d) );
	}
	
}
