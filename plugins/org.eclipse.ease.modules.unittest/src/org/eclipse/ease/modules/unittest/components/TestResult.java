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
import java.util.List;

import org.eclipse.ease.debugging.IScriptDebugFrame;

/**
 * Test problem.
 */
public class TestResult {

	/** Message type. */
	private final TestStatus fType;

	/** Message description. */
	private final String fDescription;

	/** Message stack trace. */
	private final List<IScriptDebugFrame> fStackTrace;

	/**
	 * Constructor.
	 *
	 * @param type
	 *            problem type
	 * @param description
	 *            problem description
	 * @param fileTrace
	 *            file trace for problem
	 */
	public TestResult(final TestStatus type, final String description, final List<IScriptDebugFrame> stackTrace) {
		fType = type;
		fDescription = description;

		// we need to create a carbon copy of the stack as it gets modified when the script continues
		fStackTrace = new ArrayList<IScriptDebugFrame>();
		for (final IScriptDebugFrame frame : stackTrace)
			fStackTrace.add(new ScriptDebugFrame(frame));
	}

	public TestStatus getStatus() {
		return fType;
	}

	public String getDescription() {
		return fDescription;
	}

	public List<IScriptDebugFrame> getStackTrace() {
		return fStackTrace;
	}
}
