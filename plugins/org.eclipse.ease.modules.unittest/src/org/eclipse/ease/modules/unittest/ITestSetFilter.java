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
package org.eclipse.ease.modules.unittest;

import org.eclipse.ease.modules.unittest.components.TestFile;

public interface ITestSetFilter {
	public ITestSetFilter ALL = new ITestSetFilter() {
		@Override
		public boolean matches(final TestFile set) {
			return true;
		}
	};

	public boolean matches(TestFile set);

}
