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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ease.modules.unittest.ITestListener;

public class TestEntity {

	private TestStatus fStatus = TestStatus.NOT_RUN;

	private final ListenerList fTestListeners = new ListenerList();

	private final Map<String, String> fMetaData = new HashMap<String, String>();

	private long fStartTime = 0;

	private long fEndTime = 0;

	private final TestComposite fParent;

	public TestEntity(final TestComposite parent) {
		fParent = parent;
	}

	public void setStatus(final TestStatus status) {
		if (status != fStatus) {
			// only react on status change

			if (status == TestStatus.RUNNING) {
				fStartTime = System.currentTimeMillis();
				fEndTime = 0;
			}

			else if (fStatus == TestStatus.RUNNING)
				fEndTime = System.currentTimeMillis();

			fStatus = status;
			fireTestEvent(this, getStatus());
		}
	}

	public TestStatus getStatus() {
		return fStatus;
	}

	public void reset() {
		fStartTime = 0;
		fEndTime = 0;
		setStatus(TestStatus.NOT_RUN);
	}

	public void addTestListener(final ITestListener listener) {
		fTestListeners.add(listener);
	}

	public void removeTestListener(final ITestListener listener) {
		fTestListeners.remove(listener);
	}

	protected void fireTestEvent(final Object testObject, final TestStatus status) {
		for (final Object listener : fTestListeners.getListeners())
			((ITestListener) listener).notify(testObject, status);
	}

	/**
	 * Get total execution time in [ms]
	 *
	 * @return total execution time in [ms]
	 */
	public long getExecutionTime() {
		if (fEndTime != 0)
			// test execution finished
			return fEndTime - fStartTime;

		if (fStartTime != 0)
			// test still running
			return System.currentTimeMillis() - fStartTime;

		// not started yet
		return 0;
	}

	public long getStartTime() {
		return fStartTime;
	}

	public void addMetaData(final String identifier, final String content) {
		fMetaData.put(identifier, content);
	}

	public Map<String, String> getMetaData() {
		return Collections.unmodifiableMap(fMetaData);
	}

	public TestComposite getParent() {
		return fParent;
	}
}
