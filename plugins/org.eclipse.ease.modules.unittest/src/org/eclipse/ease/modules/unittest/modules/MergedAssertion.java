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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.ease.modules.unittest.Bundle;

/**
 * An assertion that merges the results of other assertions.
 */
public class MergedAssertion extends DefaultAssertion {

	/** Child assertions. */
	private final Collection<IAssertion> fAssertions = new ArrayList<>();

	/** Merged valid message. */
	private final String fValidMessage;

	/**
	 * Constructor. Accepts messages for the valid and for the error case.
	 *
	 * @param validMessage
	 *            message for result = valid
	 * @param errorMessage
	 *            message for error case
	 */
	public MergedAssertion(final String validMessage, final String errorMessage) {
		super(errorMessage);
		fValidMessage = validMessage;
	}

	/**
	 * Constructor. Uses default message for valid case and the first error message from all stored assertion in an error case.
	 */
	public MergedAssertion() {
		this(IAssertion.VALID.toString(), IAssertion.INVALID.toString());
	}

	/**
	 * Constructor. Accepts a message for the error case.
	 *
	 * @param errorMessage
	 *            message for error case
	 */
	public MergedAssertion(final String errorMessage) {
		this(IAssertion.VALID.toString(), errorMessage);
	}

	/**
	 * Add an assertion. Accumulates assertion result.
	 *
	 * @param assertion
	 *            assertion to add
	 */
	public final void add(final IAssertion assertion) {
		fAssertions.add(assertion);
	}

	@Override
	public final boolean isValid() {
		for (final IAssertion assertion : fAssertions)
			if (!assertion.isValid())
				return false;

		return true;
	}

	@Override
	public final String toString() {
		if (isValid())
			return fValidMessage;

		// take user defied error message if set by user
		if (!IAssertion.INVALID.toString().equals(super.toString()))
			return super.toString();

		final StringBuilder message = new StringBuilder();
		for (final IAssertion assertion : fAssertions) {
			if (!assertion.isValid()) {
				if (message.length() > 0)
					message.append(Bundle.LINE_DELIMITER);

				message.append(assertion.toString());
			}
		}

		if (message.length() > 0)
			return message.toString();

		// never to be reached, but just in case, drop the default error message
		return IAssertion.INVALID.toString();
	}

	public Collection<IAssertion> getAssertions() {
		return Collections.unmodifiableCollection(fAssertions);
	}
}
