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
import org.eclipse.ease.modules.unittest.components.TestComposite;
import org.eclipse.ease.modules.unittest.ui.dialogs.AddRemarkDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddMetadataHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {

			final Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof TestComposite) {
				final AddRemarkDialog dialog = new AddRemarkDialog(Display.getDefault().getActiveShell());
				if (dialog.open() == Window.OK) {
					((TestComposite) element).getCurrentTest().addMetaData(dialog.getTitle(), dialog.getDescription());
				}
			}
		}

		return null;
	}
}
