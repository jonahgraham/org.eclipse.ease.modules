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

import java.util.List;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.modules.AbstractEnvironment;
import org.eclipse.ease.modules.IEnvironment;

/**
 * A default implementation for {@link IAssertion} that adds support for error messages.
 */
public class DefaultAssertion implements IAssertion {

	/** Optional error message. */
	private final String fErrorMessage;
	private final boolean fValid;
	private final List<IScriptDebugFrame> fStackTrace;

	public DefaultAssertion(final List<IScriptDebugFrame> stackTrace, final boolean valid, final String errorDescription) {
		fStackTrace = stackTrace;
		fValid = valid;
		fErrorMessage = errorDescription;
	}

	public DefaultAssertion(final boolean valid, final String errorDescription) {
		this(null, valid, errorDescription);
	}

	/**
	 * Default constructor for invalid assertions.
	 *
	 * @param errorDescription
	 *            cause of error
	 */
	public DefaultAssertion(final String errorDescription) {
		this(null, false, errorDescription);
	}

	@Override
	public boolean isValid() {
		return fValid;
	}

	public List<IScriptDebugFrame> getStackTrace() {
		return fStackTrace;
	}

	@Override
	public String toString() {
		if (isValid())
			return IAssertion.VALID.toString();

		if (fErrorMessage != null)
			return fErrorMessage;

		return IAssertion.INVALID.toString();
	}

	@Override
	public void throwOnError() throws Exception {
		if (!isValid()) {
			// are we running within the unit test framework?
			final IScriptEngine scriptEngine = AbstractScriptEngine.getCurrentScriptEngine();
			if (scriptEngine != null) {
				final IEnvironment environment = AbstractEnvironment.getEnvironment(scriptEngine);
				if (environment != null) {
					final UnitTestModule module = environment.getModule(UnitTestModule.class);
					if (module != null) {
						if (module.getTestFile() != null)
							// running within unit test framework
							return;
					}
				}
			}

			throw new Exception(toString());
		}
	}
}
