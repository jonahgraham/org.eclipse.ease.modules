/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ease.modules.modeling.ui.ScriptJob;
import org.eclipse.ease.modules.modeling.ui.views.ModelRefactoringView;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.scripts.ui.ScriptSelectionDialog;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class EaseScriptHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		for (IViewReference ref : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()) {
			if (ref.getId().equals(ModelRefactoringView.ID)) {
				ScriptSelectionDialog dialogBox = new ScriptSelectionDialog(
						HandlerUtil.getActiveShell(event), ref.getPart(false)
								.getSite());
				if (dialogBox.open() == ScriptSelectionDialog.OK) {
					IScript result = dialogBox.getMacro();
					ScriptJob job = new ScriptJob(result);
					job.schedule();

				}
			}
		}
		return null;
	}

}
