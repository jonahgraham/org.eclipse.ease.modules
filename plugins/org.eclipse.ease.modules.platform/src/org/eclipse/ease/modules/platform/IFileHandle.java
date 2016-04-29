/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.platform;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;

/**
 * Generic handle to an {@link IFile} or {@link File} instance.
 */
public interface IFileHandle {

	/** Open file in read mode. */
	int READ = 1;

	/** Open file in write mode. */
	int WRITE = 2;

	/** Open file in append mode. */
	int APPEND = 4;

	/**
	 * Read characters from a file.
	 *
	 * @param characters
	 *            amount of characters to read
	 * @return data read from file
	 * @throws IOException
	 *             on access errors
	 */
	String read(int characters) throws IOException;

	/**
	 * Read a line of data from a file. Reads until a line feed is detected.
	 *
	 * @return single line of text
	 * @throws IOException
	 *             on access errors
	 */
	String readLine() throws IOException;

	/**
	 * Write data to a file. Uses platform default encoding to write strings to the file.
	 *
	 * @param data
	 *            data to write
	 * @return <code>true</code> on success
	 */
	boolean write(String data);

	/**
	 * Write data to a file.
	 *
	 * @param data
	 *            data to write
	 * @return <code>true</code> on success
	 */
	boolean write(byte[] data);

	/**
	 * Check if a physical file exists.
	 *
	 * @return <code>true</code> when file exists
	 */

	boolean exists();

	/**
	 * Create a file.
	 *
	 * @param createHierarchy
	 *            create parent folders if they do not exist
	 * @return <code>true</code> on success
	 * @throws Exception
	 *             on creation errors
	 */
	boolean createFile(boolean createHierarchy) throws Exception;

	/**
	 * Close a file instance.
	 */
	void close();

	/**
	 * Get the base file object. Returns an {@link IFile} or a {@link File} instance.
	 *
	 * @return base file object
	 */
	Object getFile();
}
