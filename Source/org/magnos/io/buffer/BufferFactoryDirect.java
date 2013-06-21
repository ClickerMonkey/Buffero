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
 * A BufferFactory which does not cache and uses DirectByteBuffers.
 * 
 * @author Philip Diffenderfer
 *
 */
public class BufferFactoryDirect extends AbstractBufferFactory 
{

	private static List<ByteBuffer> EMPTY = new ArrayList<ByteBuffer>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ByteBuffer onAllocate(int size, AtomicBoolean cache) 
	{
		try {
			return ByteBuffer.allocateDirect(size);
		}
		catch (OutOfMemoryError e) {
			System.err.format("Cannot allocate a ByteBuffer of size %d; out of memory.\n", size);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean onCache(ByteBuffer buffer) 
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long onFill() 
	{
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ByteBuffer> onRelease() 
	{
		return EMPTY;
	}

}
