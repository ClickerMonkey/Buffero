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
 * A BufferFactory where all cached buffers have a power-of-2 size within
 * a range, defined by powers. If the range of powers are 8 and 14 this means
 * the buffers cached will be of sizes {256, 512, 1024, 2048, 4096, 8192, 16384}
 * since 2^8 is 256 and 2^14 is 16374. Any buffers smaller or larger requested
 * will be allocated on the fly as HeapByteBuffers.
 * 
 * @author Philip Diffenderfer
 *
 */
public class BufferFactoryBinary extends AbstractBufferFactory 
{

	// The number that determines the largest buffer size pooled,
	// maximum buffer size = 2^maxPower. Any request for a buffer
	// larger then the maximum size returns a HeapByteBuffer.
	private final int maxPower;
	private final int maxBufferSize;
	
	// The number that determines the smallest buffer size pooled,
	// minimum buffer size = 2^minPower. Any request for a buffer
	// smaller then the minimum size returns a HeapByteBuffer.
	private final int minPower;
	private final int minBufferSize;
	
	// The pool of ByteBuffers where every buffer capacity is a power
	// of 2 between minPower and maxPower.
	private final ByteBufferStack[] pool;
	
	
	/**
	 * Instantiates a new BufferFactoryBinary.
	 * 
	 * @param minPower
	 * 		The number that determines the smallest buffer size pooled, minimum 
	 * 		buffer size = 2^minPower. Any request for a buffer smaller then the 
	 * 		minimum size returns a HeapByteBuffer.
	 * @param maxPower
	 * 		The number that determines the largest buffer size pooled, maximum
	 * 		buffer size = 2^maxPower. Any request for a buffer larger then the 
	 * 		maximum size returns a HeapByteBuffer.
	 */
	public BufferFactoryBinary(int minPower, int maxPower)
	{
		int pools = (maxPower - minPower) + 1;
		
		this.pool = new ByteBufferStack[pools];
		for (int i = 0; i < pools; i++) {
			this.pool[i] = new ByteBufferStack();
		}
		
		this.minPower = minPower;
		this.minBufferSize = 1 << minPower;
		this.maxPower = maxPower;
		this.maxBufferSize = 1 << maxPower;
		
		// Default size is the buffer size halfway between min and max.
		this.setDefaultSize(1 << ((minPower + maxPower) >> 1));
	}
	
	/**
	 * Determines the log<sub>2</sub> of a given integer. If the given integer is
	 * less then or equal to 2 then 1 will be returned.
	 * 
	 * @param n
	 * 		The integer to find the log<sub>2</sub> of.
	 */
	private final int log2(int n)
	{
		if (n <= 2)
			return 1;
		int log2 = 2;
		n--;
		while (n > 3) {
			n >>= 1;
			log2++;
		}
		return log2;
	}
	
	/**
	 * Determines whether the given integer is a power of 2.
	 * 
	 * @param n
	 * 		The integer to calculate from.
	 */
	private final boolean isPowerOf2(int n)
	{
		return ((n & (n - 1)) == 0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ByteBuffer onAllocate(int size, AtomicBoolean cache) 
	{
		// If the power is to small or to large then return a HeapByteBuffer.
		if (size < minBufferSize || size > maxBufferSize) {
			try {
				return ByteBuffer.allocate(size);
			}
			catch (OutOfMemoryError e) {
				System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
				return null;
			}
		}

		// Compute the power of the buffer using log2
		int power = log2(size);
		
		// Calculate the index of the pool using minPower.
		int index = power - minPower;

		// Pop the next buffer on this stack.
		ByteBuffer buffer = pool[index].pop();
		
		// If no buffer existed on the stack allocate a new one.
		if (buffer == null) {
			try {
				buffer = ByteBuffer.allocateDirect(1 << power);
			}
			catch (OutOfMemoryError e) {
				System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
				return null;
			}
		}
		else {
			cache.set(true);
		}
		
		// Return the allocated buffer!
		return buffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean onCache(ByteBuffer buffer) 
	{
		int capacity = buffer.capacity();
		
		// We're only pooling direct buffers whose capacity is a power of 2.
		if (!buffer.isDirect() || !isPowerOf2(capacity)) {
			return false;
		}
		
		// Compute the power of the buffer using log2
		int power = log2(capacity);
	
		// If the power is to small or to large then don't pool it.
		if (power < minPower || power > maxPower) {
			return false;
		}
		
		// Calculate the index of the pool using minPower.
		int index = power - minPower;
		
		// Place the buffer on the stack
		pool[index].push(buffer);
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long onFill() 
	{
		// This algorithm will ensure each stack of buffers is taking up the
		// same amount of memory. So a stack for 128 will have 2 buffers
		// for every buffer on a stack for 256. I figure even distribution of
		// the current memory usage is a fair algorithm to fill the cache.
		
		long memory = 0;
		// How much memory is available...
		long available = getAvailable();
		
		// Caculate how much memory it would cost to add a buffer to each of the
		// stacks in the pool.
		long poolCount = (maxPower - minPower + 1);
		int maxPool = (1 << maxPower);
		long generationSize = maxPool * poolCount;
		
		// Calculate how many generations we can add to the cache
		int generations = (int)Math.floor(available / generationSize);
		if (generations == 0) {
			return 0;
		}
		
		// Add these buffers to the stacks
		ByteBuffer buffer = null;
		for (int p = minPower; p <= maxPower; p++) 
		{
			int index = p - minPower;
			int bufferCount = generations << (p - maxPower); 
			
			// Add a stack in for each generation.
			for (int c = 0; c < bufferCount; c++) 
			{
				buffer = ByteBuffer.allocateDirect(1 << p);
				// Ensure the buffer was allocated.
				if (buffer != null) {
					memory += buffer.capacity();
					pool[index].push(buffer);	
				}
				// We couldn't allocate any more buffers?
				else {
					return memory;
				}
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
		List<ByteBuffer> dump = new ArrayList<ByteBuffer>();
		ByteBuffer buffer;
		
		// For each pool of buffers...
		for (int i = 0; i < pool.length; i++) {
			// Pop every buffer off the current stack and add it to the list.
			while ((buffer = pool[i].pop()) != null) {
				dump.add(buffer);
			}
		}
		
		return dump;
	}

}
