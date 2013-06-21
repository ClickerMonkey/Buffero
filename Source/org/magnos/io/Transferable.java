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

import java.util.List;

/**
 * A Transferable class is able to dump its collection of information into a
 * list (by release) and then pass it to another Transferable which can take
 * any acceptable pieces of that information and return a list of denied 
 * information (by transfer).
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The transferable object.
 */
public interface Transferable<E> 
{
	
	/**
	 * Releases all elements from this class. This is the first step in 
	 * transfering the elements from this class into another Transferable.
	 * 
	 * @return
	 * 		A list of transferable elements.
	 */
	public List<E> release();
	
	/**
	 * Transfers all given elements into this class. This is the second step
	 * in transfering the elements from another Transferable into this class.
	 *  
	 * @param elements
	 * 		The list of transferable elements released from another class.
	 * @return
	 * 		The list of denied elements that could be transfered.
	 */
	public List<E> transfer(List<E> elements);
	
}