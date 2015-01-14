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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.modules.unittest.ITestListener;

public abstract class TestComposite extends TestEntity implements ITestListener, IExecutionListener {

	public static final String CURRENT_TESTCOMPOSITE = "__internal_testObject";

	private static final String GLOBAL_TEST_SCOPE = "[outside test scope]";

	private IScriptEngine fEngine = null;

	private final List<Test> fTests = new LinkedList<Test>();

	private Test fCurrentTest = null;

	private Test fGlobalTestScope = null;

	public TestComposite(final TestComposite parent) {
		super(parent);
	}

	@Override
	public TestStatus getStatus() {
		TestStatus status = super.getStatus();

		// merge status of tests
		for (final Test test : getTests())
			status = status.merge(test.getStatus());

		// merge status of child elements
		for (final TestEntity child : getChildren())
			status = status.merge(child.getStatus());

		return status;
	}

	@Override
	public void notify(final Object testObject, final TestStatus status) {
		// propagate event to own listeners
		fireTestEvent(testObject, status);
	}

	public void addTest(final Test test) {
		// end current test
		endTest();

		synchronized (fTests) {
			if (!fTests.contains(test))
				// register new test
				fTests.add(test);
		}

		fCurrentTest = test;
		test.addTestListener(this);
		test.setStatus(TestStatus.RUNNING);
	}

	public void endTest() {
		if (fCurrentTest != null) {
			fCurrentTest.setStatus(TestStatus.PASS);
			fCurrentTest.removeTestListener(this);
		}

		fCurrentTest = fGlobalTestScope;
	}

	@Override
	public void reset() {
		synchronized (fTests) {
			fTests.clear();
		}
		fGlobalTestScope = null;
		fCurrentTest = null;

		super.reset();
	}

	public Test getCurrentTest() {
		if (fCurrentTest == null) {
			fGlobalTestScope = new Test(this, GLOBAL_TEST_SCOPE);

			synchronized (fTests) {
				fTests.add(0, fGlobalTestScope);
			}

			addTest(fGlobalTestScope);
		}

		return fCurrentTest;
	}

	public List<Test> getTests() {
		synchronized (fTests) {
			return new ArrayList<Test>(fTests);
		}
	}

	public void addTestResult(final TestStatus status, final String message) {
		addTestResult(status, message, getStackTrace());
	}

	public void addTestResult(final TestStatus status, final String message, final List<IScriptDebugFrame> trace) {
		getCurrentTest().addMessage(new TestResult(status, message, trace));
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		if (status == IExecutionListener.ENGINE_START)
			setStatus(TestStatus.RUNNING);

		else if (status == IExecutionListener.ENGINE_END) {
			setStatus(TestStatus.PASS);
		}
	}

	protected void setScriptEngine(final IScriptEngine scriptEngine) {
		fEngine = scriptEngine;
	}

	protected IScriptEngine getScriptEngine() {
		return fEngine;
	}

	private List<IScriptDebugFrame> getStackTrace() {
		if (fEngine instanceof IDebugEngine)
			return ((IDebugEngine) fEngine).getStackTrace();

		if (fEngine instanceof AbstractScriptEngine)
			return ((AbstractScriptEngine) fEngine).getStackTrace();

		return Collections.emptyList();
	}

	public abstract Collection<? extends TestEntity> getChildren();
}
