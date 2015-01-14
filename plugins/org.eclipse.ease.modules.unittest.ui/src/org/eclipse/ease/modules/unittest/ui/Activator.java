/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.ease.modules.unittest.ui";

	/** Max amount of testsuite files to store. */
	private static final int MAX_RECENT_FILES = 5;

	/** Activator instance. */
	private static Activator fInstance;

	public static Activator getDefault() {
		return fInstance;
	}

	/** Recent testsuite files. */
	private final List<IFile> fRecentFiles = new ArrayList<IFile>();

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		loadRecentFiles();

		fInstance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		fInstance = null;

		saveRecentFiles();

		super.stop(context);
	}

	private void loadRecentFiles() {
		final File file = getStateLocation().append("recentFiles.txt").toFile();
		if (file.exists()) {

			Reader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				final String data = ResourceTools.toString(reader);
				for (final String fileName : data.split(";")) {
					if (!fileName.isEmpty()) {
						final IFile suiteFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
						if ((suiteFile != null) && (suiteFile.exists()))
							fRecentFiles.add(suiteFile);
					}
				}
			} catch (final IOException e) {
				// could not read recent files, ignore
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						// giving up
					}
				}
			}
		}
	}

	private void saveRecentFiles() {

		final StringBuilder buffer = new StringBuilder();
		for (final IFile file : getRecentFiles()) {
			buffer.append(';');
			buffer.append(file.getFullPath().toString());
		}
		if (buffer.length() > 0)
			buffer.delete(0, 1);

		// write data to config file
		final File file = getStateLocation().append("recentFiles.txt").toFile();
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(buffer.toString());

		} catch (final IOException e) {
		} finally {
			// gracefully close writer
			try {
				if (writer != null)
					writer.close();
			} catch (final IOException e) {
			}
		}
	}

	public void addRecentFile(final IFile file) {
		// remove if file already exists in list
		fRecentFiles.remove(file);

		// add new file on topmost position
		fRecentFiles.add(0, file);

		// avoid overflow
		while (fRecentFiles.size() >= MAX_RECENT_FILES)
			fRecentFiles.remove(MAX_RECENT_FILES - 1);
	}

	public List<IFile> getRecentFiles() {
		return Collections.unmodifiableList(fRecentFiles);
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
