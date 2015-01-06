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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.modules.unittest.ITestSetFilter;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.sourceprovider.TestSuiteSource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunSelectedTests extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			// save dirty editors
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);

			// collect test sets to run
			final Collection<TestFile> testFiles = new HashSet<TestFile>();
			for (final Object element : ((IStructuredSelection) selection).toArray()) {
				if (element instanceof TestFile)
					testFiles.add((TestFile) element);

				else if (element instanceof TestSuite)
					testFiles.addAll(((TestSuite) element).getChildren());

				// TODO deal with path elements and extract children
			}

			// run test sets
			final TestSuiteSource instance = TestSuiteSource.getActiveInstance();
			if (instance != null) {
				final Object suite = instance.getCurrentState().get(TestSuiteSource.VARIABLE_TESTSUITE);
				if (suite instanceof TestSuite) {
					((TestSuite) suite).run(new ITestSetFilter() {

						@Override
						public boolean matches(final TestFile set) {
							return testFiles.contains(set);
						}
					});
				}
			}
		}

		return null;
	}
}
