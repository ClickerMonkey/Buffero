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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.magnos.io.BufferStream;
import org.magnos.io.Buffers;
import org.magnos.io.DynamicBufferStream;
import org.magnos.io.buffer.BufferFactory;
import org.magnos.io.buffer.BufferFactoryDirect;
import org.magnos.test.BaseTest;


public class TestBufferStream extends BaseTest 
{

	public final BufferFactory factory = new BufferFactoryDirect();
	
	
	@Test
	public void testDrainStream() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 4, bs.drain( newInput(0, 1, 2, 3) ) );
		
		assertEquals( 4, bs.size() );
		
		ByteBuffer rd = bs.getReader(0); // TODO
		assertEquals( 4, rd.remaining() );
		assertEquals( 0, rd.get() );
		assertEquals( 1, rd.get() );
		assertEquals( 2, rd.get() );
		assertEquals( 3, rd.get() );
		assertEquals( 0, rd.remaining() );

		assertEquals( 4, bs.size() );
	}
	
	private InputStream newInput(int ... data) {
		byte bytes[] = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			bytes[i] = (byte)(data[i] & 0xFF);
		}
		return new ByteArrayInputStream(bytes);
	}
	
	@Test
	public void testDrainChannel() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 6, bs.drain( newReadable(0, 1, 2, 3, 4, 5) ) );

		assertEquals( 6, bs.size() );

		ByteBuffer rd = bs.getReader(0); // TODO
		assertEquals( 6, rd.remaining() );
		assertEquals( 0, rd.get() );
		assertEquals( 1, rd.get() );
		assertEquals( 2, rd.get() );
		assertEquals( 3, rd.get() );
		assertEquals( 4, rd.get() );
		assertEquals( 5, rd.get() );
		assertEquals( 0, rd.remaining() );

		assertEquals( 6, bs.size() );
		
	}
	
	private ReadableByteChannel newReadable(final int ... data) {
		return new ReadableByteChannel() {
			int index = 0;
			public boolean isOpen() {
				return true;
			}
			public void close() throws IOException {
			}
			public int read(ByteBuffer dst) throws IOException {
				if (index < data.length) {
					dst.put((byte)data[index++]);
					return 1;
				}
				return 0;
			}
		};
	}
	
	@Test
	public void testDrainBuffer() 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 5, bs.drain( newBuffer(4, 3, 2, 1, 0) ) );

		assertEquals( 5, bs.size() );

		ByteBuffer rd = bs.getReader(0); // TODO
		assertEquals( 5, rd.remaining() );
		assertEquals( 4, rd.get() );
		assertEquals( 3, rd.get() );
		assertEquals( 2, rd.get() );
		assertEquals( 1, rd.get() );
		assertEquals( 0, rd.get() );
		assertEquals( 0, rd.remaining() );

		assertEquals( 5, bs.size() );
	}
	
	private ByteBuffer newBuffer(int ... data) {
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		for (int i = 0; i < data.length; i++) {
			buffer.put((byte)data[i]);
		}
		buffer.flip();
		return buffer;
	}
	
	@Test
	public void testFillStream() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 12, bs.drain( newInput(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 10) ) );
		assertEquals( 12, bs.size() );
		
		AtomicReference<String> result = new AtomicReference<String>("");
		
		assertEquals( 12, bs.fill( newOutput(result) ) );
		assertEquals( 0, bs.size() );
		
		assertEquals( "Hello World\n", result.get() );
	}
	
	private OutputStream newOutput(final AtomicReference<String> result){
		return new OutputStream() {
			public void write(int b) throws IOException {
				result.set(result.get() + (char)b);
			}
		};
	}
	
	@Test
	public void testFillChannel() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 12, bs.drain( newInput(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 10) ) );
		assertEquals( 12, bs.size() );
		
		AtomicReference<String> result = new AtomicReference<String>("");
		
		assertEquals( 12, bs.fill( newWriteable(result) ) );
		assertEquals( 0, bs.size() );
		
		assertEquals( "Hello World\n", result.get() );
	}
	
	public WritableByteChannel newWriteable(final AtomicReference<String> result) {
		return new WritableByteChannel() {
			public boolean isOpen() {
				return true;
			}
			public void close() throws IOException {
			}
			public int write(ByteBuffer src) throws IOException {
				if (src.hasRemaining()) {
					result.set(result.get() + (char)src.get());
					return 1;
				}
				return 0;
			}
		};
	}
	
	@Test
	public void testFillBuffer() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);
		assertEquals( 0, bs.size() );
		
		assertEquals( 12, bs.drain( newInput(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 10) ) );
		assertEquals( 12, bs.size() );
		
		ByteBuffer result = factory.allocate();
		
		assertEquals( 12, bs.fill(result) );
		assertEquals( 0, bs.size() );
		
		result.flip();
		assertEquals( "Hello World\n", Buffers.toString(result) );
	}
	
	@Test
	public void testSkip() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);

		bs.drain( newInput(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 10) );
		bs.skip(6);
		
		ByteBuffer result = factory.allocate();
		
		assertEquals( 6, bs.fill(result) );
		assertEquals( 0, bs.size() );
		
		result.flip();
		assertEquals( "World\n", Buffers.toString(result) );
	}
	
	@Test
	public void testSync() throws IOException 
	{
		BufferStream bs = new DynamicBufferStream(null, factory);

		bs.drain( newInput(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 10) );
		
		ByteBuffer rd = bs.getReader(0); // TODO
		assertEquals( 72, rd.get() );
		assertEquals( 101, rd.get() );
		assertEquals( 108, rd.get() );
		assertEquals( 108, rd.get() );
		assertEquals( 111, rd.get() );
		assertEquals( 32, rd.get() );
		
		assertEquals( 12, bs.size() );
		bs.sync(rd);
		assertEquals( 6, bs.size() );
		
		ByteBuffer re = bs.getReader(0); // TODO
		assertEquals( 87, re.get() );
		
		assertEquals( 6, bs.size() );
	}
	
}
