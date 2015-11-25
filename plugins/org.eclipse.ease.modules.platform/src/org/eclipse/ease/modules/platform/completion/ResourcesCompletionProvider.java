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
package org.eclipse.ease.modules.platform.completion;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.platform.ResourcesModule;
import org.eclipse.ease.modules.platform.ScriptingModule;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.completion.provider.AbstractFileLocationCompletionProvider;
import org.eclipse.ui.PlatformUI;

public class ResourcesCompletionProvider extends AbstractFileLocationCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		if (super.isActive(context)) {

			// Resources module
			if (context.getLoadedModules().contains(getModule(ResourcesModule.MODULE_ID))) {

				// simple methods
				if (context.getCaller().endsWith("copyFile") || context.getCaller().endsWith("createFile") || context.getCaller().endsWith("createFolder")
						|| context.getCaller().endsWith("deleteFile") || context.getCaller().endsWith("deleteFolder")
						|| context.getCaller().endsWith("fileExists"))
					return true;

				if ((context.getCaller().endsWith("findFiles")) && (context.getParameterOffset() == 1))
					return true;
				if ((context.getCaller().endsWith("getFile")) && (context.getParameterOffset() == 0))
					return true;
				if ((context.getCaller().endsWith("openFile")) && (context.getParameterOffset() == 0))
					return true;
				if ((context.getCaller().endsWith("readFile")) && (context.getParameterOffset() == 0))
					return true;
				if ((context.getCaller().endsWith("writeFile")) && (context.getParameterOffset() == 0))
					return true;

			}

			// Scripting module
			if (context.getLoadedModules().contains(getModule(ScriptingModule.MODULE_ID))) {
				if ((context.getCaller().endsWith("fork")) && (context.getParameterOffset() == 0))
					return true;
			}

			// UI module
			if (context.getLoadedModules().contains(getModule(UIModule.MODULE_ID))) {
				if ((context.getCaller().endsWith("showEditor")) || (context.getCaller().endsWith("openEditor")))
					return true;
			}
		}

		return false;
	}

	@Override
	protected boolean showCandidate(final ICompletionContext context, final Object candidate) {
		if ((context.getCaller().endsWith("showEditor")) || (context.getCaller().endsWith("openEditor")))
			return !isFileSystemResource(candidate);

		if ((context.getCaller().endsWith("createFile")) || context.getCaller().endsWith("createFolder") || context.getCaller().endsWith("deleteFolder")
				|| (context.getCaller().endsWith("findFiles")))
			return !isFile(candidate);

		return super.showCandidate(context, candidate);
	}

	private static ModuleDefinition getModule(final String identifier) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getAvailableModules().get(identifier);
	}
}