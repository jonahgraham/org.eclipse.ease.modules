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
import java.util.Map;

import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.IScriptDebugFrame;

public class ScriptDebugFrame implements IScriptDebugFrame {

	private final Script fScript;
	private final int fLineNumber;
	private final int fType;

	public ScriptDebugFrame(Script script, int lineNumber, int type) {
		fScript = script;
		fLineNumber = lineNumber;
		fType = type;
	}

	public ScriptDebugFrame(IScriptDebugFrame frame) {
		this(frame.getScript(), frame.getLineNumber(), frame.getType());
	}

	@Override
	public int getLineNumber() {
		return fLineNumber;
	}

	@Override
	public Script getScript() {
		return fScript;
	}

	@Override
	public int getType() {
		return fType;
	}

	@Override
	public String getName() {
		return getScript().getTitle();
	}

	@Override
	public Map<String, Object> getVariables() {
		return Collections.emptyMap();
	}
}
