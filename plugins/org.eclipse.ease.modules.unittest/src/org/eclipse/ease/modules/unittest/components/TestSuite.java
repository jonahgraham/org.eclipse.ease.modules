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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.modules.unittest.Bundle;
import org.eclipse.ease.modules.unittest.ITestSetFilter;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.ease.modules.unittest.modules.UnitTestModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.launching.LaunchConstants;

public class TestSuite extends TestComposite {

	/**
	 * Get a simplified exception message.
	 *
	 * @param exception
	 *            exception to parse
	 * @return simplified message.
	 */
	public static String getExceptionMessage(final Throwable exception) {
		String message = exception.getLocalizedMessage();

		if ((message == null) || (message.isEmpty()))
			message = exception.getMessage();

		if ((message == null) || (message.isEmpty())) {
			message = exception.getClass().getName();
			message += " (" + exception.getStackTrace()[0].toString() + ")";
		}

		return message;
	}

	private final TestSuiteModel fTestModel;
	private boolean fTerminated = false;
	private final Map<String, TestFile> fTestFiles = new HashMap<String, TestFile>();

	private int fCurrentTestCount;
	private List<TestFile> fActiveTestFiles = Collections.emptyList();

	private OutputStream fOutputStream = System.out;
	private OutputStream fErrorStream = System.err;
	private InputStream fInputStream = System.in;

	private Map<String, Object> fSetupVariables;
	private ILaunch fDebugLaunch;

	private class TestSuiteJob extends Job {

		public TestSuiteJob(final String name) {
			super(name);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			// fire testsuite event
			reset();
			setStatus(TestStatus.RUNNING);

			// delete markers & results of testFiles
			for (final TestFile file : fActiveTestFiles)
				file.reset();

			// create master engine, performing testsuite setup & teardown
			setScriptEngine(createScriptEngine());
			getScriptEngine().setTerminateOnIdle(false);

			getScriptEngine().setOutputStream(getOutputStream());
			getScriptEngine().setErrorStream(getErrorStream());

			getScriptEngine().setVariable(UnitTestModule.FAIL_ON_ERROR_VARIABLE, getModel().getFlag(TestSuiteModel.FLAG_PROMOTE_ERRORS_TO_FAILURES, false));
			getScriptEngine().setVariable(CURRENT_TESTCOMPOSITE, TestSuite.this);

			getScriptEngine().executeAsync("loadModule('" + UnitTestModule.MODULE_NAME + "')");

			getScriptEngine().schedule();

			fCurrentTestCount = 0;

			// parse variables
			final StringBuffer variablesCode = new StringBuffer();
			for (final Variable var : fTestModel.getVariables())
				variablesCode.append(var.getName()).append(" = ").append(var.getContent()).append(Bundle.LINE_DELIMITER);

			try {
				// execute variables code
				if (!runCode("Variable definitions", variablesCode.toString(), monitor))
					return Status.OK_STATUS;

				// execute testsuite setup code
				if (!runCodeFragment(TestSuiteModel.CODE_LOCATION_TESTSUITE_SETUP, monitor))
					return Status.OK_STATUS;

				// setup done; extract variables
				fSetupVariables = getScriptEngine().getVariables();
				final ArrayList<TestFile> launchedTestFiles = new ArrayList<TestFile>();

				// main test execution loop
				final List<TestFile> filesUnderTest = new ArrayList<TestFile>(fActiveTestFiles);
				synchronized (TestSuite.this) {
					while ((!fTerminated) && ((fCurrentTestCount > 0) || (!filesUnderTest.isEmpty()))) {

						// always read number of threads. some testsuites adjust load dynamically
						final int maxSimultaneousThreads = Math.max(1, fTestModel.getFlag(TestSuiteModel.FLAG_MAX_THREADS, 1));

						if ((!filesUnderTest.isEmpty()) && (fCurrentTestCount < maxSimultaneousThreads)) {
							// start another test
							final TestFile testFile = filesUnderTest.remove(0);
							launchedTestFiles.add(testFile);

							testFile.addTestListener(TestSuite.this);

							fCurrentTestCount++;
							testFile.execute();

						} else
							// wait for tests to finish
							TestSuite.this.wait();
					}
				}

				if (fTerminated) {
					// terminate all running test files
					for (final TestFile testFile : launchedTestFiles)
						testFile.terminate();

					// wait for files to shut down
					synchronized (TestSuite.this) {
						while (fCurrentTestCount > 0)
							// wait for tests to finish
							TestSuite.this.wait();
					}
				}

			} catch (final InterruptedException e) {
				return Status.CANCEL_STATUS;

			} finally {
				// run teardown code
				if (getModel().getFlag(TestSuiteModel.FLAG_EXECUTE_TEARDOWN_ON_FAILURE, true)) {
					try {
						runCodeFragment(TestSuiteModel.CODE_LOCATION_TESTSUITE_TEARDOWN, monitor);
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
			return runCode(fragmentID, getCodeFragment(fragmentID), monitor);
		}

		private boolean runCode(final String identifier, final String code, final IProgressMonitor monitor) throws InterruptedException {
			if ((code != null) && (!code.trim().isEmpty())) {

				addTest(new Test(TestSuite.this, "[" + identifier + "]", true));
				final ScriptResult result = getScriptEngine().executeSync(code);

				if (result.hasException()) {
					// testFile setup failed
					final ArrayList<IScriptDebugFrame> trace = new ArrayList<IScriptDebugFrame>();
					// TODO eventually get the trace from the engine. Needs engine to keep traces on failures
					trace.add(new ScriptDebugFrame(new Script("[" + identifier + "]", ""), 0, IScriptDebugFrame.TYPE_FILE));

					addTestResult(TestStatus.FAILURE, TestSuite.getExceptionMessage(result.getException()), trace);
					endTest();
					return false;
				}

				endTest();
			}

			return true;
		}
	}

	public TestSuite(final TestSuiteModel model) {
		super(null);

		fTestModel = model;

		for (final String location : fTestModel.getTestFiles())
			fTestFiles.put(location, new TestFile(this, location));
	}

	public TestSuite(final IFile file) throws IOException, CoreException {
		this(new TestSuiteModel(file));
	}

	@Override
	protected void finalize() throws Throwable {
		fTestModel.close();

		super.finalize();
	}

	private String getCodeFragment(final String name) {
		return fTestModel.getCodeFragment(name);
	}

	@Override
	public void notify(final Object testObject, final TestStatus status) {
		super.notify(testObject, status);

		if ((testObject instanceof TestFile) && (status != TestStatus.RUNNING)) {
			fCurrentTestCount--;

			final boolean stopOnFailure = fTestModel.getFlag(TestSuiteModel.FLAG_STOP_SUITE_ON_FAILURE, false);
			if ((stopOnFailure) && (status == TestStatus.FAILURE))
				terminate();

			else {
				// reactivate TestSuite Job
				synchronized (this) {
					notifyAll();
				}
			}
		}
	}

	@Override
	public Collection<TestFile> getChildren() {
		return fTestFiles.values();
	}

	public void run() {
		run(ITestSetFilter.ALL);
	}

	public void run(final ITestSetFilter filter) {

		// filter tests
		fActiveTestFiles = new LinkedList<TestFile>();
		for (final TestFile testFile : fTestFiles.values()) {
			if (filter.matches(testFile))
				fActiveTestFiles.add(testFile);
		}

		// sort tests
		Collections.sort(fActiveTestFiles);

		if (!fActiveTestFiles.isEmpty())
			// run TestSuite
			new TestSuiteJob("Testsuite " + toString()).schedule();

		else
			setStatus(TestStatus.PASS);
	}

	public synchronized void terminate() {
		fTerminated = true;

		// reactivate TestSuite Job
		synchronized (this) {
			notifyAll();
		}
	}

	public TestSuiteModel getModel() {
		return fTestModel;
	}

	public int getActiveTestCount() {
		return fActiveTestFiles.size();
	}

	public void setOutputStream(final OutputStream outputStream) {
		if (outputStream != null)
			fOutputStream = outputStream;
	}

	public OutputStream getOutputStream() {
		return fOutputStream;
	}

	public OutputStream getErrorStream() {
		return fErrorStream;
	}

	public void setErrorStream(final InputStream inputStream) {
		if (inputStream != null)
			fInputStream = inputStream;
	}

	public InputStream getInputStream() {
		return fInputStream;
	}

	public List<TestFile> getActiveTestFiles() {
		return new ArrayList<TestFile>(fActiveTestFiles);
	}

	public void setErrorStream(final OutputStream errorStream) {
		if (errorStream != null)
			fErrorStream = errorStream;
	}

	public IScriptEngine createScriptEngine() {
		final IScriptService scriptService = ScriptService.getService();

		if (fDebugLaunch != null) {

			final ILaunchConfiguration configuration = fDebugLaunch.getLaunchConfiguration();
			String engineID = "";
			try {
				engineID = configuration.getAttribute(LaunchConstants.SCRIPT_ENGINE, "");
			} catch (final CoreException e1) {
			}

			final IScriptEngine engine = scriptService.getEngineByID(engineID).createEngine();
			if (engine instanceof IDebugEngine) {
				boolean suspendOnStartup = false;
				try {
					suspendOnStartup = configuration.getAttribute(LaunchConstants.SUSPEND_ON_STARTUP, false);
				} catch (final CoreException e) {
				}

				boolean suspendOnScriptLoad = false;
				try {
					suspendOnScriptLoad = configuration.getAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, false);
				} catch (final CoreException e) {
				}

				boolean showDynamicCode = false;
				try {
					showDynamicCode = configuration.getAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, false);
				} catch (final CoreException e) {
				}

				((IDebugEngine) engine).setupDebugger(fDebugLaunch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
			}

			return engine;
		}

		return scriptService.getEngineByID("org.eclipse.ease.javascript.rhinoDebugger").createEngine();
	}

	@Override
	public void reset() {
		fTerminated = false;

		try {
			fTestModel.getFile().deleteMarkers(Bundle.PLUGIN_ID + ".scriptassertion", false, IResource.DEPTH_ZERO);
		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		// reload model
		if (fTestModel.isDirty()) {

			try {
				fTestModel.reload();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// add new test files
			for (final String location : fTestModel.getTestFiles()) {
				if (!fTestFiles.containsKey(location))
					fTestFiles.put(location, new TestFile(this, location));
			}

			// remove obsolete test files
			fTestFiles.keySet().retainAll(fTestModel.getTestFiles());
		}

		super.reset();
	}

	Map<String, Object> getVariables() {
		return fSetupVariables;
	}

	public void setDebugOptions(final ILaunch launch) {
		fDebugLaunch = launch;
	}

	@Override
	public Object getFile() {
		return getModel().getFile();
	}
}
