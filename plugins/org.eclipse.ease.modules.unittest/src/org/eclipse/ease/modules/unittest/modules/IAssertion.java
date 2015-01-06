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
	};

	/**
	 * Return <code>true</code> when assertion is valid.
	 *
	 * @return <code>true</code> on valid assertion
	 */
	boolean isValid();
}
