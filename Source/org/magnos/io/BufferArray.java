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

import java.nio.ByteBuffer;

import org.magnos.io.buffer.BufferFactory;

/**
 * WIP
 * 
 * @author pdiffenderfer
 *
 */
public class BufferArray 
{
	public static final int DEFAULT_ARRAY_SIZE = 1;

	private int readerIndex;
	private int readerPosition;

	private int writerIndex;
	private int writerPosition;

	private ByteBuffer[] buffers;

	private final int bufferSize;
	private final BufferFactory factory;

	public BufferArray(BufferFactory factory)
	{
		this( factory, factory.getDefaultSize() );
	}

	public BufferArray(BufferFactory factory, int bufferSize)
	{
		this.factory = factory;
		this.bufferSize = bufferSize;
		this.buffers = new ByteBuffer[DEFAULT_ARRAY_SIZE];
	}

	public void put(byte x)
	{
		getWriter(1).put(write(1), x);
	}

	public byte get()
	{
		return getReader(1).get(read(1));
	}

	public void putByte(byte x)
	{
		getWriter(1).put(write(1), x);
	}

	public byte getByte()
	{
		return getReader(1).get(read(1));
	}

	public void putShort(short x)
	{
		getWriter(2).putShort(write(2), x);
	}

	public short getShort()
	{
		return getReader(2).getShort(read(2));
	}

	public void put(byte[] data)
	{
		ByteBuffer writer = buffers[writerIndex];
		int remaining = data.length;
		int offset = 0;
		while (remaining > 0) {
			int pieces = getChunkedWriter(remaining, 0);
			if (pieces > 0) {
				writer.position(writerPosition);
				writer.put(data, offset, pieces);
				writer.position(0);
				writerPosition += pieces << 0;
			}
			else {
				writer = nextWriter();
			}
			remaining -= pieces;
			offset += pieces;
		}
	}

	public void get(byte[] data)
	{
		if (canRead(data.length)) {
			getReader(data.length).get(data);
		}
		else {

		}
	}

	public void free()
	{
		for (int i = 0; i < buffers.length; i++) {
			if (buffers[i] != null) {
				factory.free( buffers[i] );
			}
		}
	}

	private void doubleArray()
	{
		int p = readerIndex;
		int n = buffers.length;
		int r = n - p;
		int newCapacity = n << 1;

		if (newCapacity < 0) {
			throw new IllegalStateException("Sorry, deque too big");
		}

		ByteBuffer[] a = new ByteBuffer[newCapacity];
		System.arraycopy(buffers, p, a, 0, r);
		System.arraycopy(buffers, 0, a, r, p);

		buffers = a;
		readerIndex = 0;
		writerIndex = n;
	}

	private ByteBuffer getWriter(int bytes)
	{
		ByteBuffer writer = buffers[writerIndex];

		if (writerPosition + bytes > writer.limit()) 
		{
			writer = nextWriter();
		}

		return writer;
	}

	private int getChunkedWriter(int chunks, int chunkPower) 
	{
		ByteBuffer writer = buffers[writerIndex];

		int remaining = writer.limit() - writerPosition;
		int remainingChunks =  remaining >> chunkPower;

		return (remainingChunks < chunks ? remainingChunks : chunks);
	}

	private ByteBuffer nextWriter() 
	{
		ByteBuffer writeBuffer = buffers[writerIndex];
		writeBuffer.limit(writerPosition);
		writeBuffer = factory.allocate(bufferSize);

		writerIndex = (writerIndex + 1) & (buffers.length - 1);
		if (writerIndex == readerIndex) {
			doubleArray();
		}

		buffers[writerIndex] = writeBuffer;
		writerPosition = 0;

		return writeBuffer;
	}

	private ByteBuffer getReader(int bytes)
	{
		ByteBuffer reader = buffers[readerIndex];

		if (readerPosition + bytes > reader.limit()) 
		{
			if (readerPosition == reader.limit()) {
				reader = nextReader();
			}
			else {
				reader = nextCompactedReader(bytes);
			}
		}

		return reader;
	}

	private ByteBuffer nextReader() 
	{
		ByteBuffer readerBuffer = buffers[readerIndex];
		factory.free( readerBuffer );
		buffers[readerIndex] = null;

		readerIndex = (readerIndex + 1) & (buffers.length - 1);
		readerBuffer = buffers[readerIndex];

		readerPosition = 0;

		return readerBuffer;
	}

	private ByteBuffer nextCompactedReader(int bytes)
	{
		ByteBuffer reader = buffers[readerIndex];

		int has = reader.limit() - readerPosition;
		int needs = bytes - has;

		int nextIndex = (readerIndex + 1) & (buffers.length - 1);
		ByteBuffer next = buffers[nextIndex].duplicate();
		next.limit(needs);

		reader.position( readerPosition );
		reader.compact();
		reader.put(next);
		reader.flip();

		readerPosition = needs;

		return reader;
	}

	private boolean canRead(int bytes) 
	{
		return (bytes <= bufferSize);
	}

	private int write(int bytes) 
	{
		int pos = writerPosition;
		writerPosition += bytes;
		return pos;
	}

	private int read(int bytes) 
	{
		int pos = readerPosition;
		readerPosition += bytes;
		return pos;
	}



}
