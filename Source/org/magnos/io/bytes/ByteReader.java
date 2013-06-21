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

package org.magnos.io.bytes;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.magnos.io.BufferStream;


/**
 * A detailed reader of a BufferStream. This reader will read exactly what was
 * placed in the BufferStream by a ByteWriter. Both the ByteReader and 
 * ByteWriter will accept null arguments since each nullable object writes a 
 * boolean for whether it is null or not. If the null byte is false a null 
 * object is returned, if the null byte was true the object that was written is 
 * returned. A reader can also read beyond the length of its BufferStream. If 
 * the reader goes beyond the ByteReader all numbers will be returned 0, all
 * objects returned will be null, and the isValid() method will return false.
 * 
 * @author Philip Diffenderfer
 *
 */
public class ByteReader
{
	
	/**
	 * A callback to unserialize an object from a ByteReader.
	 * 
	 * @author Philip Diffenderfer
	 *
	 * @param <T>
	 * 		The type of object to unserialize.
	 */
	public interface ReadCallback<T> 
	{
		
		/**
		 * Unserializes an item from the given ByteReader.
		 * 
		 * @param reader
		 * 		The ByteReader to read the object from.
		 * @return
		 * 		The deserialized item, or null if the reader did not contain
		 * 		enough data (optional, this will be detected anyway).
		 */
		public T read(ByteReader reader);
		
	}
	
	
	
	// The underlying stream
	private final BufferStream stream;
	
	// The buffer we're reading from that contains the data from the stream.
	private final ByteBuffer buffer;
	
	// Whether the reader is valid. This is true unless a method is invoked 
	// which attempts to read more data than exists in the buffer. Once this is
	// set to false all read operations will return 0, false, or null depending
	// on the requested type.
	private boolean valid = true;
	
	
	/**
	 * Instantiates a new ByteReader.
	 * 
	 * @param stream
	 * 		The stream to read from. This reader will only read on the data 
	 * 		that exists in the stream when this reader is instantiated. In other
	 * 		words any data added to the stream will not be visible.
	 */
	public ByteReader(BufferStream stream) 
	{
		this.stream = stream;
		this.buffer = stream.getReader(0); // TODO
	}
	
	/**
	 * Returns the underlying stream of this reader.
	 * 
	 * @return
	 * 		The reference
	 */
	public BufferStream getStream() 
	{
		return stream;
	}

	/**
	 * Returns the number of bytes remaining to read.
	 * 
	 * @return
	 * 		The number of bytes to read.
	 */
	public int size() 
	{
		return buffer.remaining();
	}
	
	/**
	 * Returns whether this reader is valid.
	 * 
	 * @return
	 * 		Whether the reader is valid. This is true unless a method is invoked 
	 * 		which attempts to read more data than exists in the buffer. Once 
	 * 		this is set to false all read operations will return 0, false, or 
	 * 		null depending on the requested type.
	 */
	public boolean isValid() 
	{
		return valid;
	}
	
	/**
	 * Skips the given number of bytes in the reader. If the given number of
	 * bytes exceed the number of bytes remaining in this buffer, all data in
	 * the buffer will be skipped.
	 * 
	 * @param bytes
	 * 		The number of bytes to read.
	 */
	public void skip(int bytes) 
	{
		buffer.position(buffer.position() + Math.min(bytes, buffer.remaining()));
	}
	
	/**
	 * Returns the byte order of the reader.
	 * 
	 * @return
	 * 		The byte order of the reader.
	 */
	public ByteOrder order() 
	{
		return buffer.order();
	}
	
	/**
	 * Sets the byte order of the reader.
	 * 
	 * @param order
	 * 		The new byte order of the reader.
	 */
	public void order(ByteOrder order) 
	{
		buffer.order(order);
	}

	/**
	 * Syncs this reader with the underlying stream. All data that was read in
	 * this reader will be read from the stream, essentially skipping the number
	 * of total bytes read by this reader.
	 */
	public void sync() 
	{
		stream.sync(buffer);
	}
	
	/**
	 * Returns whether this reader has any data remaining.
	 * 
	 * @return
	 * 		True if there's atleast 1 byte in the reader, otherwise false.
	 */
	public boolean hasRemaining() 
	{
		return buffer.hasRemaining();
	}
	
	/**
	 * Returns whether this reader has atleast the number of give bytes.
	 * 
	 * @param bytes
	 * 		The number of bytes to check for existence.
	 * @return
	 * 		True if the size() of this reader is >= bytes.
	 */
	public boolean has(int bytes) 
	{
		return buffer.remaining() >= bytes;
	}
	
	/**
	 * Internal method for ensuring this reader has the given number of bytes.
	 * If it doesn't this reader is now invalid and it returns false.
	 * 
	 * @param bytes
	 * 		The number of bytes to check for existence.
	 * @return
	 * 		True if the reader is still valid, otherwise false.
	 */
	private boolean contains(int bytes) 
	{
		if (bytes > buffer.remaining()) {
			valid = false;
		}
		return valid;
	}
	
	/**
	 * Returns true if this reader has enough data to read a boolean.
	 * 
	 * @return
	 * 		True if this reader has alteast 1 byte, otherwise false.
	 */
	public boolean hasBoolean() 
	{
		return has(1);
	}
	

	/**
	 * Returns true if this reader has enough data to read a byte.
	 * 
	 * @return
	 * 		True if this reader has alteast 1 byte, otherwise false.
	 */
	public boolean hasByte() 
	{
		return has(1);
	}

	/**
	 * Returns true if this reader has enough data to read a short.
	 * 
	 * @return
	 * 		True if this reader has alteast 2 bytes, otherwise false.
	 */
	public boolean hasShort() 
	{
		return has(2);
	}

	/**
	 * Returns true if this reader has enough data to read a char.
	 * 
	 * @return
	 * 		True if this reader has alteast 2 bytes, otherwise false.
	 */
	public boolean hasChar() 
	{
		return has(2);
	}

	/**
	 * Returns true if this reader has enough data to read an integer.
	 * 
	 * @return
	 * 		True if this reader has alteast 4 bytes, otherwise false.
	 */
	public boolean hasInt() 
	{
		return has(4);
	}

	/**
	 * Returns true if this reader has enough data to read a long.
	 * 
	 * @return
	 * 		True if this reader has alteast 8 bytes, otherwise false.
	 */
	public boolean hasLong() 
	{
		return has(8);
	}

	/**
	 * Returns true if this reader has enough data to read a float.
	 * 
	 * @return
	 * 		True if this reader has alteast 4 bytes, otherwise false.
	 */
	public boolean hasFloat() 
	{
		return has(4);
	}

	/**
	 * Returns true if this reader has enough data to read a double.
	 * 
	 * @return
	 * 		True if this reader has alteast 8 bytes, otherwise false.
	 */
	public boolean hasDouble() 
	{
		return has(8);
	}
	
	/**
	 * Reads a boolean from the underlying stream. If the underlying stream
	 * does not contain enough bytes to read a boolean false will be returned
	 * and this reader will be made invalid. A boolean takes up a single byte,
	 * a 0 for false and every other number means true.
	 * 
	 * @return
	 * 		The boolean read from the underlying stream.
	 */
	public boolean getBoolean() 
	{
		if (contains(1)) {
			return (buffer.get() > 0);	
		}
		return false;
	}
	
	/**
	 * Reads a boolean array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array.
	 * 
	 * @return
	 * 		The boolean array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public boolean[] getBooleanArray() 
	{
		if (getBoolean()) {
			return getBooleans(getUshort());
		}
		return null;
	}
	
	/**
	 * Reads a boolean array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The boolean array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public boolean[] getBooleans(int count) 
	{
		return getBooleans(new boolean[count]);
	}
	
	/**
	 * Reads an array of booleans from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned.
	 * 
	 * @param bs
	 * 		The array to place the read booleans in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public boolean[] getBooleans(boolean[] bs) 
	{
		if (contains(bs.length)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = getBoolean();
			}	
			return bs;
		}
		return null;
	}
	
	/**
	 * Reads a byte from the underlying stream. If the underlying stream
	 * does not contain enough bytes to read a byte 0 will be returned and this
	 * reader will be made invalid. 
	 * 
	 * @return
	 * 		The byte read from the underlying stream.
	 */
	public byte get() 
	{
		if (contains(1)) {
			return buffer.get();
		}
		return 0;
	}
	
	/**
	 * Reads a byte from the underlying stream. If the underlying stream
	 * does not contain enough bytes to read a byte 0 will be returned and this
	 * reader will be made invalid. 
	 * 
	 * @return
	 * 		The byte read from the underlying stream.
	 */
	public byte getByte() 
	{
		if (contains(1)) {
			return buffer.get();
		}
		return 0;
	}

	/**
	 * Reads a byte array from the underlying stream. If not enough data exists 
	 * in the stream null will be returned and this reader will be made invalid.
	 * The original state of the array will be returned, if the array written to
	 * the stream was null, then null will be returned. If the array written to
	 * the stream was empty, then an empty array will be returned. This is 
	 * accomplished by using the next boolean in the stream as a marker for 
	 * whether the array is null. The next 2 bytes in the stream are used as an 
	 * unsigned short (0-65535) to determine the number of elements in the 
	 * array.
	 * 
	 * @return
	 * 		The byte array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public byte[] getByteArray() 
	{
		if (getBoolean()) {
			return getBytes(getUshort());
		}
		return null;
	}

	/**
	 * Reads a byte array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The byte array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public byte[] getBytes(int count) 
	{
		return getBytes(new byte[count]);
	}

	/**
	 * Reads an array of bytes from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned.
	 * 
	 * @param bs
	 * 		The array to place the read bytes in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public byte[] getBytes(byte[] bs) 
	{
		if (contains(bs.length)) {
			buffer.get(bs);
			return bs;
		}
		return null;
	}
	
	/**
	 * Reads an unsigned byte from the underlying stream. If the underlying 
	 * stream does not contain enough bytes to read an unsigned byte 0 will be 
	 * returned and this reader will be made invalid. 
	 * 
	 * @return
	 * 		The unsigned byte read from the underlying stream.
	 */
	public short getUbyte() 
	{
		if (contains(1)) {
			return (short)(buffer.get() & 0xFF);
		}
		return 0;
	}

	/**
	 * Reads an unsigned byte array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid. The original state of the array will be returned, if the 
	 * array written to the stream was null, then null will be returned. If the 
	 * array written to the stream was empty, then an empty array will be 
	 * returned. This is accomplished by using the next boolean in the stream as
	 * a marker for whether the array is null. The next 2 bytes in the stream 
	 * are used as an unsigned short (0-65535) to determine the number of 
	 * elements in the array.
	 * 
	 * @return
	 * 		The unsigned byte array written to the stream, or null if the reader
	 *  	is invalid.
	 */
	public short[] getUbyteArray() 
	{
		if (getBoolean()) {
			return getUbytes(getUshort());
		}
		return null;
	}

	/**
	 * Reads an unsigned byte array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid, otherwise an array of the given size will be returned.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The unsigned byte array read from the stream, or null if the read 
	 * 		is invalid.
	 */
	public short[] getUbytes(int count) 
	{
		return getUbytes(new short[count]);
	}

	/**
	 * Reads an array of unsigned bytes from the underlying stream into the 
	 * given array. If not enough data exists in the stream null will be 
	 * returned and this reader will be made invalid, otherwise the given array 
	 * will be returned.
	 * 
	 * @param bs
	 * 		The array to place the read unsigned bytes in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public short[] getUbytes(short[] bs) 
	{
		if (contains(bs.length)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = (short)(buffer.get() & 0xFF);
			}
			return bs;
		}
		return null;
	}
	

	/**
	 * Reads a char from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read a char 0 will be returned and this
	 * reader will be made invalid. A char takes up 2 bytes.
	 * 
	 * @return
	 * 		The char read from the underlying stream.
	 */
	public char getChar() 
	{
		if (contains(2)) {
			return buffer.getChar();	
		}
		return 0;
	}

	/**
	 * Reads a char array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A char takes up 2 bytes.
	 * 
	 * @return
	 * 		The char array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public char[] getCharArray() 
	{
		if (getBoolean()) {
			return getChars(getUshort());
		}
		return null;
	}

	/**
	 * Reads a char array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A char 
	 * takes up 2 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The char array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public char[] getChars(int count) 
	{
		return getChars(new char[count]);
	}

	/**
	 * Reads an array of chars from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A char takes up 2 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read chars in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public char[] getChars(char[] bs) 
	{
		if (contains(bs.length << 1)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getChar();
			}
			return bs;
		}
		return null;
	}

	/**
	 * Reads a short from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read a short 0 will be returned and this
	 * reader will be made invalid. A short takes up 2 bytes. 
	 * 
	 * @return
	 * 		The short read from the underlying stream.
	 */
	public short getShort() 
	{
		if (contains(2)) {
			return buffer.getShort();	
		}
		return 0;
	}
	
	/**
	 * Reads a short array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A short takes up 2 bytes.
	 * 
	 * @return
	 * 		The short array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public short[] getShortArray() 
	{
		if (getBoolean()) {
			return getShorts(getUshort());
		}
		return null;
	}

	/**
	 * Reads a short array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A short 
	 * takes up 2 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The short array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public short[] getShorts(int count) 
	{
		return getShorts(new short[count]);
	}

	/**
	 * Reads an array of shorts from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A short takes up 2 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read shorts in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public short[] getShorts(short[] bs) 
	{
		if (contains(bs.length << 1)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getShort();
			}
			return bs;
		}
		return null;
	}

	/**
	 * Reads an unsigned short from the underlying stream. If the underlying 
	 * stream does not contain enough bytes to read an unsigned short 0 will be
	 * returned and this reader will be made invalid. An unsigned short takes up
	 * 2 bytes.
	 * 
	 * @return
	 * 		The unsigned short read from the underlying stream.
	 */
	public int getUshort() 
	{
		if (contains(2)) {
			return buffer.getShort() & 0xFFFF;	
		}
		return 0;
	}

	/**
	 * Reads an unsigned short array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid. The original state of the array will be returned, if the 
	 * array written to the stream was null, then null will be returned. If the 
	 * array written to the stream was empty, then an empty array will be 
	 * returned. This is accomplished by using the next boolean in the stream 
	 * as a marker for whether the array is null. The next 2 bytes in the stream 
	 * are used as an unsigned short (0-65535) to determine the number of 
	 * elements in the array. An unsigned short takes up 2 bytes.
	 * 
	 * @return
	 * 		The unsigned short array written to the stream, or null if the 
	 * 		reader is invalid.
	 */
	public int[] getUshortArray() 
	{
		if (getBoolean()) {
			return getUshorts(getUshort());
		}
		return null;
	}

	/**
	 * Reads an unsigned short array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid, otherwise an array of the given size will be returned. An
	 * unsigned short takes up 2 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The unsigned short array read from the stream, or null if the read
	 * 		is invalid.
	 */
	public int[] getUshorts(int count) 
	{
		return getUshorts(new int[count]);
	}

	/**
	 * Reads an array of unsigned shorts from the underlying stream into the 
	 * given array. If not enough data exists in the stream null will be 
	 * returned and this reader will be made invalid, otherwise the given array 
	 * will be returned. An unsigned short takes up 2 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read unsigned shorts in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public int[] getUshorts(int[] bs) 
	{
		if (contains(bs.length << 1)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getShort() & 0xFFFF;
			}
			return bs;
		}
		return null;
	}

	/**
	 * Reads an int from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read an int 0 will be returned and this
	 * reader will be made invalid. An int takes up 4 bytes.
	 * 
	 * @return
	 * 		The int read from the underlying stream.
	 */
	public int getInt() 
	{
		if (contains(4)) {
			return buffer.getInt();	
		}
		return 0;
	}

	/**
	 * Reads an int array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. An int takes up 4 bytes.
	 * 
	 * @return
	 * 		The int array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public int[] getIntArray() 
	{
		if (getBoolean()) {
			return getInts(getUshort());
		}
		return null;
	}

	/**
	 * Reads an int array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. An int 
	 * takes up 4 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The int array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public int[] getInts(int count) 
	{
		return getInts(new int[count]);
	}

	/**
	 * Reads an array of ints from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. An int takes up 4 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read ints in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public int[] getInts(int[] bs) 
	{
		if (contains(bs.length << 2)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getInt();
			}	
			return bs;
		}
		return null;
	}

	/**
	 * Reads an unsigned int from the underlying stream. If the underlying 
	 * stream does not contain enough bytes to read an unsigned int 0 will be 
	 * returned and this reader will be made invalid. An unsigned int takes up 
	 * 4 bytes.
	 * 
	 * @return
	 * 		The unsigned int read from the underlying stream.
	 */
	public long getUint() 
	{
		if (contains(4)) {
			return buffer.getInt() & 0xFFFFFFFFL;	
		}
		return 0;
	}

	/**
	 * Reads an unsigned int array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid. The original state of the array will be returned, if the 
	 * array written to the stream was null, then null will be returned. If the 
	 * array written to the stream was empty, then an empty array will be 
	 * returned. This is accomplished by using the next boolean in the stream 
	 * as a marker for whether the array is null. The next 2 bytes in the stream 
	 * are used as an unsigned short (0-65535) to determine the number of 
	 * elements in the array. An unsigned int takes up 4 bytes.
	 * 
	 * @return
	 * 		The unsigned int array written to the stream, or null if the 
	 * 		reader is invalid.
	 */
	public long[] getUintArray() 
	{
		if (getBoolean()) {
			return getUints(getUshort());
		}
		return null;
	}

	/**
	 * Reads an unsigned int array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid, otherwise an array of the given size will be returned. An 
	 * unsigned int takes up 4 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The unsigned int array read from the stream, or null if the read
	 * 		is invalid.
	 */
	public long[] getUints(int count) 
	{
		return getUints(new long[count]);
	}
	

	/**
	 * Reads an array of unsigned ints from the underlying stream into the 
	 * given array. If not enough data exists in the stream null will be 
	 * returned and this reader will be made invalid, otherwise the given array 
	 * will be returned. An unsigned int takes up 4 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read unsigned ints in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public long[] getUints(long[] bs) 
	{
		if (contains(bs.length << 2)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getInt() & 0xFFFFFFFFL;
			}
			return bs;
		}
		return null;
	}

	/**
	 * Reads a long from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read a long 0 will be returned and this
	 * reader will be made invalid. A long takes up 8 bytes.
	 * 
	 * @return
	 * 		The long read from the underlying stream.
	 */
	public long getLong() 
	{
		if (contains(8)) {
			return buffer.getLong();
		}
		return 0;
	}

	/**
	 * Reads a long array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A long takes up 8 bytes.
	 * 
	 * @return
	 * 		The long array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public long[] getLongArray() 
	{
		if (getBoolean()) {
			return getLongs(getUshort());
		}
		return null;
	}

	/**
	 * Reads a long array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A long 
	 * takes up 8 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The long array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public long[] getLongs(int count) 
	{
		return getLongs(new long[count]);
	}

	/**
	 * Reads an array of longs from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A long takes up 8 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read longs in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public long[] getLongs(long[] bs) 
	{
		if (contains(bs.length << 3)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getLong();
			}
			return bs;
		}
		return null;
	}

	/**
	 * Reads a float from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read a float 0 will be returned and this
	 * reader will be made invalid. A float takes up 4 bytes.
	 * 
	 * @return
	 * 		The float read from the underlying stream.
	 */
	public float getFloat() 
	{
		if (contains(4)) {
			return buffer.getFloat();	
		}
		return 0;
	}

	/**
	 * Reads a float array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A float takes up 4 bytes.
	 * 
	 * @return
	 * 		The float array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public float[] getFloatArray() 
	{
		if (getBoolean()) {
			return getFloats(getUshort());
		}
		return null;
	}

	/**
	 * Reads a float array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A float 
	 * takes up 4 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The float array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public float[] getFloats(int count) 
	{
		return getFloats(new float[count]);
	}
	
	/**
	 * Reads an array of floats from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A float takes up 4 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read floats in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public float[] getFloats(float[] bs) 
	{
		if (contains(bs.length << 2)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getFloat();
			}
			return bs;
		}
		return null;
	}


	/**
	 * Reads a double from the underlying stream. If the underlying stream does
	 * not contain enough bytes to read a double 0 will be returned and this
	 * reader will be made invalid. A double takes up 8 bytes.
	 * 
	 * @return
	 * 		The double read from the underlying stream.
	 */
	public double getDouble() 
	{
		if (contains(8)) {
			return buffer.getDouble();	
		}
		return 0;
	}

	/**
	 * Reads a double array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A double takes up 8 bytes.
	 * 
	 * @return
	 * 		The double array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public double[] getDoubleArray() 
	{
		if (getBoolean()) {
			return getDoubles(getUshort());
		}
		return null;
	}

	/**
	 * Reads an int array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A double 
	 * takes up 8 bytes.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The int array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public double[] getDoubles(int count) 
	{
		return getDoubles(new double[count]);
	}

	/**
	 * Reads an array of doubles from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A double takes up 8 bytes.
	 * 
	 * @param bs
	 * 		The array to place the read doubles in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public double[] getDoubles(double[] bs) 
	{
		if (contains(bs.length << 3)) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = buffer.getDouble();
			}	
			return bs;
		}
		return null;
	}
	

	/**
	 * Reads data from the underlying stream and places it in the given buffer.
	 * As much data as possible is copied over.
	 * 
	 * @param x
	 * 		The buffer to place data into.
	 */
	public void getBuffer(ByteBuffer x) 
	{
		x.put(buffer);
	}
	
	/**
	 * Reads a string from the underlying stream. If not enough data exists in
	 * the stream null will be returned and this reader will be made invalid.
	 * The original state of the string will be returned. If the string written
	 * to the stream was null, then null will be returned. If the string written
	 * to the stream was empty, then an empty string will be returned. This is 
	 * accomplished by using the next boolean in the stream as a marker for 
	 * whether the string is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of characters in the 
	 * string. A string takes up 1 byte as the null marker, an unsigned short
	 * for its length, and a byte for each of its characters.
	 * 
	 * @return
	 * 		The string read from the stream, or null if the reader is invalid.
	 */
	public String getString() 
	{
		if (getBoolean() && hasShort()) {
			return getString(getUshort());	
		}
		return null;
	}

	/**
	 * Reads a string from the underlying stream. If not enough data exists in
	 * the stream null will be returned and this reader will be made invalid.
	 * A string is a byte for each of its characters.
	 * 
	 * @param length
	 * 		The length of the string to read in characters.
	 * @return
	 * 		The string read from the stream, or null if the reader is invalid.
	 */
	public String getString(int length) 
	{
		if (contains(length)) {
			return new String(getBytes(length));	
		}
		return null;
	}

	/**
	 * Reads a string array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the array will be returned, if the array 
	 * written to the stream was null, then null will be returned. If the array 
	 * written to the stream was empty, then an empty array will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the array is null. The next 2 bytes in the stream are used as
	 * an unsigned short (0-65535) to determine the number of elements in the 
	 * array. A string takes up 1 byte as the null marker, an unsigned short
	 * for its length, and a byte for each of its characters.
	 * 
	 * @return
	 * 		The string array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public String[] getStringArray() 
	{
		if (getBoolean()) {
			return getStrings(getUshort());
		}
		return null;
	}

	/**
	 * Reads a string array from the underlying stream. If not enough data
	 * exists in the stream null will be returned and this reader will be made
	 * invalid, otherwise an array of the given size will be returned. A string 
	 * takes up 1 byte as the null marker, an unsigned short for its length, and
	 * a byte for each of its characters.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The string array read from the stream, or null if the read is
	 * 		invalid.
	 */
	public String[] getStrings(int count) 
	{
		return getStrings(new String[count]);
	}

	/**
	 * Reads an array of strings from the underlying stream into the given
	 * array. If not enough data exists in the stream null will be returned and
	 * this reader will be made invalid, otherwise the given array will be
	 * returned. A string takes up 1 byte as the null marker, an unsigned short
	 * for its length, and a byte for each of its characters.
	 * 
	 * @param bs
	 * 		The array to place the read strings in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public String[] getStrings(String[] bs) 
	{
		for (int i = 0; i < bs.length && valid; i++) {
			bs[i] = getString();
		}
		return (valid ? bs : null);
	}
	

	/**
	 * Reads a unicod string from the underlying stream. If not enough data 
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. The original state of the string will be returned. If the string
	 * written to the stream was null, then null will be returned. If the string
	 * written to the stream was empty, then an empty string will be returned. 
	 * This is accomplished by using the next boolean in the stream as a marker 
	 * for whether the string is null. The next 2 bytes in the stream are used 
	 * as an unsigned short (0-65535) to determine the number of characters in 
	 * the string. A unicode string takes up 1 byte as the null marker, an 
	 * unsigned short for its length, and 2 bytes for each of its characters.
	 * 
	 * @return
	 * 		The string read from the stream, or null if the reader is invalid.
	 */
	public String getUnicode() 
	{
		if (getBoolean()) {
			return getUnicode(getUshort());
		}
		return null;
	}

	/**
	 * Reads a unicode string from the underlying stream. If not enough data 
	 * exists in the stream null will be returned and this reader will be made 
	 * invalid. A unicode string is 2 bytes for each of its characters.
	 * 
	 * @param length
	 * 		The length of the string to read in characters.
	 * @return
	 * 		The string read from the stream, or null if the reader is invalid.
	 */
	public String getUnicode(int length) 
	{
		if (contains(length)) {
			return new String(getChars(length));	
		}
		return null;
	}

	/**
	 * Reads a unicode string array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid. The original state of the array will be returned, if the 
	 * array written to the stream was null, then null will be returned. If the 
	 * array written to the stream was empty, then an empty array will be 
	 * returned. This is accomplished by using the next boolean in the stream as 
	 * a marker for whether the array is null. The next 2 bytes in the stream 
	 * are used as an unsigned short (0-65535) to determine the number of 
	 * elements in the array. A unicode string takes up 1 byte as the null 
	 * marker, an unsigned short for its length, and 2 bytes for each of its 
	 * characters.
	 * 
	 * @return
	 * 		The long array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	public String[] getUnicodeArray() 
	{
		if (getBoolean()) {
			return getUnicodes(getUshort());
		}
		return null;
	}

	/**
	 * Reads a unicode string array from the underlying stream. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid, otherwise an array of the given size will be returned.  A 
	 * unicode string takes up 1 byte as the null marker, an unsigned short for 
	 * its length, and 2 bytes for each of its characters.
	 * 
	 * @param count
	 * 		The size of the array to return.
	 * @return
	 * 		The unicode string array read from the stream, or null if the read 
	 * 		is invalid.
	 */
	public String[] getUnicodes(int count) 
	{
		return getUnicodes(new String[count]);
	}

	/**
	 * Reads an array of unicode strings from the underlying stream into the 
	 * given array. If not enough data exists in the stream null will be 
	 * returned and this reader will be made invalid, otherwise the given array 
	 * will be returned. A unicode string takes up 1 byte as the null marker, an
	 * unsigned short for its length, and 2 bytes for each of its characters.
	 * 
	 * @param bs
	 * 		The array to place the read unicode strings in.
	 * @return
	 * 		The given array or null if not enough data exists in the stream.
	 */
	public String[] getUnicodes(String[] bs) 
	{
		for (int i = 0; i < bs.length && valid; i++) {
			bs[i] = getUnicode();
		}
		return (valid ? bs : null);
	}
	
	/**
	 * Reads an enum from the underlying stream of the give type. If not enough 
	 * data exists in the stream null will be returned and this reader will be 
	 * made invalid. The original state of the enum will be returned, if the 
	 * enum written to the stream was null, then null will be returned. The
	 * value of the enum is stored as an unsigned short, if the short read is
	 * not a valid enum ordinal then null is returned and this reader is marked
	 * invalid.
	 * 
	 * @param <T>
	 * 		The enum type.
	 * @param type
	 * 		The class of the enum.
	 * @return
	 * 		The enum read from the stream or null if nout enough data exists in
	 * 		the stream.
	 */
	public <T extends Enum<T>> T getEnum(Class<T> type) 
	{
		if (getBoolean()) {
			int ordinal = getUshort();
			if (valid) {
				T[] constants = type.getEnumConstants();
				if (ordinal < constants.length) {
					return constants[ordinal];
				}
				valid = false;
			}
		}
		return null;
	}
	
	/**
	 * Reads an object from the underlying stream using deserialization. The
	 * object being read must be serializable or at least fit the requirements
	 * to be deserialized. If not enough data exists in the stream null will be 
	 * returned and this reader will be made invalid. The original state of the
	 * object will be returned, if the object written to the stream was null, 
	 * then null will be returned. 
	 *  
	 * @return
	 * 		The object read from the stream or null if the reader is invalid.
	 */
	public Object getObject() 
	{
		return getItem(Object.class, new SerializerCallback());
	}
	
	/**
	 * Reads an object from the underlying stream using deserialization.
	 * 
	 * @param <T>
	 * 		The type to cast the read object to.
	 * @return
	 * 		The object read from the stream or null if the reader is invalid.
	 * @see ByteReader#getObject()
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCastObject() 
	{
		return (T)getObject();
	}
	
	/**
	 * Reads an object from the underlying stream using deserialization.
	 * 
	 * @param <T>
	 * 		The type to cast the read object to.
	 * @param type
	 * 		The class of the object type to return.
	 * @return
	 * 		The object read from the stream or null if the reader is invalid.
	 * @see ByteReader#getObject()
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> type) 
	{
		try {
			return (T)getItem(Object.class, new SerializerCallback());
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	
	/**
	 * Reads an object from the underlying stream using a callback which
	 * serializes the object.
	 * 
	 * @param <T>
	 * 		The type of the read object.
	 * @param type
	 * 		The class of the object type to return.
	 * @param callback
	 * 		The callback to invoke to deserialize the object from the reader.
	 * @return
	 * 		The object read from the stream or null if the reader is invalid.
	 */
	public <T> T getItem(Class<T> type, ReadCallback<T> callback) 
	{
		if (getBoolean()) {
			T item = callback.read(this);
			if (valid) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Reads an array from the underlying stream. If not enough data exists in 
	 * the stream null will be returned and this reader will be made invalid. 
	 * The original state of the array will be returned, if the array written 
	 * to the stream was null, then null will be returned. If the array written 
	 * to the stream was empty, then an empty array will be returned. This is 
	 * accomplished by using the next boolean in the stream as a marker for 
	 * whether the array is null. The next 2 bytes in the stream are used as an
	 * unsigned short (0-65535) to determine the number of elements in the 
	 * array.
	 *
	 * @param type
	 * 		The element type of the array.
	 * @param callback
	 * 		The callback to invoke to read each non-null element in the stream.
	 * @return
	 * 		The array written to the stream, or null if the reader is
	 * 		invalid.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] getArray(Class<T> type, ReadCallback<T> callback) 
	{
		if (getBoolean()) {
			T[] array = (T[])Array.newInstance(type, getUshort());
			if (!valid) {
				return null;
			}
			for (int i = 0; i < array.length && valid; i++) {
				if (getBoolean()) {
					array[i] = callback.read(this);
				}
			}
			if (valid) {
				return array;	
			}
		}
		return null;
	}
	
}

