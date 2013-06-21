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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * Works best when application only requests a known subset of buffer sizes
 * opposed to a crazy combination of sizes.
 * 
 * @author Philip Diffenderfer
 *
 */
public class BufferFactoryMap extends AbstractBufferFactory 
{

	// The minimum allowable size to cache on the map.
	private final int minSize;
	
	// The maximum allowable size to cache on the map.
	private final int maxSize;
	
	// A lock acquired when stacks are being added to the map, and when the map
	// is being traversed and you want to avoid concurrent modification (adds).
	private final Object writeLock = new Object(); 
	
	// The map of stacks by the capacity of the buffers they hold.
	private final HashMap<Integer, ByteBufferStack> map;
	
	
	/**
	 * Instantiates a new BufferFactoryMap where the maximum buffer size is
	 * 8192 and the minimum buffer size is 128.
	 */
	public BufferFactoryMap()
	{
		this(8192, 128);
	}
	
	/**
	 * Instantiates a new BufferFactoryMap.
	 * 
	 * @param maxSize
	 * 		The maximum allowable buffer size to cache on the map.
	 * @param minSize
	 * 		The minimum allowable buffer size to cache on the map.
	 */
	public BufferFactoryMap(int maxSize, int minSize) 
	{
		this.map = new HashMap<Integer, ByteBufferStack>();
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ByteBuffer onAllocate(int size, AtomicBoolean cache) 
	{
		if (size < minSize || size > maxSize) {
			return ByteBuffer.allocate(size);
		}
		
		ByteBuffer buffer = null;
		ByteBufferStack stack = map.get(size);
		if (stack != null) {
			buffer = stack.pop();
		}
		if (buffer == null) {
			try {
				buffer = ByteBuffer.allocateDirect(size);
			}
			catch (OutOfMemoryError e) {
				System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
				return null;
			}
		}
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
		int size = buffer.capacity();

		if (!buffer.isDirect() || size < minSize || size > maxSize) {
			return false;
		}
		
		ByteBufferStack stack = map.get(size);
		if (stack == null) {
			// Only acquire write lock when adding to map.
			synchronized (writeLock) {
				stack = new ByteBufferStack();
				map.put(size, stack);
			}
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
		// This algorithm transfers each of the stacks in the map to an array
		// list of stacks. This algorithm will continue placing a buffer into
		// each stack until the available memory is as taken as possible.
		
		List<ByteBufferStack> stacks = new ArrayList<ByteBufferStack>();
		// Acquires write lock so no concurrent stack adds occur.
		synchronized (writeLock) {
			for (Entry<Integer, ByteBufferStack> e : map.entrySet()) {
				stacks.add(e.getValue());
			}	
		}
		
		// No stacks? exit!
		if (stacks.isEmpty()) { 
			return 0;
		}
		
		// Sort the stacks from smallest buffers to largest placing the empty
		// stacks at the end of the list.
		Collections.sort(stacks, new Comparator<ByteBufferStack>() {
			public int compare(ByteBufferStack o1, ByteBufferStack o2) {
				ByteBuffer a = o1.peek();
				ByteBuffer b = o2.peek();
				int ax = (a == null ? 0 : a.capacity());
				int bx = (b == null ? 0 : b.capacity());
				return (ax - bx);
			}
		});

		// TODO implement algorithm
		if (Math.random() < 1.0) { // RUN!
			throw new RuntimeException();
		}
		
		long memory = 0;
		long available = getAvailable();
		
		// Place buffers in these until they're full.
		while (available >= 0) {
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
		List<ByteBufferStack> stacks = new ArrayList<ByteBufferStack>();
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		
		// Acquire lock to avoid concurrent adds and fills
		synchronized (writeLock) 
		{
			Iterator<Entry<Integer, ByteBufferStack>> iter = map.entrySet().iterator();
			// Remove each stack and add it to a list for later traversal. This
			// is done to avoid extended control of the write lock.
			while (iter.hasNext()) {
				stacks.add(iter.next().getValue());
				iter.remove();
			}
		}
		
		// Empty each stack into buffers
		for (ByteBufferStack s : stacks) 
		{
			// Pop em off!
			while ((buffer = s.pop()) != null) {
				buffers.add(buffer);
			}
		}
		
		return buffers;
	}

}
