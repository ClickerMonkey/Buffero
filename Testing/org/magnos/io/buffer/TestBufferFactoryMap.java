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
import org.magnos.io.buffer.BufferFactoryMap;

public class TestBufferFactoryMap
{

	@Test
	public void testAllocate()
	{
		// Creates DirectByteBuffers at sizes 8->32
		BufferFactory bf = new BufferFactoryMap(32, 8);
		ByteBuffer b;
		
		// Below minDirectSize
		b = bf.allocate(6);
		assertEquals( 6, b.capacity() );
		assertEquals( 6, b.remaining() );
		assertFalse( b.isDirect() );
		
		// At minDirectSize
		b = bf.allocate(8);
		assertEquals( 8, b.capacity() );
		assertEquals( 8, b.remaining() );
		assertTrue( b.isDirect() );
		
		// Between minDirectSize and maxDirectSize
		b = bf.allocate(24);
		assertEquals( 24, b.capacity() );
		assertEquals( 24, b.remaining() );
		assertTrue( b.isDirect() );
		
		// At maxDirectSize
		b = bf.allocate(32);
		assertEquals( 32, b.capacity() );
		assertEquals( 32, b.remaining() );
		assertTrue( b.isDirect() );
		
		// Above maxDirectSize
		b = bf.allocate(33);
		assertEquals( 33, b.capacity() );
		assertEquals( 33, b.remaining() );
		assertFalse( b.isDirect() );
	}
	
	@Test
	public void testFree()
	{
		// Creates DirectByteBuffers at sizes 8->32
		BufferFactory bf = new BufferFactoryMap(32, 8);

		// A non direct buffer
		assertFalse( bf.free(ByteBuffer.allocate(16)) );
		
		// A direct buffer below minDirectSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(7)) );

		// A direct buffer at minDirectSize
		assertTrue( bf.free(ByteBuffer.allocateDirect(8)) );

		// A direct buffer between minDirectSize and maxDirectSize
		assertTrue( bf.free(ByteBuffer.allocateDirect(24)) );
		
		// A direct buffer at maxDirectSize
		assertTrue( bf.free(ByteBuffer.allocateDirect(32)) );
		
		// A direct buffer above maxDirectSize
		assertFalse( bf.free(ByteBuffer.allocateDirect(33)) );
	}
	
}
