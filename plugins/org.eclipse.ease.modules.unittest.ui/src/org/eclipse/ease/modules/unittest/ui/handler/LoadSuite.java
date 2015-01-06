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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.modules.unittest.ui.dialogs.SuiteSelectionDialog;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class LoadSuite extends AbstractHandler implements IHandler {

	public static final String COMMAND_ID = "org.eclipse.ease.unittest.commands.loadTestSuite";
	public static final String PARAMETER_SUITE_NAME = COMMAND_ID + ".suite";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof UnitTestView) {
			// extract suite parameter for suite to load
			String fileName = event.getParameter(PARAMETER_SUITE_NAME);

			if (fileName == null) {
				// no parameter set, show selection dialog
				final SuiteSelectionDialog dialog = new SuiteSelectionDialog(HandlerUtil.getActiveShell(event));
				if (dialog.open() == Window.OK) {
					final IFile result = dialog.getSuiteFile();
					if (result != null)
						fileName = result.getFullPath().toString();
				}
			}

			if (fileName != null) {
				// try to load suite
				final IFile suiteFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
				if (suiteFile.exists()) {
					try {
						((UnitTestView) part).loadSuite(new TestSuite(new TestSuiteModel(suiteFile)));
					} catch (final Exception e) {
						// TODO implement
						throw new RuntimeException(e);
					}
				}
			}
		}

		return null;
	}
}
