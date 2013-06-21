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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.magnos.io.buffer.BufferFactory;


/**
 * A dynamically expanding buffer of data. A BufferStream is used to read and
 * write to Streams, Channels, ByteBuffers, and byte arrays. To get more 
 * detailed interaction use ByteReader or ByteWriter. Once data is written to
 * the stream flush must be called to notify the streams listener that data
 * exists on the BufferStream and is ready for more processing.
 * 
 * A BufferStream is not thread-safe, therefore any access to it must be guarded
 * by synchronization or locks.
 * 
 * @author Philip Diffenderfer
 *
 */
public class DynamicBufferStream implements BufferStream
{
	
	// The listener to flush events.
	private final BufferStreamListener listener;
	
	// The factory to allocate and resize ByteBuffers.
	private final BufferFactory factory;
	
	// The current underlying ByteBuffer.
	private ByteBuffer buffer;
	
	/**
	 * Instantiates a new BufferStream.
	 * 
	 * @param listener
	 * 		The listener which handles flush invokations.
	 * @param factory
	 * 		The factory that allocates and resizes ByteBuffers.
	 */
	public DynamicBufferStream(BufferStreamListener listener, BufferFactory factory) 
	{
		this.listener = listener;
		this.factory = factory;
		this.buffer = factory.allocate();
	}
	
	/**
	 * Drains the channel and puts the data in this BufferStream. If the given
	 * channel cannot be read an exception will be thrown, otherwise the number
	 * of bytes read from the channel will be returned. The given channel is
	 * expected to be non-blocking, and the read method on the channel must
	 * return 0 if there is no more data to drain.
	 * 
	 * @param channel
	 * 		The channel to drain data from.
	 * @return
	 * 		The number of bytes drained from the channel.
	 * @throws IOException
	 * 		The channel is unreadable.
	 */
	public int drain(ReadableByteChannel channel) throws IOException 
	{
		int read, drained = 0;
		for (read = 0; (read = channel.read(buffer)) > 0; ) {
			if (buffer.remaining() == 0) {
				expand();
			}
			drained += read;
		}
		return (read < 0 ? -1 : drained);
	}
	
	/**
	 * Drains the stream and puts the data in this BufferStream. If the given
	 * input stream cannot be read an exception will be thrown, otherwise the
	 * number of bytes read from the channel will be returned. Since the given
	 * stream blocks on reads first this attempts to use the available() 
	 * method of InputStream to guess the number of bytes to read into the
	 * BufferStream. If the availabe method returns 0 then a blocking read will
	 * be made for a single byte, once the read method unblocks that byte will
	 * be added to this BufferStream. If there is no more data in the given 
	 * InputStream this method will return -1.
	 * 
	 * @param stream
	 * 		The InputStream to read from. If the InputStream can be read in
	 * 		non-blocking mod an attempt will be made. If know data exists in
	 * 		the stream but an EOF has not been reached this method will block
	 * 		until data exists or an EOF has been given.
	 * @return
	 * 		The number of bytes added to this BufferStream or -1 if the
	 * 		InputStream has reached EOF.
	 * @throws IOException
	 * 		The InputStream is unreadable.
	 */
	public int drain(InputStream stream) throws IOException 
	{
		int read = stream.available();
		// First try reading in only the available bytes. We can't depend on 
		// this to return the exact number of available bytes, and using this
		// method will never return EOF which is why a blocking read must be
		// made eventually.
		if (read > 0) {
			// Expand if there isn't enough space.
			pad(read);
			for (int i = 0; i < read; i++) {
				buffer.put((byte)stream.read());
			}
		}
		// Make a blocking read for a single byte.
		else {
			read = stream.read();
			// If the EOF has not been reached yet...
			if (read >= 0) {
				// Expand if there isn't enough space.
				if (buffer.remaining() == 0) {
					expand();
				}
				buffer.put((byte)read);
				read = 1;
			}
		}
		// Return the number of bytes read, or -1 for EOF.
		return (read < 0 ? -1 : read);
	}
	
	/**
	 * Drains the buffer and puts the data in this BufferStream. If the given
	 * buffer is empty this will have no affect.
	 * 
	 * @param writer
	 * 		The buffer to take data from and place in this BufferStream.
	 * @return
	 * 		The number of bytes taken from the given buffer and placed in this
	 * 		BufferStream. This is always writer.remaining();
	 */
	public int drain(ByteBuffer writer) 
	{
		int write = writer.remaining();
		if (write > 0) {
			pad(write);
			buffer.put(writer);	
		}
		return write;
	}
	
	/**
	 * Drains the byte array and puts the data in this BufferStream. The data
	 * is taken from the section of the given array described by the datas 
	 * offset in the array and the length of the section (number of bytes). If
	 * the given length or offset exceed the bounds of the array a negative 
	 * number will be returned. If the given length exceeds the bounds of the 
	 * array but the offset exists in the array then the maximum number of bytes 
	 * will be put.
	 * 
	 * @param data
	 * 		The array of data to read from.
	 * @param offset
	 * 		The offset in the given array to start reading from.
	 * @param length
	 * 		The number of bytes to read.
	 * @return
	 * 		The number of bytes taken from the array. If no bytes could be taken
	 * 		from the array a negative number will be returned .
	 */
	public int drain(byte[] data, int offset, int length) 
	{
		int drained = Math.min(data.length - offset, length);
		if (drained > 0) {
			pad(length);
			buffer.put(data, offset, length);
		}
		else {
			drained = -1;
		}
		return drained;
	}
	
	/**
	 * Fills the channel by writing the data from this BufferStream to it. If
	 * the given channel cannot be written to an exception will be thrown,
	 * otherwise the number of bytes written to the channel will be returned.
	 * The given channel is expected to be non-blocking, and the write method
	 * on the channel must return 0 if data cannot currently be written to
	 * the channel (the device could be busy).
	 * 
	 * @param channel
	 * 		The channel to fill with the data from this BufferStream.
	 * @return
	 * 		The number of bytes taken from this BufferStream and successfully
	 * 		written to the given channel.
	 * @throws IOException
	 * 		The channel is unwritable.
	 */
	public int fill(WritableByteChannel channel) throws IOException 
	{
		int position = buffer.position();
		buffer.flip();
		int write, filled = 0;
		for (write = 0; (write = channel.write(buffer)) > 0; ) {
			filled += write;
		}
		buffer.position(position);
		skip(filled);
		return filled;	
	}
	
	/**
	 * Fills the stream by writing the data from this BufferStream to it. This
	 * method will block until all data taken from this BufferStream is written
	 * to the given stream. The number of bytes written to the stream will be
	 * returned, or an exception will be thrown if the stream is unwritable.
	 *  
	 * @param stream
	 * 		The stream to fill with all of the data in this BufferStream.
	 * @return
	 * 		The number of bytes written to the stream.
	 * @throws IOException
	 * 		The OutputStream is unwritable.
	 */
	public int fill(OutputStream stream) throws IOException 
	{
		int filled = buffer.position();
		buffer.flip();
		while (buffer.hasRemaining()) {
			stream.write(buffer.get() & 0xFF);
		}
		skip(filled);
		return filled;
	}
	
	/**
	 * Fills the buffer and puts data from this BufferStream into it. Only the
	 * maximum number of bytes are copied. The number of bytes copied to the
	 * given buffer will be returned.
	 * 
	 * @param reader
	 * 		The buffer to write data to from this BufferStream.
	 * @return
	 * 		The number of bytes written to the given buffer.
	 */
	public int fill(ByteBuffer reader) 
	{
		int position = buffer.position();
		int read = Math.min(reader.remaining(), position);
		if (read > 0) {
			buffer.position(0);
			buffer.limit(read);
			reader.put(buffer);
			buffer.clear();
			buffer.position(position);
			skip(read);
		}
		return read;
	}


	@Override
	public ByteBuffer getWriter(int bytes) {
		pad(bytes);
		return buffer;
	}

	// TODO
	@Override
	public ByteBuffer getReader(int bytes) {
		return (ByteBuffer)buffer.asReadOnlyBuffer().flip();
	}

	@Override
	public ByteOrder order() {
		return buffer.order();
	}

	@Override
	public void order(ByteOrder order) {
		buffer.order();
	}

	/**
	 * Returns the underlying ByteBuffer for this ByteStream. This may change
	 * as data is added to the BufferStream if it requires more space than
	 * the returned ByteBuffer can provide.
	 * 
	 * @return
	 * 		The reference to the current underlying buffer.
	 */
	public ByteBuffer buffer() 
	{
		return buffer;
	}
	
	/**
	 * Syncs the given read ByteBuffer with this stream. This simulates all data
	 * read in the given ByteBuffer will be read from the stream and discarded.
	 * This is equavilent to calling skip(reader.position()).
	 * 
	 * @param reader
	 * 		The ByteBuffer to synchronize with.
	 */
	public void sync(ByteBuffer reader) 
	{
		skip(reader.position());
	}
	
	/**
	 * Skips a given number of bytes. Skipping bytes will discard the oldest
	 * bytes written to the BufferStream. If the number of bytes to skip exceeds
	 * the number of bytes that exist in this BufferStream then all bytes in
	 * this BufferStream are discarded.
	 * 
	 * @param bytes
	 * 		The number of bytes to skip.
	 */
	public void skip(int bytes) 
	{
		int marker = buffer.position();
		int separation = marker - bytes;
		// Skips all bytes in the stream.
		if (separation <= 0) {
			buffer.clear();
		}
		// Skips only a section of bytes in the stream. This requires a 
		// compaction of the underlying buffer to properly discard of the 
		// skipped bytes.
		else {
			buffer.position(bytes);
			buffer.limit(marker);
			buffer.compact();
			buffer.clear();
			buffer.position(separation);	
		}
	}
	
	/**
	 * Discards all bytes in this BufferStream.
	 */
	public void clear()
	{
		buffer.clear();
	}
	
	/**
	 * Expands the total capacity of the underlying buffer to double its size.
	 */
	public void expand() 
	{
		int position = buffer.position();
		buffer = factory.resize(buffer, buffer.limit() << 1);
		buffer.position(position);
	}
	
	/**
	 * Returns the number of bytes which can be written to this BufferStream
	 * before it must be expanded to hold more.
	 * 
	 * @return
	 * 		The number of bytes remaining.
	 */
	public int remaining() 
	{
		return buffer.remaining();
	}
	
	/**
	 * Ensures the BufferStream has enough space to write the given number
	 * of bytes to it.
	 * 
	 * @param bytes
	 * 		The requested number of bytes to make available.
	 */
	public void pad(int bytes) 
	{
		while (bytes > buffer.remaining()) {
			expand();
		}
	}
	
	/**
	 * Returns the writing position of this BufferStream in its underlying
	 * ByteBuffer.
	 * 
	 * @return
	 * 		The write position in bytes.
	 */
	public int position() 
	{
		return buffer.position();
	}
	
	/**
	 * Returns the number of bytes current written to the BufferStream.
	 * 
	 * @return
	 * 		The position in bytes.
	 */
	public int size() 
	{
		return buffer.position();
	}
	
	/**
	 * Returns the full capacity of this BufferStream in bytes. This may
	 * change if the buffer is expanded to hold more data.
	 * 
	 * @return
	 * 		The capacity in bytes.
	 */
	public int capacity() 
	{
		return buffer.capacity();
	}
	
	/**
	 * Completely frees the underlying ByteBuffer of this BufferStream. This
	 * should only be called if the BufferStream is never going to be invoked
	 * in any way ever again. If an attempt to invoke this BufferStream after
	 * its freed is made a NullPointerException will be thrown. This method
	 * can be invoked any number of times but only the first invokation will
	 * actually free the BufferStream.
	 */
	public void free() 
	{
		if (buffer != null) {
			factory.free(buffer);
			buffer = null;	
		}
	}
	
	/**
	 * Flushes the data in this BufferStream by notifying the listener that data
	 * is ready to be processed. This is typically only used if the BufferStream
	 * is manipulated explicitly and not through typical methods.
	 */
	public void flush() 
	{
		listener.onBufferFlush(this);
	}
	
	/**
	 * Returns whether this BufferStream contains any data.
	 * 
	 * @return
	 * 		True if this BufferStream has no data, otherwise false.
	 */
	public boolean isEmpty() 
	{
		return buffer.position() == 0;
	}
	
	/**
	 * Returns whether this BufferStream contains any data.
	 * 
	 * @return
	 * 		True if this BufferStream has at least 1 byte, otherwise false.
	 */
	public boolean hasBytes() 
	{
		return buffer.position() > 0;
	}
	
	/**
	 * Returns whether this BufferStream has been freed. A freed BufferStream
	 * should never be accessed again, if it is a NullPointerException will
	 * be thrown.
	 * 
	 * @return
	 * 		True if free() has been invoked, otherwise false.
	 */
	public boolean isFree() 
	{
		return (buffer == null);
	}
	
}
