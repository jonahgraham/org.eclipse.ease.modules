/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.module.platform;

import java.io.IOException;

public interface IFileHandle {
	int OFFSET_ENF_OF_FILE = -1;

	int READ = 1;
	int WRITE = 2;
	int APPEND = 4;
	int RANDOM_ACCESS = 8;

	String read(int characters) throws IOException;

	String readLine() throws IOException;

	boolean write(String data, int offset);

	boolean exists();

	boolean createFile(boolean createHierarchy) throws Exception;

	void close();
}
