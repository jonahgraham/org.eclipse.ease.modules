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

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.modules.unittest.ITestListener;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

public class RunTestSuite extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorInput input = HandlerUtil.getActiveEditor(event).getEditorInput();

		if (input instanceof FileEditorInput) {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true)) {

				try {
					// create testsuite
					final TestSuite suiteToRun = new TestSuite(((FileEditorInput) input).getFile());

					// TODO activate only if visualization is set to "view"
					// activate view
					final IViewPart view = UIModule.showView(UnitTestView.VIEW_ID);
					if (view instanceof ITestListener)
						suiteToRun.addTestListener((ITestListener) view);

					suiteToRun.run();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
