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
package org.eclipse.ease.modules.platform.dnd;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractEnvironment;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.platform.ScriptingModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.dnd.IShellDropHandler;
import org.eclipse.ui.PlatformUI;

public class ForkDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(IScriptEngine scriptEngine, Object element) {
		if ((element instanceof IFile) || (element instanceof File) || (element instanceof URI))
			return getScriptType(element) != null;

		return false;
	}

	@Override
	public void performDrop(IScriptEngine scriptEngine, Object element) {
		final ScriptType scriptType = getScriptType(element);

		if (scriptType != null) {
			// resource not supported by current engine, spawn new one
			final IEnvironment environment = AbstractEnvironment.getEnvironment(scriptEngine);
			if (environment != null) {
				if (environment.getModule(ScriptingModule.class) == null) {
					if (environment.loadModule(ScriptingModule.MODULE_ID) == null)
						throw new RuntimeException("Cannot load module '" + ScriptingModule.MODULE_ID + "'");
				}

				scriptEngine.executeAsync("fork(\"" + ResourceTools.toAbsoluteLocation(element, null) + "\")");

			} else
				throw new RuntimeException("No environment loaded, cannot execute dropped file");

		} else {
			// should not happen as accepts guarantees that a scripttype exists for the given resource
			throw new RuntimeException("Could not detect script type of dropped resource");
		}
	}

	private static ScriptType getScriptType(Object element) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getScriptType(ResourceTools.toAbsoluteLocation(element, null));
	}

}
