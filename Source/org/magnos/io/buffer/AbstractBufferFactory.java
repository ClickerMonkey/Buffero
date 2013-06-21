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
import java.util.concurrent.atomic.AtomicLong;

import sun.nio.ch.DirectBuffer;

/**
 * An abstract implementation of a BufferFactory. This implementation provides
 * the basic functionality for all aspects of a BufferFactory. Only a simple
 * subset of functionality is left to the implementation (caching, allocating,
 * filling, and clearing).
 *
 * Implementing factories should ensure concurrent access to any of their
 * methods doesn't forfeit any of the integrity of the implementations internal
 * structure. In other words the implementations must be thread safe.
 *
 * If the implementation has an more appropriate default size it is expected
 * that this default size be set on instantiation of the factory.
 *
 * @author Philip Diffenderfer
 *
 */
public abstract class AbstractBufferFactory implements BufferFactory
{

	// The amount of memory this factory is using for cache.
	protected final AtomicLong usedMemory = new AtomicLong(0);

	// The maximum amount of memory this factory can cache. The default maximum
	// for a single factory is 1 megabyte (2^20 bytes).
	protected final AtomicLong maxMemory = new AtomicLong(1 << 20);

	// The default size of the ByteBuffers when allocated without a given size.
	// The default value for this is 512 bytes (2^9 bytes).
	protected int defaultSize = 1 << 9;


	/**
	 * Provides the implementation with a ByteBuffer to cache if it chooses to
	 * do so. If the implementation cannot or will not cache the given buffer
	 * it should return false, if the buffer is cached for later retrieval then
	 * true should be returned.
	 *
	 * @param buffer
	 * 		The buffer to attempt to cache.
	 * @return
	 * 		True if the buffer was cached, otherwise false.
	 */
	protected abstract boolean onCache(ByteBuffer buffer);

	/**
	 * Requests that the implementation allocate a ByteBuffer with a capacity
	 * greater than or equal to the given size. If the implementation was able
	 * to take a buffer from cache it should invoke cache.set(true).
	 *
	 * @param size
	 * 		The minimum capacity of the buffer to allocate.
	 * @param cache
	 * 		The flag to set to true if the buffer was taken from cache and not
	 * 		allocated on the spot.
	 * @return
	 * 		The ByteBuffer allocated. The capacity must be greater than or equal
	 * 		to the given size, but the ByteBuffers properties (position, limit)
	 * 		have no required state.
	 */
	protected abstract ByteBuffer onAllocate(int size, AtomicBoolean cache);

	/**
	 * Requires the implementation to fill its cache with the maximum amount of
	 * cached memory (or a sensible amount) and return the amount of memory
	 * which was allocated to fill the cache (if any).
	 *
	 * @return
	 * 		The amount of memory allocated to fill the factory.
	 */
	protected abstract long onFill();

	/**
	 * Requires the implementation to iterate over all cached buffers (if any)
	 * and remove each one from the cache and add it to a List structure.
	 *
	 * @return
	 * 		The list of ByteBuffers taken from the implementations cache.
	 */
	protected abstract List<ByteBuffer> onRelease();


	/**
	 * Tries to deallocate the buffer from memory immediately. This only works
	 * if the given buffer is a DirectBuffer.
	 *
	 * @param buffer
	 * 		The buffer to free from memory.
	 */
	protected void onFree(ByteBuffer buffer)
	{
		if (buffer instanceof DirectBuffer) {
			((DirectBuffer)buffer).cleaner().clean();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ByteBuffer allocate(int size)
	{
		AtomicBoolean cache = new AtomicBoolean(false);

		// Get a buffer with the required capacity.
		ByteBuffer buffer = onAllocate(size, cache);

		// Returns null if not enough memory
		if (buffer == null) {
			return null;
		}

		// If the buffer was taken from the cache, update the amount of
		// memory this factory is using for cached buffers.
		if (cache.get()) {
			usedMemory.addAndGet(-buffer.capacity());
		}

		// Set the position and limit of the buffer.
		buffer.position(0);
		buffer.limit(size);

		return buffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer allocate()
	{
		return allocate(defaultSize);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean free(ByteBuffer buffer)
	{
		boolean cached = false;

		// If caching this buffer will go over the maximum allowable cached
		// buffer memory then simply free the buffer and return the result.
		if (usedMemory.get() + buffer.capacity() > maxMemory.get()) {
			onFree(buffer);
		}
		// Try caching the buffer...
		else {
			cached = onCache(buffer);
			// If cached update used memory.
			if (cached) {
				usedMemory.addAndGet(buffer.capacity());
			}
			// Else free the buffer from memory.
			else {
				onFree(buffer);
			}
		}
		return cached;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer resize(ByteBuffer old, int size)
	{
		// Be hopeful that we can keep the old one and not have to allocate
		// a new buffer to fit the size...
		ByteBuffer upgrade = old;

		// If the old buffer can fit the size, keep it and adjust the limit.
		if (size <= old.capacity())
		{
			old.limit(size);
		}
		// If a new buffer must be made...
		else
		{
			// Allocate a buffer with the limit of size.
			upgrade = allocate(size);

			// Copy over the data from the given buffer to the new buffer.
			old.position(0);
			upgrade.put(old);
			upgrade.position(0);

			// Free the old buffer.
			free(old);
		}

		// Return the upgraded buffer.
		return upgrade;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ByteBuffer> release()
	{
		// Take all buffers from implementation
		List<ByteBuffer> released = onRelease();
		// Total how much memory they required.
		long memory = 0;
		for (ByteBuffer b : released)
		{
			memory += b.capacity();
		}
		// Adjust the used memory to account for removal of these buffers.
		usedMemory.addAndGet(-memory);
		// Return the array of removed buffers.
		return released;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ByteBuffer> transfer(List<ByteBuffer> elements)
	{
		List<ByteBuffer> denied = new ArrayList<ByteBuffer>();
		// Calculate how much memory is transfered to this factory, and build
		// a list of the buffers denied for caching.
		for (ByteBuffer b : elements)
		{
			// If the buffer can be cached, increment the amount of cache memory
			if (b.capacity() + usedMemory.get() <= maxMemory.get() && onCache(b)) {
				usedMemory.addAndGet(b.capacity());
			}
			// Else the buffer wasn't the right type, size, or the max amount of
			// memory has been reached.
			else {
				denied.add(b);
			}
		}
		return denied;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long clear()
	{
		// Take all buffers from implementation
		List<ByteBuffer> released = onRelease();
		// Total how much memory they required.
		long memory = 0;
		for (ByteBuffer b : released)
		{
			memory += b.capacity();
			// Finally release this buffer from memory.
			onFree(b);
		}
		// Adjust the used memory to account for removal of these buffers.
		usedMemory.addAndGet(-memory);

		// Return the amount of memory freed.
		return memory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long fill()
	{
		long cached = onFill();
		usedMemory.addAndGet(cached);
		return cached;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSize()
	{
		return usedMemory.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCapacity()
	{
		return maxMemory.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCapacity(long capacity)
	{
		maxMemory.set(capacity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getAvailable()
	{
		return maxMemory.get() - usedMemory.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDefaultSize()
	{
		return defaultSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultSize(int size)
	{
		this.defaultSize = size;
	}

}
