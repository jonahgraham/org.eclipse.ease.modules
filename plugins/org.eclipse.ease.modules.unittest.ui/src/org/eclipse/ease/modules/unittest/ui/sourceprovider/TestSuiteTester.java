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
package org.eclipse.ease.modules.unittest.ui.sourceprovider;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ease.modules.unittest.components.TestSuite;

public class TestSuiteTester extends PropertyTester {

	private static String PROPERTY_STATUS = "status";
	private static String PROPERTY_EXISTS = "exists";

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof TestSuite) {
			if (PROPERTY_STATUS.equals(property)) {
				if (expectedValue != null)
					return ((TestSuite) receiver).getStatus().toString().equalsIgnoreCase(expectedValue.toString());

			} else if (PROPERTY_EXISTS.equals(property))
				return true;
		}

		return false;
	}
}
