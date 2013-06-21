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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.magnos.io.bytes.ByteReader.ReadCallback;
import org.magnos.io.bytes.ByteWriter.WriteCallback;


/**
 * A callback for serializing and deserializing between bytes and Objects
 * 
 * @author Philip Diffenderfer
 *
 */
@SuppressWarnings("resource")
public class SerializerCallback implements ReadCallback<Object>, WriteCallback<Object>
{

	/**
	 * Deserializes an Object from the given ByteReader.
	 * 
	 * @param reader
	 * 		The ByteReader to read from.
	 */
	public Object read(ByteReader reader)
	{
		Object output = null;
		try {
			InputStream is = new ByteReaderStream(reader);
			ObjectInputStream ois = new ObjectInputStream(is);
			output = ois.readObject();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
		return output;
	}

	/**
	 * Serializes the given Object to the given ByteWriter.
	 * 
	 * @param writer
	 * 		The ByteWriter to write to.
	 * @param input
	 * 		The object to write to the ByteWriter.
	 */
	public void write(ByteWriter writer, Object input)
	{
		try {
			OutputStream os = new ByteWriterStream(writer);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(input);
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

}
