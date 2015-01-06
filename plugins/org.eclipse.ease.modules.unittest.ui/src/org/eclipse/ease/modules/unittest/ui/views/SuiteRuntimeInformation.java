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
package org.eclipse.ease.modules.unittest.ui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.ITestListener;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class SuiteRuntimeInformation implements ITestListener {

	private static final String XML_PARAMETER_TIMING = "timing";
	private static final String XML_PARAMETER_INDEX = "index";
	private static final String XML_NODE_RUN = "run";
	private static final String XML_NODE_FILE = "file";

	/** Typical script runtime. In case we have no other information. */
	private static final long DEFAULT_RUNTIME = 10000;
	private static final int RUNS_TO_SAVE = 10;

	private class RuntimeInformation {

		private final ArrayList<Long> fTimings = new ArrayList<Long>();

		public synchronized long getEstimatedRuntime() {
			if (fTimings.isEmpty())
				return DEFAULT_RUNTIME;

			// use a linear weighted average
			int weight = 0;
			long timing = 0;
			for (final Long time : fTimings) {
				weight++;
				timing += time * weight;
			}

			timing /= (weight + 1) * (weight / 2.0);

			return timing;
		}

		public synchronized void addRuntime(final long time) {
			fTimings.add(time);
		}

		public ArrayList<Long> getTimings() {
			return fTimings;
		}
	}

	private final HashMap<String, RuntimeInformation> fRuntimes = new HashMap<String, RuntimeInformation>();
	private List<TestFile> fTestFiles;
	private long fEstimatedEndOfTests;
	private final TestSuite fTestSuite;

	public SuiteRuntimeInformation(final TestSuite suite) {
		fTestSuite = suite;
		load();

		suite.addTestListener(this);
	}

	private void load() {
		try {
			final XMLMemento root = XMLMemento.createReadRoot(new FileReader(getSettingsFile()));

			final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
			for (final IMemento fileNode : root.getChildren(XML_NODE_FILE)) {
				for (final IMemento runNode : fileNode.getChildren(XML_NODE_RUN))
					// TODO currently ignore index, expect we read sequentially
					addTiming(fileNode.getTextData(), Long.parseLong(runNode.getString(XML_PARAMETER_TIMING)));
			}

		} catch (final Exception e) {
			// cannot load stats, ignore
		}
	}

	public synchronized void save() {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(getSettingsFile());

			// create xml output
			final XMLMemento rootNode = XMLMemento.createWriteRoot("root");
			for (final Entry<String, RuntimeInformation> entry : fRuntimes.entrySet()) {
				final IMemento fileNode = rootNode.createChild(XML_NODE_FILE);
				fileNode.putTextData(entry.getKey());

				final List<Long> timings = entry.getValue().getTimings();

				int nodeIndex = 0;
				for (int index = Math.max(0, timings.size() - RUNS_TO_SAVE); index < timings.size(); index++) {
					final IMemento runNode = fileNode.createChild(XML_NODE_RUN);
					runNode.putInteger(XML_PARAMETER_INDEX, nodeIndex++);
					runNode.putString(XML_PARAMETER_TIMING, Long.toString(timings.get(index)));
				}
			}

			rootNode.save(new OutputStreamWriter(outputStream));

		} catch (final Exception e) {
		} finally {
			try {
				outputStream.close();
			} catch (final IOException e) {
			}
		}
	}

	@Override
	public synchronized void notify(final Object testObject, final TestStatus status) {
		if ((testObject instanceof TestSuite) && (status == TestStatus.RUNNING)) {
			// testsuite started
			fTestFiles = fTestSuite.getActiveTestFiles();

			estimateEndOfTests();

		} else if ((testObject instanceof TestFile) && (status != TestStatus.RUNNING) && (status != TestStatus.NOT_RUN)) {
			addTiming(createTestToken((TestFile) testObject), ((TestFile) testObject).getExecutionTime());
			fTestFiles.remove(testObject);

			estimateEndOfTests();
		}
	}

	private static String createTestToken(TestFile testFile) {
		final Object file = testFile.getFile();

		if (file instanceof IFile)
			return ((IFile) file).getFullPath().toPortableString();

		if (file instanceof File)
			return ((File) file).getAbsolutePath();

		return file.toString();
	}

	private synchronized void addTiming(final String fileIdentifier, final long timing) {
		if (!fRuntimes.containsKey(fileIdentifier))
			fRuntimes.put(fileIdentifier, new RuntimeInformation());

		fRuntimes.get(fileIdentifier).addRuntime(timing);
	}

	private File getSettingsFile() {
		final IPath path = Activator.getDefault().getStateLocation().append("timing_" + fTestSuite.getModel().getFile().getProject().hashCode() + ".xml");
		return path.toFile();
	}

	private synchronized void estimateEndOfTests() {
		long time = 0;
		for (final TestFile file : fTestFiles) {
			final RuntimeInformation info = fRuntimes.get(createTestToken(file));
			time += (info != null) ? info.getEstimatedRuntime() : getAverageTestTime();
		}

		fEstimatedEndOfTests = System.currentTimeMillis() + time;
	}

	private synchronized long getAverageTestTime() {
		long time = 0;
		int items = 0;

		for (final RuntimeInformation info : fRuntimes.values()) {
			if (info.getEstimatedRuntime() > 0) {
				time += info.getEstimatedRuntime();
				items++;
			}
		}

		if (items > 0)
			return time / items;

		// we do not have any information at all, assume a typical test time of 10 seconds
		return DEFAULT_RUNTIME;
	}

	public long getEstimatedTestTime() {
		return fEstimatedEndOfTests - System.currentTimeMillis();
	}
}
