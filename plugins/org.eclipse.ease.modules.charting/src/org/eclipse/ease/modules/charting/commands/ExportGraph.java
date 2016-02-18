/*******************************************************************************
 * Copyright (c) 2015 Domjan Sansovic and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Domjan Sansovic - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.charting.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.charting.PluginConstants;
import org.eclipse.ease.modules.charting.views.ChartView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportGraph extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof ChartView) {
			try {
				((ChartView) activePart).getChart().export(null, false);
			} catch (Throwable e) {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "Export Graph", "Could not export Graph to PNG file");
				Logger.error(PluginConstants.PLUGIN_ID, "Could not export Graph to PNG file", e);
			}
		}
		return null;
	}

}
