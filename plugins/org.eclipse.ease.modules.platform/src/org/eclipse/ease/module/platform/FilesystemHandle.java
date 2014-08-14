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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

public class FilesystemHandle implements IFileHandle {

	private final File fFile;
	private int fMode;
	protected BufferedReader fReader = null;
	private PrintWriter fWriter = null;

	public FilesystemHandle(final File file, final int mode) {
		fFile = file;
		fMode = mode;
	}

	protected BufferedReader createReader() throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fFile)));
	}

	private BufferedReader getReader() {
		try {
			if (fReader == null)
				fReader = createReader();

		} catch (Exception e) {
		}

		return fReader;
	}

	@Override
	public String read(final int characters) throws IOException {
		BufferedReader reader = getReader();
		if (reader != null) {
			if (characters >= 0) {
				// read dedicated amount of characters
				char[] buffer = new char[characters];
				int length = reader.read(buffer);

				if (length >= 0)
					return new String(buffer, 0, length);

			} else {
				// read rest of file
				StringBuilder data = read(reader);
				if (data != null)
					return data.toString();
			}
			// did not work, close reader
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		return null;
	}

	@Override
	public String readLine() throws IOException {
		BufferedReader reader = getReader();
		if (reader != null)
			return reader.readLine();

		return null;
	}

	protected Writer createWriter() throws Exception {
		return new FileWriter(fFile, (fMode & APPEND) == APPEND);
	}

	@Override
	public boolean write(final String data, int offset) {
		try {
			if (fMode == RANDOM_ACCESS) {
				// random write access to file
				BufferedReader reader = createReader();
				StringBuilder buffer = read(reader);

				if (offset == OFFSET_ENF_OF_FILE)
					offset = buffer.length();

				buffer.insert(Math.min(buffer.length(), offset), data);

				PrintWriter writer = new PrintWriter(new BufferedWriter(createWriter()));
				writer.write(buffer.toString());
				writer.close();

				try {
					if (fReader != null)
						fReader.close();
				} catch (Exception e) {
				}

			} else {
				// replace file content or append content
				if (fWriter == null)
					fWriter = new PrintWriter(new BufferedWriter(createWriter()));

				fWriter.print(data);
				fWriter.flush();
				return true;
			}

		} catch (Exception e) {
		}

		return false;
	}

	protected static StringBuilder read(final Reader reader) throws IOException {
		// consume reader
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[1024];
		int bytes = 0;
		do {
			bytes = reader.read(buffer);
			builder.append(buffer, 0, Math.max(bytes, 0));
		} while (bytes != -1);

		if (builder.length() > 0)
			return builder;

		return null;
	}

	@Override
	public boolean exists() {
		return fFile.exists();
	}

	@Override
	public boolean createFile(final boolean createHierarchy) throws Exception {
		if (createHierarchy) {
			File folder = fFile.getParentFile();
			if (!folder.exists())
				folder.mkdirs();
		}

		return fFile.createNewFile();
	}

	public void setMode(final int mode) {
		fMode = mode;
	}

	protected int getMode() {
		return fMode;
	}

	@Override
	protected void finalize() throws Throwable {
		close();

		super.finalize();
	}

	@Override
	public void close() {
		try {
			if (fReader != null)
				fReader.close();
		} catch (IOException e) {
		}

		try {
			if (fWriter != null)
				fWriter.close();
		} catch (Exception e) {
		}
	}
}
