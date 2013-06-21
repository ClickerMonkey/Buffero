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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.magnos.io.BufferStream;


/**
 * A detailed writer of a BufferStream. This writer will write all data given
 * to it expanding the underlying buffer as needed. The underlying buffer may
 * become null however if there is not enough memory to store the buffer. Both 
 * the ByteReader and ByteWriter will accept null arguments since each nullable 
 * object writes a boolean for whether it is null or not. If the null byte is 
 * false a null object is returned, if the null byte was true the object that 
 * was written is returned.
 * 
 * @author Philip Diffenderfer
 *
 */
public class ByteWriter
{
	
	/**
	 * A callback to serialize an object to a ByteWriter.
	 * 
	 * @author Philip Diffenderfer
	 *
	 * @param <T>
	 * 		The type of the object to serialize.
	 */
	public interface WriteCallback<T> 
	{
		
		/**
		 * Serializes the given item to the given ByteWriter.
		 * 
		 * @param writer
		 * 		The ByteWriter to place the serialized item in.
		 * @param item
		 * 		The item to serialize.
		 */
		public void write(ByteWriter writer, T item);
	}


	// The underlying stream
	private final BufferStream stream;
	
	/**
	 * Instantiates a new ByteWriter.
	 * 
	 * @param stream
	 * 		The stream to write to. If the stream does not have enough space for
	 * 		any data being written it will be expanded when required.
	 */
	public ByteWriter(BufferStream stream) 
	{
		this.stream = stream;
	}

	/**
	 * Returns the total number of bytes in the underlying stream which have 
	 * been written.
	 * 
	 * @return
	 * 		The number of bytes written.
	 */
	public int size() 
	{
		return stream.size();
	}

	/**
	 * Returns the byte order of the writer.
	 * 
	 * @return
	 * 		The byte order of the writer.
	 */
	public ByteOrder order() 
	{
		return stream.order();
	}

	/**
	 * Sets the byte order of the reader.
	 * 
	 * @param order
	 * 		The new byte order of the reader.
	 */
	public void order(ByteOrder order) 
	{
		stream.order(order);
	}
	
	/**
	 * An internal method for writing whether the object is null and then 
	 * returning that boolean.
	 * 
	 * @param o
	 * 		The object that is attempting to be written.
	 * @return
	 * 		Whether the object can be written. An object can't be written if its
	 * 		null, but can if it was not null.
	 */
	private final boolean putIsNotNull(Object o) 
	{
		boolean exists = (o != null);
		putBoolean(exists);
		return exists;
	}
	
	/**
	 * Writes a boolean to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it will be expanded before it 
	 * is written to. A boolean takes up a single byte, a 0 for false and every
	 * other number means true.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putBoolean(boolean value) 
	{
		stream.getWriter(1).put((byte)(value ? 1 : 0));
	}
	
	/**
	 * Writes a boolean array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putBooleanArray(boolean[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putBooleans(x);
		}
	}
	
	/**
	 * Writes an array of booleans to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putBooleans(boolean[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length);
		for (int i = 0; i < x.length; i++) {
			b.put((byte)(x[i] ? 1 : 0));
		}
	}
	
	/**
	 * Writes a byte to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void put(byte value) 
	{
		stream.getWriter(1).put(value);
	}

	/**
	 * Writes a byte to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putByte(byte value) 
	{
		stream.getWriter(1).put(value);
	}

	/**
	 * Writes a byte array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putByteArray(byte[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putBytes(x);
		}
	}

	/**
	 * Writes an array of bytes to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putBytes(byte[] x) 
	{
		stream.getWriter(x.length).put(x);
	}
	
	/**
	 * Writes an unsigned byte to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it, it will be 
	 * expanded before it is written to.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putUbyte(short value)
	{
		stream.getWriter(1).put((byte)value);
	}

	/**
	 * Writes an unsigned byte array to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. This will save the state of the array, so if its 
	 * null or empty and read back it will return null or empty. This is 
	 * accomplished by writing a single boolean to whether the array is null, if
	 * its not null the length of the array is then written as an unsigned short
	 * (0-65535) and finally the arrays elements are written. An array passed to
	 * this function must have fewer than 65536 elements to ensure when the 
	 * array is read back it remains valid.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUbyteArray(short[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putUbytes(x);
		}
	}

	/**
	 * Writes an array of unsigned bytes to the underlying stream. If the 
	 * underlying stream does not contain enough of free space to write it will 
	 * be expanded before it is written to.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUbytes(short[] x)
	{
		ByteBuffer b = stream.getWriter(x.length);
		for (int i = 0; i < x.length; i++){
			b.put((byte)x[i]);
		}
	}

	/**
	 * Writes a char to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. A char takes up 2 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putChar(char value) 
	{
		stream.getWriter(2).putChar(value);
	}
	
	/**
	 * Writes a char array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A char takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putCharArray(char[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putChars(x);
		}
	}

	/**
	 * Writes an array of chars to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A char takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putChars(char[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 1);
		for (int i = 0; i < x.length; i++) {
			b.putChar(x[i]);
		}
	}
	
	/**
	 * Writes a short to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. A short takes up 2 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putShort(short value) 
	{
		stream.getWriter(2).putShort(value);
	}

	/**
	 * Writes a short array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A short takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putShortArray(short[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putShorts(x);
		}
	}

	/**
	 * Writes an array of shorts to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A short takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putShorts(short[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 1);
		for (int i = 0; i < x.length; i++) {
			b.putShort(x[i]);
		}
	}
	
	/**
	 * Writes an unsigned short to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it, it will be 
	 * expanded before it is written to. An unsigned short takes up 2 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putUshort(int value) 
	{
		stream.getWriter(2).putShort((short)value);
	}

	/**
	 * Writes an unsigned short array to the underlying stream. If the 
	 * underlying stream does not contain enough of free space to write it will 
	 * be expanded before it is written to. This will save the state of the 
	 * array, so if its null or empty and read back it will return null or 
	 * empty. This is accomplished by writing a single boolean to whether the 
	 * array is null, if its not null the length of the array is then written as
	 * an unsigned short (0-65535) and finally the arrays elements are written. 
	 * An array passed to this function must have fewer than 65536 elements to 
	 * ensure when the array is read back it remains valid. An unsigned short 
	 * takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUshortArray(int[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putUshorts(x);
		}
	}

	/**
	 * Writes an array of unsigned shorts to the underlying stream. If the 
	 * underlying stream does not contain enough of free space to write it will 
	 * be expanded before it is written to. An unsigned short takes up 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUshorts(int[] x) 
	{
		for (int i = 0; i < x.length; i++) {
			putUshort(x[i]);
		}
	}
	
	/**
	 * Writes an int to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. An int takes up 4 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putInt(int value) 
	{
		stream.getWriter(4).putInt(value);
	}
	
	/**
	 * Writes an int array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. An int takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putIntArray(int[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putInts(x);
		}
	}

	/**
	 * Writes an array of ints to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. An int takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putInts(int[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 2);
		for (int i = 0; i < x.length; i++) {
			b.putInt(x[i]);
		}
	}
	
	/**
	 * Writes an unsigned int to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it, it will be 
	 * expanded before it is written to. An unsigned int takes up 4 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putUint(long value) 
	{
		stream.getWriter(4).putInt((int)value);
	}
	
	/**
	 * Writes an unsigned int array to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. This will save the state of the array, so if its 
	 * null or empty and read back it will return null or empty. This is 
	 * accomplished by writing a single boolean to whether the array is null, if
	 * its not null the length of the array is then written as an unsigned short
	 * (0-65535) and finally the arrays elements are written. An array passed to
	 * this function must have fewer than 65536 elements to ensure when the 
	 * array is read back it remains valid. An unsigned int takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUintArray(long[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putUints(x);
		}
	}
	
	/**
	 * Writes an array of unsigned ints to the underlying stream. If the 
	 * underlying stream does not contain enough of free space to write it will 
	 * be expanded before it is written to. An unsigned int takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUints(long[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 2);
		for (int i = 0; i < x.length; i++) {
			b.putInt((int)x[i]);
		}
	}
	
	/**
	 * Writes a long to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. A long takes up 8 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putLong(long value) 
	{
		stream.getWriter(8).putLong(value);
	}
	
	/**
	 * Writes a long array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A long takes up 8 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putLongArray(long[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putLongs(x);
		}
	}

	/**
	 * Writes an array of longs to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A long takes up 8 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putLongs(long[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 3);
		for (int i = 0; i < x.length; i++) {
			b.putLong(x[i]);
		}
	}
	
	/**
	 * Writes a float to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. A float takes up 4 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putFloat(float value) 
	{
		stream.getWriter(4).putFloat(value);
	}
	
	/**
	 * Writes a float array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A float takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putFloatArray(float[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putFloats(x);
		}
	}

	/**
	 * Writes an array of floats to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A float takes up 4 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putFloats(float[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 2);
		for (int i = 0; i < x.length; i++) {
			b.putFloat(x[i]);
		}
	}

	/**
	 * Writes a double to the underlying stream. If the underlying stream does
	 * not contain enough of free space to write it, it will be expanded before 
	 * it is written to. A double takes up 8 bytes.
	 * 
	 * @param value
	 * 		The value to write to the stream.
	 */
	public void putDouble(double value) 
	{
		stream.getWriter(8).putDouble(value);
	}
	
	/**
	 * Writes a double array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A double takes up 8 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putDoubleArray(double[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putDoubles(x);
		}
	}
	
	/**
	 * Writes an array of doubles to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A double takes up 8 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putDoubles(double[] x) 
	{
		ByteBuffer b = stream.getWriter(x.length << 3);
		for (int i = 0; i < x.length; i++) {
			b.putDouble(x[i]);
		}
	}
	
	/**
	 * Writes the remaining data in the given buffer to the underlying stream.
	 * If the underlying stream does not contain enough of free space to write 
	 * it will be expanded before it is written to.
	 * 
	 * @param x
	 * 		The buffer to transfer data from.
	 */
	public void putBuffer(ByteBuffer x) 
	{
		stream.drain(x);
	}
	
	/**
	 * Writes a string to the underlying stream. If the underlying stream does 
	 * not contain enough of free space to write it will be expanded before it 
	 * is written to. This will save the state of the string, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the string is null, if its not 
	 * null the length of the string is then written as an unsigned short 
	 * (0-65535) and finally the strings characters are written. A string passed 
	 * to this function must have fewer than 65536 characters to ensure when the
	 * string is read back it remains valid. A string takes up 1 byte as the 
	 * null marker, an unsigned short for its length, and a byte for each of its 
	 * characters.
	 * 
	 * @param s
	 * 		The string to write to the stream.
	 */
	public void putString(String s) 
	{
		if (putIsNotNull(s)) {
			putUshort(s.length());
			putBytes(s.getBytes());
		}
	}

	/**
	 * Writes a string array to the underlying stream. If the underlying stream 
	 * does not contain enough of free space to write it will be expanded before 
	 * it is written to. This will save the state of the array, so if its null
	 * or empty and read back it will return null or empty. This is accomplished
	 * by writing a single boolean to whether the array is null, if its not null
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the arrays elements are written. An array passed to this 
	 * function must have fewer than 65536 elements to ensure when the array
	 * is read back it remains valid. A string takes up a byte for a null 
	 * marker, an unsigned short for the number of characters in it, and
	 * each character as a byte.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putStringArray(String[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putStrings(x);
		}
	}
	
	/**
	 * Writes an array of strings to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. A string takes up a byte for a null marker, an 
	 * unsigned short for the number of characters in it, and each character as 
	 * a byte.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putStrings(String[] x) 
	{
		for (int i = 0; i < x.length; i++) {
			putString(x[i]);
		}
	}

	/**
	 * Writes a unicode string to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. This will save the state of the string, so if 
	 * its null or empty and read back it will return null or empty. This is 
	 * accomplished by writing a single boolean to whether the string is null, 
	 * if its not null the length of the string is then written as an unsigned 
	 * short (0-65535) and finally the strings characters are written. A string 
	 * passed to this function must have fewer than 65536 characters to ensure 
	 * when the string is read back it remains valid. A string takes up 1 byte 
	 * as the null marker, an unsigned short for its length, and 2 bytes for 
	 * each of its characters.
	 * 
	 * @param s
	 * 		The string to write to the stream.
	 */
	public void putUnicode(String s) 
	{
		if (putIsNotNull(s)) {
			putUshort(s.length());
			putChars(s.toCharArray());
		}
	}

	/**
	 * Writes a unicode string array to the underlying stream. If the underlying 
	 * stream does not contain enough of free space to write it will be expanded 
	 * before it is written to. This will save the state of the array, so if its 
	 * null or empty and read back it will return null or empty. This is 
	 * accomplished by writing a single boolean to whether the array is null, if
	 * its not null the length of the array is then written as an unsigned short
	 * (0-65535) and finally the arrays elements are written. An array passed to 
	 * this function must have fewer than 65536 elements to ensure when the 
	 * array is read back it remains valid. A string takes up a byte for a null 
	 * marker, an unsigned short for the number of characters in it, and
	 * each character as 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUnicodeArray(String[] x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			putUnicodes(x);
		}
	}

	/**
	 * Writes an array of unicode strings to the underlying stream. If the 
	 * underlying stream does not contain enough of free space to write it will 
	 * be expanded before it is written to. A string takes up a byte for a null 
	 * marker, an unsigned short for the number of characters in it, and each 
	 * character as 2 bytes.
	 * 
	 * @param x
	 * 		The array to write to the stream.
	 */
	public void putUnicodes(String[] x) 
	{
		for (int i = 0; i < x.length; i++) {
			putUnicode(x[i]);
		}
	}
	
	/**
	 * Writes an enum to the underlying stream. If the underlying stream does 
	 * not contain enough of free space to write it will be expanded before it 
	 * is written to. This will save the state of the enum, so if its null and
	 * read back it will return null. An enum takes up one boolean for a null
	 * marker, and an unsigned short for the ordinal of the given enum.
	 * 
	 * @param <T>
	 * 		The enum type.
	 * @param x
	 * 		The enum to write to the stream.
	 */
	public <T extends Enum<T>> void putEnum(Enum<T> x) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.ordinal());
		}
	}
	
	/**
	 * Writes an object to the underlying stream using serialization. The
	 * object being written must be serializable or at least fit the 
	 * requirements to be serialized. If the underlying stream does not contain 
	 * enough of free space to write it will be expanded before it is written 
	 * to. This will save the state of the object, so if its null and read back 
	 * it will return null.
	 * 
	 * @param o
	 * 		The object to write to the stream.
	 */
	public void putObject(Object o) 
	{
		putItem(o, new SerializerCallback()); 
	}
	
	/**
	 * Writes an object to the underlying stream using a callback which 
	 * serializes the object. If the underlying stream does not contain enough 
	 * of free space to write it will be expanded before it is written to.
	 * 
	 * @param <T>
	 * 		The type of the written object.
	 * @param item
	 * 		The object to write to the stream.
	 * @param callback
	 * 		The callback to invoke to serialize the object with this writer.
	 */
	public <T> void putItem(T item, WriteCallback<T> callback) 
	{
		if (putIsNotNull(item)) {
			callback.write(this, item);
		}
	}
	
	/**
	 * Writes an array to the underlying stream. If the underlying stream does 
	 * not contain enough of free space to write it will be expanded before it 
	 * is written to. This will save the state of the array, so if its null or 
	 * empty and read back it will return null or empty. This is accomplished by
	 * writing a single boolean to whether the array is null, if its not null 
	 * the length of the array is then written as an unsigned short (0-65535) 
	 * and finally the elements in the array are written.
	 * 
	 * @param <T>
	 * 		The type of the written object.
	 * @param x
	 * 		The array of objects to write to the stream.
	 * @param callback
	 * 		The callback to invoke to serialize the objects with this writer.
	 */
	public <T> void putArray(T[] x, WriteCallback<T> callback) 
	{
		if (putIsNotNull(x)) {
			putUshort(x.length);
			for (int i = 0; i < x.length; i++) {
				if (putIsNotNull(x[i])) {
					callback.write(this, x[i]);
				}
			}
		}
	}
	
}
