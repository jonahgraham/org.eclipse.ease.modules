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
package org.eclipse.ease.modules.unittest.reporters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.ease.modules.unittest.components.Test;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestResult;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Creates reports of unit test results.
 */
public final class JUnitReportGenerator implements IReportGenerator {

	@Override
	public String createReport(final String title, final String description, final TestSuite testSuite) {
		final XMLMemento root = XMLMemento.createWriteRoot("root");

		final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

		final IMemento suiteNode = root.createChild("testsuite");
		suiteNode.putString("name", title);
		suiteNode.putString("timestamp", timeFormat.format(testSuite.getStartTime()));
		suiteNode.putString("hostname", "localhost");
		suiteNode.putInteger("tests", getTestCount(testSuite));
		suiteNode.putInteger("failures", getErrors(testSuite));
		suiteNode.putInteger("errors", getFailures(testSuite));
		suiteNode.putInteger("time", (int) (testSuite.getExecutionTime() / 1000));

		final IMemento propertiesNode = suiteNode.createChild("properties");
		for (final Variable variable : testSuite.getModel().getVariables()) {
			final IMemento propertyNode = propertiesNode.createChild("property");
			propertyNode.putString("name", escape(variable.getName()));
			propertyNode.putTextData(escape(variable.getContent()));
		}

		for (final TestFile testFile : testSuite.getChildren()) {
			for (final Test test : testFile.getTests()) {
				final IMemento testcaseNode = suiteNode.createChild("testcase");
				testcaseNode.putString("name", escape(test.getTitle()));
				testcaseNode.putString("classname", escape(ResourceTools.toProjectRelativeLocation(testFile.getFile(), null)));
				testcaseNode.putInteger("time", (int) (test.getExecutionTime() / 1000));
				for (final TestResult message : test.getMessages()) {
					if (message.getStatus() == TestStatus.FAILURE) {
						final IMemento errorNode = testcaseNode.createChild("error");
						errorNode.putString("message", escape(message.getDescription()));
						errorNode.putString("type", "script aborted");
					} else if (message.getStatus() == TestStatus.ERROR) {
						final IMemento failureNode = testcaseNode.createChild("failure");
						failureNode.putString("message", escape(message.getDescription()));
						failureNode.putString("type", "verification mismatch");
					}
				}
			}
		}

		return root.toString();
	}

	private static String escape(final String variable) {
		return variable.replace("<", "&lt;").replace(">", "&gt;");
	}

	private static int getTestCount(final TestSuite testSuite) {
		int tests = 0;

		for (final TestFile testFile : testSuite.getChildren()) {
			for (final Test test : testFile.getTests()) {
				if (!test.isTransient())
					tests++;
			}
		}

		return tests;
	}

	private static int getErrors(final TestSuite testSuite) {
		int errors = 0;

		for (final TestFile file : testSuite.getChildren()) {
			for (final Test test : file.getTests()) {
				if (test.getMessages(TestStatus.ERROR).size() > 0)
					errors++;
			}
		}

		return errors;
	}

	private static int getFailures(final TestSuite testSuite) {
		int errors = 0;

		for (final TestFile file : testSuite.getChildren()) {
			for (final Test test : file.getTests()) {
				if (test.getMessages(TestStatus.FAILURE).size() > 0)
					errors++;
			}
		}

		return errors;
	}

	@Override
	public String getDefaultExtension() {
		return "xml";
	}
}
