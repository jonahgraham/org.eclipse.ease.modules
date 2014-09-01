/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Module used to interact with the workbench
 *
 * @author adaussy
 *
 */
public class WorkbenchModule extends AbstractScriptModule {

	public WorkbenchModule() {
	}

	/**
	 * Return the active workbench
	 *
	 * @return
	 */
	@WrapToScript
	public static IWorkbench getActiveWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * Return the Active Window {@link IWorkbench#getActiveWorkbenchWindow()}
	 *
	 * @return
	 */
	@WrapToScript
	public static IWorkbenchWindow getActiveWindow() {
		RunnableWithResult<IWorkbenchWindow> runnable = new RunnableWithResult<IWorkbenchWindow>() {

			@Override
			public void run() {
				setResult(getActiveWorkbench().getActiveWorkbenchWindow());
			}
		};
		Display.getDefault().syncExec(runnable);
		return runnable.getResult();
	}

	/**
	 * Return the active shell
	 *
	 * @return The active shell
	 */
	@WrapToScript
	public static Shell getActiveShell() {
		return getActiveWindow().getShell();
	}

	/**
	 * Return the active page {@link IWorkbenchWindow#getActivePage()}
	 *
	 * @return
	 */
	@WrapToScript
	public static IWorkbenchPage getActivePage() {
		return getActiveWindow().getActivePage();
	}

	/**
	 * Return the current editor
	 *
	 * @return The current editor
	 */
	@WrapToScript
	public static IEditorPart getActiveEditor() {
		return getActiveWindow().getActivePage().getActiveEditor();
	}
}
