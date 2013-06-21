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

import org.magnos.io.Transferable;


/**
 * A factory for allocating and freeing ByteBuffers. The factory implementation
 * can cache buffers to save on allocation costs. The factory can be controlled
 * with how much memory its allowed to use for caching. The buffers returned
 * can have pre-existing data.
 * 
 * @author Philip Diffenderfer
 *
 */
public interface BufferFactory extends Transferable<ByteBuffer>
{

	/**
	 * Creates a ByteBuffer with a limit of the given size and a capacity
	 * greater than or equal to. The buffer returned will have a position of 
	 * zero and most likely will contain non-zero data. 
	 * 
	 * @param size
	 * 		The requested size of the allocated ByteBuffer.
	 * @return
	 * 		The ByteBuffer allocated.
	 */
	public ByteBuffer allocate(int size);
	
	/**
	 * Allocates a ByteBuffer with the default size. The returning buffer will 
	 * have a position of zero and a limit of the default size.
	 * 
	 * @return
	 * 		The ByteBuffer allocated.
	 */
	public ByteBuffer allocate();
	
	/**
	 * Performs a resize on the given ByteBuffer if necessary. If the capacity
	 * of the given buffer is less than or equal to the requested size the limit
	 * of the buffer will be adjusted to the new size. If the given buffer is
	 * not large enough for the requested size, a new buffer will be allocated
	 * (possible taken from cache) and all data in the given buffer will
	 * be copied to the new buffer. The returning buffer will have a position
	 * of zero and a limit of the given size.
	 * 
	 * @param old
	 * 		The buffer to "resize" to the requested size.
	 * @param size
	 * 		The minimum capacity of the returned buffer, and the limit of the
	 * 		returned buffer.
	 * @return
	 * 		The buffer with the data from the given buffer (between its start 
	 * 		and its limit) which has a limit matching the given size. The buffer
	 * 		returned could be the buffer given if it can hold the requested
	 * 		amount of data. 
	 */
	public ByteBuffer resize(ByteBuffer old, int size);
	
	/**
	 * Disposes a ByteBuffer. This buffer once given to this method should
	 * never be used again, it could be completely deallocated from memory. If
	 * the factory supports it the buffer may be cached for later use.
	 * 
	 * @param buffer
	 * 		The buffer to dispose.
	 * @return
	 * 		True if the buffer was cached in the factory, otherwise false.
	 */
	public boolean free(ByteBuffer buffer);
	
	/**
	 * Clears the buffer factory of all cached buffers. This should have the 
	 * effect of setting the factory cached size to zero. The amount of memory
	 * released will be returned.
	 * 
	 * @return
	 * 		The amount of memory deallocated in bytes.
	 */
	public long clear();
	
	/**
	 * Fills the BufferFactory cache (if it has one) with the maximum amount of
	 * buffers (or a sensible amount) and return the amount of memory which was
	 * allocated to fill the cache (in bytes).
	 * 
	 * @return
	 * 		The amount of memory allocated to fill the factory.
	 */
	public long fill();
	
	/**
	 * The amount of memory this BufferFactory is using to store cached buffers.
	 * 
	 * @return
	 * 		The amount of memory used for cached buffers in bytes.
	 */
	public long getSize();
	
	/**
	 * Sets the maximum allowable amount of memory the factory can use to cache
	 * buffers. 
	 * 
	 * @param capacity
	 * 		The maximum amount of memory usable for caching, in bytes.
	 */
	public void setCapacity(long capacity);
	
	/**
	 * The maximum amount of memory the factory can use to cache buffers.
	 * 
	 * @return
	 * 		The maximum amount of memory usable for caching, in bytes.
	 */
	public long getCapacity();
	
	/**
	 * Sets the default size for allocating a ByteBuffer without specifying a 
	 * desired size (i.e. BufferFactory.allocate()).
	 * 
	 * @param size
	 * 		The default size of the allocated buffers in bytes.
	 */
	public void setDefaultSize(int size);
	
	/**
	 * The default size for allocating a ByteBuffer without specifying a desired
	 * size (i.e. BufferFactory.allocate()).
	 * 
	 * @return
	 * 		The default size of the allocated buffers in bytes.
	 */
	public int getDefaultSize();
	
	/**
	 * The amount of memory available to the factory for caching buffers.
	 * 
	 * @return
	 * 		The amount of memory in bytes.
	 */
	public long getAvailable();
	
}
