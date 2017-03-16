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
import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.ITestSetFilter;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.sourceprovider.TestSuiteSource;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunSelectedTests extends RunAllTests implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			// save dirty editors
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true)) {

				// collect test files to run
				final Collection<TestFile> testFiles = new HashSet<>();
				for (final Object element : ((IStructuredSelection) selection).toArray()) {
					if (element instanceof TestFile)
						testFiles.add((TestFile) element);

					else if (element instanceof TestSuite)
						testFiles.addAll(((TestSuite) element).getChildren());

					else if (element instanceof IPath) {
						// we need to contact the context provider and extract all child nodes
						final IWorkbenchPart part = HandlerUtil.getActivePart(event);
						if (part instanceof UnitTestView) {
							final IContentProvider contentProvider = ((UnitTestView) part).getFileTreeViewer().getContentProvider();

							final LinkedList<Object> parentElements = new LinkedList<>();
							parentElements.add(element);

							// iterate over tree nodes looking for leaves
							while (!parentElements.isEmpty()) {
								final Object parent = parentElements.removeFirst();
								for (final Object child : ((ITreeContentProvider) contentProvider).getChildren(parent)) {
									if (child instanceof TestFile)
										testFiles.add((TestFile) child);
									else
										parentElements.add(child);
								}
							}
						}
					}
				}

				// run test sets
				final TestSuiteSource instance = TestSuiteSource.getActiveInstance();
				if (instance != null) {
					final Object suite = instance.getCurrentState().get(TestSuiteSource.VARIABLE_TESTSUITE);
					if (suite instanceof TestSuite) {

						updateSources((TestSuite) suite);

						((TestSuite) suite).run(new ITestSetFilter() {

							@Override
							public boolean matches(final TestFile set) {
								return testFiles.contains(set);
							}
						});
					}
				}
			}
		}

		return null;
	}
}
