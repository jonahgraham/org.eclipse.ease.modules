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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.modules.unittest.Bundle;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class Test extends TestEntity {

	private final String fTitle;
	private String fDescription;
	private List<IScriptDebugFrame> fTestLocation = null;

	private final List<TestResult> fResults = new ArrayList<TestResult>();

	public Test(final TestComposite parent, final String title, final String description) {
		super(parent);

		fTitle = title;
		setDescription(description);
	}

	public Test(final TestComposite parent, final String title) {
		this(parent, title, null);
	}

	public String getTitle() {
		return fTitle;
	}

	@Override
	public TestStatus getStatus() {
		TestStatus status = super.getStatus();

		// merge status of own messages
		for (final TestResult message : getMessages())
			status = status.merge(message.getStatus());

		return status;
	}

	@Override
	public void reset() {
		super.reset();
		fResults.clear();
	}

	public void setTestLocation(final List<IScriptDebugFrame> stackTrace) {
		// we need to create a carbon copy of the stack as it gets modified when the script continues
		fTestLocation = new ArrayList<IScriptDebugFrame>();
		for (final IScriptDebugFrame frame : stackTrace)
			fTestLocation.add(new ScriptDebugFrame(frame));
	}

	public List<IScriptDebugFrame> getTestLocation() {
		return fTestLocation;
	}

	public String getDescription() {
		return fDescription;
	}

	/**
	 * Sets the test description. A previous description gets replaced by the new one.
	 *
	 * @param description
	 *            description to use
	 */
	public void setDescription(final String description) {
		fDescription = description;
	}

	public void addMessage(final TestResult message) {
		getMessages().add(message);
		createMarker(message);
	}

	public List<TestResult> getMessages() {
		return fResults;
	}

	public TestResult getSeverestMessage() {
		TestResult result = null;
		for (final TestResult message : getMessages()) {
			if ((result == null) || (result.getStatus().compareTo(message.getStatus()) < 0))
				result = message;
		}

		return result;
	}

	/**
	 * Returns all messages with a given {@link TestStatus}. Only messages with the exact TestStatus will be returned.
	 *
	 * @param status
	 *            TestStatus to query for
	 * @return messages with given <i>status</i>
	 */
	public Collection<TestResult> getMessages(final TestStatus status) {
		final ArrayList<TestResult> result = new ArrayList<TestResult>();

		for (final TestResult message : getMessages())
			if (message.getStatus().equals(status))
				result.add(message);

		return result;
	}

	private static void createMarker(final TestResult result) {
		// add error marker
		if (result.getStatus().isEqualOrWorse(TestStatus.ERROR)) {
			final List<IScriptDebugFrame> trace = result.getStackTrace();
			if (trace != null) {

				for (final IScriptDebugFrame element : trace) {
					final Object file = element.getScript().getFile();
					if ((file instanceof IFile) && (((IFile) file).exists())) {
						try {
							final HashMap<String, Object> attributes = new HashMap<String, Object>();
							attributes.put(IMarker.LINE_NUMBER, element.getLineNumber());
							attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
							attributes.put(IMarker.MESSAGE, result.getDescription());
							MarkerUtilities.createMarker((IFile) file, attributes, Bundle.PLUGIN_ID + ".scriptassertion");
						} catch (final CoreException e) {
							// TODO error handling
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public boolean isTransient() {
		// TODO Auto-generated method stub
		return false;
	}
}
