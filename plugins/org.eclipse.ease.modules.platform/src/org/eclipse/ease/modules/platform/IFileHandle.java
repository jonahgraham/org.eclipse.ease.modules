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

public interface IFileHandle {
	int READ = 1;
	int WRITE = 2;
	int APPEND = 4;

	String read(int characters) throws IOException;

	String readLine() throws IOException;

	boolean write(String data);

	boolean exists();

	boolean createFile(boolean createHierarchy) throws Exception;

	void close();

	/**
	 * Get the base file object. Returns an {@link IFile} or a {@link File} instance.
	 *
	 * @return base file object
	 */
	Object getFile();
}
