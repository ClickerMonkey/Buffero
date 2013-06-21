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
import org.magnos.io.buffer.BufferFactoryHeap;


/**
 * A utility for working with ByteBuffers and BufferFactorys.
 * 
 * @author Philip Diffenderfer
 *
 */
public class Buffers 
{

	// The default buffer factory for any ByteBuffer creation.
	private static BufferFactory factory = new BufferFactoryHeap();
	
	// An empty ByteBuffer
	public static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	
	
	/**
	 * Sets the default buffer factory used by the utility functions 
	 * provided in this class.
	 * 
	 * @param factory
	 * 		The new default factory to use.
	 */
	public static void setDefaultFactory(BufferFactory factory)
	{
		Buffers.factory = factory;
	}
	
	/**
	 * Disposes of the given buffer by disposing it to the default factory
	 * for this BufferUtil. This should be called after a toBuffer or doubleSize
	 * when the buffer has no more use.
	 * 
	 * @param buffer 
	 * 		The buffer to dispose.
	 */
	public static void dispose(ByteBuffer buffer)
	{
		Buffers.factory.free(buffer);
	}
	
	/**
	 * Returns whether the given ByteBuffers are identical. ByteBuffers are
	 * identical when their bytes between their positions and limits are equal.
	 * The positions and limits of the byte buffers are not affected by this.
	 * 
	 * @param a
	 * 		The first ByteBuffer to compare.
	 * @param b 
	 * 		The second ByteBuffer to compare.
	 * @return 
	 * 		True if the ByteBuffers are equal within their current bounds.
	 */
	public static boolean equals(ByteBuffer a, ByteBuffer b)
	{
		if (a.remaining() != b.remaining()) {
			return false;
		}
		
		int posA = a.position();
		int posB = b.position();
		int limA = a.limit();
		
		while (posA < limA) {
			if (a.get(posA) != b.get(posB)) {
				return false;
			}
			
			posA++;
			posB++;
		}
		
		return true;
	}
	
	/**
	 * Returns whether the given sections of the ByteBuffers are identical. 
	 * The position and limit of the buffers have no effect on their equality,
	 * the offsets given and the length are technically their position and limit
	 * but it doesn't affect the actual positions and limits of the given buffers.
	 * If the combination of the offsets and the length go outside the bounds of
	 * a buffer then false is immediately returned.
	 * 
	 * @param a 
	 * 		The first ByteBuffer to compare.
	 * @param offsetA 
	 * 		The offset in bytes in the first buffer to start comparing.
	 * @param b
	 * 		The second ByteBuffer to compare.
	 * @param offsetB
	 * 		The offset in bytes in the second buffer to start comparing.
	 * @param length 
	 * 		The number of bytes in each buffer to compare.
	 * @return 
	 * 		True if the ByteBuffers are equal within their current bounds.
	 */
	public static boolean equals(ByteBuffer a, int offsetA, ByteBuffer b, int offsetB, int length)
	{
		if ((offsetA + length) > a.capacity() || (offsetB + length) > b.capacity()) {
			return false;
		}
		
		int limA = offsetA + length;
		
		while (offsetA < limA) {
			if (a.get(offsetA) != b.get(offsetB)) {
				return false;
			}
			
			offsetA++;
			offsetB++;
		}
		
		return true;
	}
	
	/**
	 * Transfers as many bytes as possible from a source buffer to a
	 * destination buffer and returns how many bytes were transfered.
	 * 
	 * @param src 
	 * 		The source buffer to transfer bytes from.
	 * @param dst 
	 * 		The destination buffer to transfer bytes to.
	 * @return 
	 * 		The number of bytes transfered.
	 */
	public static int fill(ByteBuffer src, ByteBuffer dst)
	{
		// The number of bytes to transfer from the source to destination.
		int transfer = Math.min(dst.remaining(), src.remaining());
		
		// Copy that many bytes over.
		for (int i = 0; i < transfer; i++) {
			dst.put(src.get());
		}
		
		return transfer;	
	}
	
	/**
	 * Transfers a given number of bytes from a source buffer to a 
	 * destination buffer. The given number of bytes is trusted in the fact
	 * that this method doesn't do checking to determine whether the number
	 * of bytes to transfer actually exist in either of the buffers.
	 * 
	 * @param src 
	 * 		The source buffer to transfer bytes from.
	 * @param dst 
	 * 		The destination buffer to transfer bytes to.
	 * @param transfer 
	 * 		The number of bytes to transfer.
	 * @return 
	 * 		The number of bytes transfered.
	 */
	public static int fill(ByteBuffer src, ByteBuffer dst, int transfer)
	{
		// Copy that many bytes over.
		for (int i = 0; i < transfer; i++) {
			dst.put(src.get());
		}
		
		return transfer;	
	}

	/**
	 * Given a ByteBuffer this will return a buffer double the size with
	 * the same data and position. The ByteBuffer given will be disposed, do not
	 * use this method if you plan on using old after this method call.
	 * 
	 * @param old 
	 * 		The old buffer to copy data from.
	 */
	public static ByteBuffer doubleSize(ByteBuffer old)
	{
		// Create the new one with double the capacity
		ByteBuffer buffer = factory.allocate(old.capacity() << 1);
		
		// Copy the old data over.
		old.flip();
		buffer.put(old);
		
		// Dispose the old buffer
		factory.free(old);
		
		// Return the new buffer
		return buffer;
	}
	
	/**
	 * Converts the remaining bytes from a {@link ByteBuffer} to a String. 
	 * 
	 * @param buffer
	 * 		The ByteBuffer to convert.
	 */
	public static String toString(ByteBuffer buffer)
	{
		int total = buffer.remaining();
		char[] chars = new char[total];
		int offset = buffer.position();

		// Convert from bytes to chars
		for (int i = 0; i < total; i++) {
			chars[i] = (char)buffer.get(i + offset);
		}
		
		return new String(chars);
	}
	
	/**
	 * Converts the remaining bytes from a {@link ByteBuffer} to a String. 
	 * 
	 * @param buffer 
	 * 		The ByteBuffer to convert.
	 */
	public static String toString(ByteBuffer buffer, int start, int length)
	{
		int total = Math.min(length, buffer.capacity() - start);
		char[] chars = new char[total];
		
		// Convert from bytes to chars
		for (int i = 0; i < total; i++) {
			chars[i] = (char)buffer.get(start + i);
		}
		
		return new String(chars);
	}
	
	/**
	 * Converts the given String to a ByteBuffer. The position of the buffer
	 * return is 0 and the limit and capacity match the length of the given
	 * String.
	 * 
	 * @param value 
	 * 		The ByteBuffer to convert.
	 */
	public static ByteBuffer toBuffer(String value)
	{
		if (value == null || value.length() == 0) {
			return EMPTY;
		}
		
		ByteBuffer buffer = factory.allocate(value.length());
		char[] chars = value.toCharArray();
		
		// Convert from chars to bytes
		for (int i = 0; i < chars.length; i++) {
			buffer.put((byte)chars[i]);
		}
		buffer.flip();
		
		return buffer;
	}
	
	/**
	 * Gets a string out of the given buffer. The next 2 bytes in the buffer is
	 * the length of the string in characters and then that string follows as
	 * a byte array.
	 * 
	 * @param buffer
	 * 		The buffer to get the string from.
	 */
	public static String readString(ByteBuffer buffer)
	{
		// Check that the buffer contains a short with the length of the string
		if (buffer.remaining() < 2) {
			return null;
		}
		
		// The first 2 bytes is the string length.
		int stringLength = buffer.getShort();
		
		// Check that the buffer contains the entire string.
		if (buffer.remaining() < stringLength) {
			return null;
		}
		
		// Grab the entire string as an array of bytes
		char[] chars = new char[stringLength];

		for (int i = 0; i < stringLength; i++) {
			chars[i] = (char)buffer.get();
		}
			
		return new String(chars);
	}
	
	/**
	 * Puts the given string into the buffer where the first 2 bytes put in the
	 * buffer is the length of the string and the following bytes are the
	 * characters of the string.
	 * 
	 * @param buffer 
	 * 		The ByteBuffer to put the string into.
	 * @param value
	 * 		The string to put into the ByteBuffer.
	 */
	public static boolean writeString(ByteBuffer buffer, String value)
	{
		if (buffer.remaining() < getStoredSpace(value)) {
			return false;
		}
		
		char[] chars = value.toCharArray();
	
		buffer.putShort((short)chars.length);
		
		// Convert from chars to bytes
		for (int i = 0; i < chars.length; i++) {
			buffer.put((byte)chars[i]);
		}
		
		return true;
	}
	
	/**
	 * Given a string this will return its length in bytes when written to a
	 * ByteBuffer using putString().
	 * 
	 * @param values 
	 * 		The strings to measure.
	 */
	public static int getStoredSpace(String ... values)
	{
		int total = values.length << 1;
		
		for (int i = 0; i < values.length; i++) {
			total += values[i].length();
		}
		
		return total;
	}
	
}
