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
package org.eclipse.ease.modules.unittest.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.sourceprovider.TestSuiteSource;
import org.eclipse.ui.PlatformUI;

public class RunAllTests extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final TestSuiteSource instance = TestSuiteSource.getActiveInstance();
		if (instance != null) {
			final Object suite = instance.getCurrentState().get(TestSuiteSource.VARIABLE_TESTSUITE);
			if (suite instanceof TestSuite) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);

				((TestSuite) suite).run();
			}
		}

		return null;
	}
}
