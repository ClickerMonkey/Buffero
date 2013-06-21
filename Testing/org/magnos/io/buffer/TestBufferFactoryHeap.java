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
import org.magnos.io.buffer.BufferFactoryHeap;
import org.magnos.test.BaseTest;


public class TestBufferFactoryHeap extends BaseTest 
{

	@Test
	public void testFill()
	{
		BufferFactory bf = new BufferFactoryHeap();
		
		assertEquals( 0, bf.fill() );
	}
	
	@Test
	public void testClear()
	{
		BufferFactory bf = new BufferFactoryHeap();
		
		bf.free(ByteBuffer.allocate(16));
		
		assertEquals( 0, bf.clear() );
	}
	
	@Test
	public void testFree()
	{
		BufferFactory bf = new BufferFactoryHeap();
		
		assertFalse( bf.free(ByteBuffer.allocate(16)) );
	}
	
	@Test
	public void testAllocate()
	{
		BufferFactory bf = new BufferFactoryHeap();
		
		ByteBuffer b = bf.allocate(16);
		
		assertFalse( b.isDirect() );
	}
	
}
