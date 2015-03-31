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
package org.eclipse.ease.modules.unittest.modules;

import java.io.IOException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IScriptFunctionModifier;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.platform.IFileHandle;
import org.eclipse.ease.modules.platform.ResourcesModule;
import org.eclipse.ease.modules.unittest.components.Test;
import org.eclipse.ease.modules.unittest.components.TestComposite;
import org.eclipse.ease.modules.unittest.components.TestEntity;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.modules.unittest.reporters.IReportGenerator;
import org.eclipse.ease.modules.unittest.reporters.ReportTools;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Library providing UnitTest functionality. Unit tests are parts of a JavaScript file which are embedded between a startTest() and an endTest() function call.
 * Every function result returned inbetween will be checked for a response of type {@link IAssertion}. If such a response is detected its result will
 * automatically be applied to the current unit test.
 */
public class UnitTestModule extends AbstractScriptModule implements IScriptFunctionModifier {

	public static final String MODULE_NAME = "Unittest";
	private static final String ASSERTION_FUNCION_NAME = "assertion";
	public static final String INJECTED_MAIN = "injected_code_";
	public static final String FAIL_ON_ERROR_VARIABLE = "__FAIL_ON_ERROR";

	private boolean fAssertionEnablement = true;
	private int fAssertionsToBeIgnored = 0;

	/**
	 * Run a unit test. If the file addressed is a suite, then the whole suite is started. If the file is a simple JavaScript file, then a dynamic suite is
	 * created that contains just the one file.
	 *
	 * @param filename
	 *            location of testsuite or testfile. Must be a file from the workspace
	 * @return {@link TestSuite} definition or <code>null</code>
	 */
	@WrapToScript
	public TestSuite runUnitTest(Object executable) {
		if (executable instanceof TestSuite) {
			((TestSuite) executable).run();
			return (TestSuite) executable;
		}

		if (!(executable instanceof IFile))
			executable = ResourceTools.resolveFile(executable.toString(), getScriptEngine().getExecutedFile(), true);

		if ((executable instanceof IFile) && (((IFile) executable).exists())) {
			try {
				if ("suite".equalsIgnoreCase(((IFile) executable).getFileExtension())) {
					// we have a test suite
					final TestSuiteModel description = new TestSuiteModel((IFile) executable);
					final TestSuite testSuite = new TestSuite(description);
					testSuite.run();
					return testSuite;

				} else if ("js".equalsIgnoreCase(((IFile) executable).getFileExtension())) {
					// we have a JavaScript file
					final TestSuiteModel description = new TestSuiteModel();
					// FIXME re-implement
					// description.addTestFile((IFile) executable);

					final TestSuite testSuite = new TestSuite(description);
					testSuite.run();
					return testSuite;
				}

			} catch (final Exception e) {
				getScriptEngine().getErrorStream().println("Error executing unit test suite: " + e.getLocalizedMessage());
			}
		}

		return null;
	}

	/**
	 * Open a testsuite without executing it.
	 *
	 * @param filename
	 *            location of testsuite file. Must be a file from the workspace
	 * @return {@link TestSuite} definition or <code>null</code>
	 * @throws IOException
	 * @throws CoreException
	 */
	@WrapToScript
	public TestSuite openTestSuite(final String filename) throws IOException, CoreException {
		final Object file = ResourceTools.resolveFile(filename, getScriptEngine().getExecutedFile(), true);
		if ((file instanceof IFile) && (((IFile) file).exists())) {
			if ("suite".equalsIgnoreCase(((IFile) file).getFileExtension())) {
				// we have a test suite
				return new TestSuite(new TestSuiteModel((IFile) file));
			}
		}

		return null;
	}

	/**
	 * Create a test report file.
	 *
	 * @param reportType
	 *            type of report; see getReportTypes() for values
	 * @param suite
	 *            {@link TestSuite} to be reported
	 * @param fileLocation
	 *            location where report should be stored
	 * @param title
	 *            report title
	 * @param description
	 *            report description (ignored by some reports)
	 * @return <code>true</code> when report was created successfully
	 * @throws Exception
	 *             thrown on file write errors
	 */
	@WrapToScript
	public boolean createReport(final String reportType, final TestSuite suite, final String fileLocation, final String title, final String description)
			throws Exception {

		final Object file = ResourceTools.resolveFile(fileLocation, getScriptEngine().getExecutedFile(), false);

		final IReportGenerator report = ReportTools.getReport(reportType);
		if (report != null) {
			final String reportData = report.createReport(title, description, suite);
			IFileHandle handle = getEnvironment().getModule(ResourcesModule.class).writeFile(file, reportData, ResourcesModule.WRITE);
			ResourcesModule.closeFile(handle);
			return true;
		}

		return false;
	}

	/**
	 * Get a list of available report types.
	 *
	 * @return String array containing available report types
	 */
	@WrapToScript
	public static String[] getReportTypes() {
		return ReportTools.getReportTemplates().toArray(new String[0]);
	}

	@Override
	public String getPreExecutionCode(final Method method) {
		return "";
	}

	@Override
	public String getPostExecutionCode(final Method method) {
		if (returnsAssertion(method))
			return "\t" + ASSERTION_FUNCION_NAME + "(" + IScriptFunctionModifier.RESULT_NAME + ");\n\n";

		return "";
	}

	/**
	 * Check whether a method returns an assertion.
	 *
	 * @param method
	 *            method to verify
	 * @return <code>true</code> when method returns an assertion
	 */
	private static boolean returnsAssertion(final Method method) {
		final Class<?> returnType = method.getReturnType();

		// check for return type "IAssertion"
		return (IAssertion.class.isAssignableFrom(returnType));
	}

	/**
	 * Start a specific unit test. Started tests should be terminated by an {@link #endTest()}.
	 *
	 * @param title
	 *            name of test
	 * @param description
	 *            short test description
	 */
	@WrapToScript
	public final void startTest(final String title, @ScriptParameter(defaultValue = "") final String description) {
		fAssertionsToBeIgnored = 0;
		fAssertionEnablement = true;

		executeUserCode(TestSuiteModel.CODE_LOCATION_TEST_SETUP);

		final TestComposite testObject = getTestObject();
		if (testObject != null) {
			final Test test = new Test(testObject, title, description);
			final IScriptEngine engine = getScriptEngine();
			if (engine instanceof IDebugEngine)
				test.setTestLocation(((IDebugEngine) engine).getStackTrace());
			else if (engine instanceof AbstractScriptEngine)
				test.setTestLocation(((AbstractScriptEngine) engine).getStackTrace());

			testObject.addTest(test);
		}
	}

	/**
	 * End the current test.
	 */
	@WrapToScript
	public final void endTest() {
		final TestComposite testObject = getTestObject();
		if (testObject != null)
			testObject.endTest();

		executeUserCode(TestSuiteModel.CODE_LOCATION_TEST_TEARDOWN);
	}

	/**
	 * Create a new assertion for the current test. According to the assertion status an error might be added to the current testcase.
	 *
	 * @param reason
	 *            assertion to be checked
	 */
	@WrapToScript
	public final void assertion(final IAssertion reason) {
		if (fAssertionEnablement) {
			if (fAssertionsToBeIgnored == 0) {
				if (!reason.isValid()) {
					// TODO check if we need this any longer as mergedassertion will return all errors
					if (reason instanceof MergedAssertion) {
						// we might have multiple errors here, create an error marker for each of them
						for (final IAssertion mergedReason : ((MergedAssertion) reason).getAssertions()) {
							if (!mergedReason.isValid())
								error(mergedReason.toString());
						}

					}

					error(reason.toString());
				}

			} else
				fAssertionsToBeIgnored--;
		}
	}

	/**
	 * Add a new error to the current testcase.
	 *
	 * @param message
	 *            error message
	 */
	@WrapToScript
	public final void error(final String message) {
		final TestComposite testObject = getTestObject();
		if (testObject != null) {
			final Object failOnError = getScriptEngine().getVariable(FAIL_ON_ERROR_VARIABLE);
			if ((failOnError instanceof Boolean) && (((Boolean) failOnError).booleanValue())) {
				testObject.addTestResult(TestStatus.FAILURE, message);

				throw new RuntimeException("Unit Failure: " + message);

			} else
				testObject.addTestResult(TestStatus.ERROR, message);
		}
	}

	/**
	 * Report a new failure for the current UnitTest.
	 *
	 * @param message
	 *            failure message
	 */
	@WrapToScript
	public final void failure(final String message) {
		final TestComposite testObject = getTestObject();
		if (testObject != null)
			testObject.addTestResult(TestStatus.FAILURE, message);

		throw new RuntimeException("Unit Failure: " + message);
	}

	/**
	 * Get the current {@link TestFile} instance.
	 *
	 * @return test file instance
	 */
	@WrapToScript
	public TestFile getTestFile() {
		final TestComposite testObject = getTestObject();
		if (testObject instanceof TestFile)
			return (TestFile) testObject;

		return null;
	}

	/**
	 * Get a {@link Test} instance. if no <i>name</i> is provided, the current test instance is returned. When provided, this method searches for a test with
	 * the given title.
	 *
	 * @param name
	 *            test name to look for
	 * @return current test or <code>null</code>
	 */
	@WrapToScript
	public Test getTest(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String name) {
		if (name == null)
			return getTestFile().getCurrentTest();

		for (TestEntity test : getTestFile().getChildren()) {
			if ((test instanceof Test) && (name.equals(((Test) test).getTitle())))
				return (Test) test;
		}

		return null;
	}

	private TestComposite getTestObject() {
		return (TestComposite) getScriptEngine().getVariable(TestComposite.CURRENT_TESTCOMPOSITE);
	}

	/**
	 * Get the current {@link TestSuite} instance.
	 *
	 * @return test suite instance
	 */
	@WrapToScript
	public TestSuite getTestSuite() {
		final TestComposite testObject = getTestObject();
		if (testObject instanceof TestSuite)
			return (TestSuite) testObject;

		if (testObject instanceof TestFile)
			return ((TestFile) testObject).getTestSuite();

		return null;
	}

	/**
	 * Insert additional test code at a given code location. Some code locations are predefined like start test file or start test case. Others may be freely
	 * defined by the unit test execution target. Those other tests need to be invoked by the unit tests by calling <code>executeUserCode(identifier);</code>.
	 *
	 * @param identifier
	 *            code identifier
	 */
	@WrapToScript
	public final void executeUserCode(final String identifier) {
		if (getTestObject() != null) {
			final String code = getTestSuite().getModel().getCodeFragment(identifier);
			if ((code != null) && (!code.isEmpty()))
				getScriptEngine().inject(code);
		}
	}

	/**
	 * Expect two objects to be equal.
	 *
	 * @param expected
	 *            expected result
	 * @param actual
	 *            actual result
	 * @param errorDescription
	 *            optional error text to be displayed when not equal
	 * @return assertion containing comparison result
	 */
	@WrapToScript
	public static IAssertion assertEquals(final Object expected, final Object actual,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		if (expected != null)
			return new DefaultAssertion(expected.equals(actual), (errorDescription == null) ? "Objects do not match: expected<" + expected + ">, actual <"
					+ actual + ">" : errorDescription.toString());

		return assertNull(actual, errorDescription);
	}

	/**
	 * Expect two objects not to be equal.
	 *
	 * @param expected
	 *            unexpected result
	 * @param actual
	 *            actual result
	 * @param errorDescription
	 *            optional error text to be displayed when equal
	 * @return assertion containing comparison result
	 */
	@WrapToScript
	public static IAssertion assertNotEquals(final Object expected, final Object actual,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		if (expected != null)
			return new DefaultAssertion(!expected.equals(actual), (errorDescription == null) ? "Objects match" : errorDescription.toString());

		return assertNotNull(actual, errorDescription);
	}

	/**
	 * Asserts when provided value is not <code>null</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertNull(final Object actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual == null, (errorDescription == null) ? "Object is not null, actual <" + actual + ">" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>null</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertNotNull(final Object actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual != null, (errorDescription == null) ? "Object is null" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>false</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertTrue(final boolean actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(actual, (errorDescription == null) ? "Value is false" : errorDescription.toString());
	}

	/**
	 * Asserts when provided value is <code>true</code>.
	 *
	 * @param actual
	 *            value to verify
	 * @param errorDescription
	 *            optional error description
	 * @return assertion depending on <code>actual</code> value
	 */
	@WrapToScript
	public static IAssertion assertFalse(final boolean actual, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object errorDescription) {
		return new DefaultAssertion(!actual, (errorDescription == null) ? "Value is true" : errorDescription.toString());
	}

	/**
	 * Disables automatic assertions in tests. When set user generated errors, warnings, failures will still be reported, but assert functions will not report
	 * errors. Assertions are automatically enabled on a {@link #startTest(String, String)} command.
	 */
	@WrapToScript
	public void disableAssertions() {
		fAssertionEnablement = false;
	}

	/**
	 * Enables automatic assertions in tests. This is the default setting for any testfile.
	 */
	@WrapToScript
	public void enableAssertions() {
		fAssertionEnablement = true;
		fAssertionsToBeIgnored = 0;
	}

	/**
	 * Define amount of upcoming assertions to be ignored. Allows to ignore <code>count</code> upcoming assertions (not depending on actual assertion status).
	 * Assertions are enabled again once the counter reaches 0,when {@link #enableAssertions()} is called or a new test is started using
	 * {@link #startTest(String, String)}.
	 *
	 * @param count
	 *            assertions to be ignored
	 */
	@WrapToScript
	public void ignoreAssertions(final int count) {
		assert (count >= 0);
		fAssertionsToBeIgnored = count;
	}

	/**
	 * Wait until a test entity is completed. If the entity is not scheduled this method might stall forever!
	 *
	 * @param testObject
	 *            {@link TestSuite} or {@link Test} to wait for
	 * @return <code>true</code> on success, <code>false</code> if test was interrupted
	 */
	@WrapToScript
	public static boolean waitForCompletion(final TestComposite testObject) {
		try {
			while ((testObject.getStatus() == TestStatus.NOT_RUN) || (testObject.getStatus() == TestStatus.RUNNING))
				Thread.sleep(1000);
		} catch (final InterruptedException e) {
			return false;
		}

		return true;
	}

	/**
	 * Add metadata to the current test object. Metadata is stored as a Map, so setting with an already existing keyword will override the previous setting.
	 *
	 * @param key
	 *            metadata keyword
	 * @param data
	 *            metadata
	 */
	@WrapToScript
	public void addMetaData(final String key, final String data) {
		final TestComposite testObject = getTestObject();
		testObject.getCurrentTest().addMetaData(key, data);
	}
}
