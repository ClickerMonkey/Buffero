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

import java.io.IOException;
import java.io.OutputStream;

/**
 * A wrapper to disguise a ByteWriter as an OutputStream.
 *  
 * @author Philip Diffenderfer
 *
 */
public class ByteWriterStream extends OutputStream 
{

	// The internal ByteWriter.
	private final ByteWriter writer;
	
	/**
	 * Instantiates a new ByteWriterStream.
	 * 
	 * @param writer
	 * 		The internal ByteWriter.
	 */
	public ByteWriterStream(ByteWriter writer) 
	{
		this.writer = writer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException 
	{
		writer.putByte((byte)b);
	}

}
