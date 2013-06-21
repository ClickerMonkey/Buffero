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
import java.io.InputStream;

/**
 * A wrapper to disguise a ByteReader as an InputStream.
 *  
 * @author Philip Diffenderfer
 *
 */
public class ByteReaderStream extends InputStream 
{
	
	// The internal ByteReader.
	private final ByteReader reader;
	
	/**
	 * Instantiates a new ByteReaderStream.
	 * 
	 * @param reader
	 * 		The internal ByteReader.
	 */
	public ByteReaderStream(ByteReader reader) 
	{
		this.reader = reader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException 
	{
		if (reader.hasByte()) {
			return reader.getUbyte(); 
		}
		return -1;
	}

}
