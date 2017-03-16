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

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractEnvironment;
import org.eclipse.ease.modules.IEnvironment;

/**
 * Return value type that can be checked for validity.
 */
public interface IAssertion {
	/**
	 * Valid assertion.
	 */
	IAssertion VALID = new IAssertion() {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public String toString() {
			return "OK";
		}

		@Override
		public void throwOnError() throws Exception {
			// nothing to do
		}
	};

	/**
	 * Invalid assertion.
	 */
	IAssertion INVALID = new IAssertion() {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String toString() {
			return "Assertion failed";
		}

		@Override
		public void throwOnError() throws Exception {
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
	};

	/**
	 * Return <code>true</code> when assertion is valid.
	 *
	 * @return <code>true</code> on valid assertion
	 */
	boolean isValid();

	/**
	 * Throws an exception when the assertion is not valid and run outside of the unit test framework. When run within the unit test framework this method is
	 * not evaluated and will do nothing.
	 *
	 * @throws Exception
	 *             thrown when assertion is not valid
	 */
	void throwOnError() throws Exception;
}
