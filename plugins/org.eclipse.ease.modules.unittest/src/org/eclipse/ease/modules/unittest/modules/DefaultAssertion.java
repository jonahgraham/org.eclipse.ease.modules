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
 * A default implementation for {@link IAssertion} that adds support for error messages.
 */
public class DefaultAssertion implements IAssertion {

	/** Optional error message. */
	private final String fDescription;
	private final boolean fValid;

	protected DefaultAssertion() {
		fValid = true;
		fDescription = "";
	}

	/**
	 * Default constructor for invalid assertions.
	 *
	 * @param errorDescription
	 *            cause of error
	 */
	public DefaultAssertion(final String errorDescription) {
		this(false, errorDescription);
	}

	public DefaultAssertion(final boolean valid, final String errorDescription) {
		fValid = valid;
		fDescription = errorDescription;
	}

	@Override
	public boolean isValid() {
		return fValid;
	}

	@Override
	public String toString() {
		return (fDescription != null) ? fDescription : ((isValid()) ? IAssertion.VALID.toString() : IAssertion.INVALID.toString());
	}
}
