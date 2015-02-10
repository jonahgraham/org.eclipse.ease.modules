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
package org.eclipse.ease.modules.unittest.components;

import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.unittest.Bundle;
import org.eclipse.ease.modules.unittest.modules.UnitModule;
import org.eclipse.ease.tools.ResourceTools;

public class TestFile extends TestComposite implements Comparable<TestFile> {

	private class TestFileJob extends Job {

		public TestFileJob(final String name) {
			super(name);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {

			// clear old tests
			reset();
			setStatus(TestStatus.RUNNING);

			// setup engine
			setScriptEngine(getTestSuite().createScriptEngine());

			// connect output streams
			getScriptEngine().setOutputStream(getTestSuite().getOutputStream());
			getScriptEngine().setErrorStream(getTestSuite().getErrorStream());

			// add variables
			getScriptEngine().addExecutionListener(TestFile.this);
			getScriptEngine().setTerminateOnIdle(false);
			for (final Entry<String, Object> entry : getTestSuite().getVariables().entrySet())
				if (!entry.getKey().startsWith(EnvironmentModule.MODULE_PREFIX))
					getScriptEngine().setVariable(entry.getKey(), entry.getValue());

			getScriptEngine().setVariable(CURRENT_TESTCOMPOSITE, TestFile.this);

			// load unit module
			// TODO make sure this is compatible with any engine
			getScriptEngine().executeAsync("loadModule('" + UnitModule.MODULE_NAME + "')");

			// start engine
			getScriptEngine().schedule();

			boolean runTeardown = true;
			try {
				// check for user abort request
				if (monitor.isCanceled()) {
					addTestResult(TestStatus.FAILURE, "Test aborted by user");
					return Status.CANCEL_STATUS;
				}

				// testFile setup()
				if (!runCodeFragment(TestSuiteModel.CODE_LOCATION_TESTFILE_SETUP, monitor))
					return Status.OK_STATUS;

				// check for abort request
				if (monitor.isCanceled()) {
					addTestResult(TestStatus.FAILURE, "Test aborted by user");
					return Status.CANCEL_STATUS;
				}

				// execute test code
				final ScriptResult testFileResult = getScriptEngine().executeSync(
						ResourceTools.resolveFile(fFileLocation, getTestSuite().getModel().getFile(), true));
				if (testFileResult.hasException()) {
					// this is probably an exception due to calling exit()
					if (!(testFileResult.getException() instanceof ExitException)) {
						// we had a real exception, inform user
						addTestResult(TestStatus.FAILURE, TestSuite.getExceptionMessage(testFileResult.getException()));
						runTeardown = getTestSuite().getModel().getFlag(TestSuiteModel.FLAG_EXECUTE_TEARDOWN_ON_FAILURE, true);
						return Status.OK_STATUS;
					}
				}

				// check for abort request
				if (monitor.isCanceled()) {
					addTestResult(TestStatus.FAILURE, "Test aborted by user");
					return Status.CANCEL_STATUS;
				}

			} catch (final InterruptedException e) {
				runTeardown = getTestSuite().getModel().getFlag(TestSuiteModel.FLAG_EXECUTE_TEARDOWN_ON_FAILURE, true);
				return Status.CANCEL_STATUS;

			} finally {
				// testFile teardown()
				if (runTeardown) {
					try {
						runCodeFragment(TestSuiteModel.CODE_LOCATION_TESTFILE_TEARDOWN, monitor);
					} catch (final InterruptedException e) {
						// TODO handle this exception (but for now, at least know it happened)
						throw new RuntimeException(e);
					}
				}

				// terminate all tests that are still marked as running
				// used for badly written test cases and when tests fail by throwing an exception
				for (final Test test : getTests())
					test.setStatus(TestStatus.PASS);

				getScriptEngine().terminate();
				setScriptEngine(null);

				setStatus(TestStatus.PASS);
			}

			return Status.OK_STATUS;
		}

		private boolean runCodeFragment(final String fragmentID, final IProgressMonitor monitor) throws InterruptedException {
			final String fragmentCode = getCodeFragment(fragmentID);
			if ((fragmentCode != null) && (!fragmentCode.trim().isEmpty())) {

				addTest(new Test(TestFile.this, "[" + fragmentID + "]"));
				final ScriptResult setupResult = getScriptEngine().executeSync(fragmentCode);

				if (setupResult.hasException()) {
					// testFile setup failed
					addTestResult(TestStatus.FAILURE, TestSuite.getExceptionMessage(setupResult.getException()));
					endTest();
					return false;
				}

				endTest();
			}

			return true;
		}
	}

	private TestFileJob fJob;
	private final String fFileLocation;

	public TestFile(final TestSuite suite, final String fileLocation) {
		super(suite);
		fFileLocation = fileLocation;
	}

	public void execute() {
		fJob = new TestFileJob("TestFile: " + toString());
		fJob.schedule();
	}

	public String getCodeFragment(final String identifier) {
		return getTestSuite().getModel().getCodeFragment(identifier);
	}

	public void terminate() {
		if (fJob != null)
			fJob.cancel();
	}

	public TestSuite getTestSuite() {
		return (TestSuite) getParent();
	}

	@Override
	public Collection<? extends TestEntity> getChildren() {
		return getTests();
	}

	@Override
	public void reset() {
		final IFile parent = (getTestSuite() != null) ? getTestSuite().getModel().getFile() : null;
		final Object file = ResourceTools.resolveFile(fFileLocation, parent, true);
		if (file instanceof IFile) {
			try {
				((IFile) file).deleteMarkers(Bundle.PLUGIN_ID + ".scriptassertion", false, IResource.DEPTH_ZERO);
			} catch (final CoreException e) {
				// TODO handle this exception (but for now, at least know it happened)
				throw new RuntimeException(e);
			}
		}

		super.reset();
	}

	@Override
	public int compareTo(final TestFile o) {
		return fFileLocation.compareTo(o.fFileLocation);
	}

	public Object getFile() {
		return ResourceTools.resolveFile(fFileLocation, getTestSuite().getModel().getFile(), true);
	}

	@Override
	public String toString() {
		final Object file = getFile();
		if (file instanceof IFile)
			return ((IFile) file).getName();

		if (file instanceof File)
			return ((File) file).getName();

		return super.toString();
	}
}
