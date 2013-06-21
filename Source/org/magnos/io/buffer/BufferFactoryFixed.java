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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A BufferFactory where the buffers allocated all have the same size.
 * 
 * @author Philip Diffenderfer
 *
 */
public class BufferFactoryFixed extends AbstractBufferFactory 
{

	// The size of the DirectByteBuffers allocated. If the size requested is
	// more than this then a HeapByteBuffer will be allocated instead.
	private final int maxSize;
	
	// The minimum requestable size of a DirectByteBuffer. If the size requested
	// is less than this, a HeapByteBuffer will be returned. If the size is
	// larger than this and smaller than maxSize then a buffer of capacity 
	// maxSize will be returned.
	private final int minSize;
	
	// The stack of buffers.
	private final ByteBufferStack stack;
	

	/**
	 * Instantiates a new BufferFactoryFixed.
	 * 
	 * @param maxSize
	 * 		The size of the DirectByteBuffers allocated. If the size requested 
	 * 		is more than this then a HeapByteBuffer will be allocated instead.
	 * @param minSize 
	 * 		The minimum requestable size of a DirectByteBuffer. If the size 
	 * 		requested is less than this, a HeapByteBuffer will be returned. If 
	 * 		the size is larger than this and smaller than maxSize then a buffer 
	 * 		of capacity maxSize will be returned.
	 */
	public BufferFactoryFixed(int maxSize, int minSize)
	{
		this.stack = new ByteBufferStack();
		this.maxSize = maxSize;
		this.minSize = minSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ByteBuffer onAllocate(int size, AtomicBoolean cache) 
	{
		// For any size thats greater then the max or less then the minimum
		// buffer sizes just create a HeapByteBuffer.
		if (size > maxSize || size < minSize) {
			try {
				return ByteBuffer.allocate(size);
			}
			catch (OutOfMemoryError e) {
				System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
				return null;
			}
		}
		
		// Pop a buffer off of the stack, or allocate a new one.
		ByteBuffer buffer = stack.pop();
		if (buffer == null) {
			try {
				buffer = ByteBuffer.allocateDirect(maxSize);
			}
			catch (OutOfMemoryError e) {
				System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
				return null;
			}
		}
		// Taken from the cache!
		else {
			cache.set(true);
		}
		return buffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean onCache(ByteBuffer buffer) 
	{
		// If the buffer doesn't have the same buffer size or the buffer
		// is not a DirectByteBuffer then return.
		if (buffer.capacity() != maxSize || !buffer.isDirect()) {
			return false;
		}
		
		stack.push(buffer);
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long onFill() 
	{
		long memory = 0;
		// While available memory decreases...
		while (getAvailable() >= maxSize) 
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(maxSize);
			// Ensure the buffer has actually been allocated.
			if (buffer != null) {
				stack.push(buffer);
				memory += maxSize;
			}
			// Buffer allocation issue?
			else {
				return memory;
			}
		}
		return memory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ByteBuffer> onRelease() 
	{
		ByteBuffer buffer;
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		while ((buffer = stack.pop()) != null) {
			buffers.add(buffer);
		}
		return buffers;
	}

}
