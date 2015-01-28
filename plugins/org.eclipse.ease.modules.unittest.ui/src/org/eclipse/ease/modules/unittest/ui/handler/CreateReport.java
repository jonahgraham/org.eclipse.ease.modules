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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.dialogs.CreateReportDialog;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Create test report command.
 */
public class CreateReport extends AbstractHandler implements IHandler {

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof UnitTestView) {

			final TreeViewer viewer = ((UnitTestView) part).getTreeViewer();
			final Object input = viewer.getInput();
			if ((input instanceof Object[]) && (((Object[]) input).length > 0)) {
				final Object suite = ((Object[]) input)[0];
				if (suite instanceof TestSuite) {

					// we found a test suite to export
					final CreateReportDialog dialog = new CreateReportDialog(HandlerUtil.getActiveShell(event));
					if (dialog.open() == Window.OK) {

						try {
							String filename = dialog.getFileName();
							final String extension = dialog.getReport().getDefaultExtension();
							if (!filename.toLowerCase().endsWith(extension))
								filename += extension;

							final FileWriter writer = new FileWriter(new File(filename));
							writer.write(dialog.getReport().createReport(dialog.getTitle(), dialog.getDescription(), (TestSuite) suite));
							writer.close();

							if (dialog.isOpenReport()) {
								// open report after creation
								if (Platform.getOS().startsWith("win"))
									Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + new File(filename).toURI().toString());

								else if (Platform.getOS().startsWith("linux")) {
									String desktop = System.getenv("XDG_CURRENT_DESKTOP");
									if (desktop == null)
										desktop = System.getenv("sun.desktop");

									if ("KDE".equalsIgnoreCase(desktop))
										Runtime.getRuntime().exec("kfmclient exec " + new File(filename).toURI().toString());

									else if ("GNOME".equalsIgnoreCase(desktop))
										Runtime.getRuntime().exec("gnome-open " + new File(filename).toURI().toString());

								} else {
									// TODO add support for other platforms
									MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not open report", "Support for your platform ("
											+ Platform.getOS() + ") not implemented. Please raise a bug if needed.");
								}
							}
						} catch (final IOException e) {
							MessageDialog.openError(HandlerUtil.getActiveShell(event), "Create Report failed",
									"Could not open file for writing. Report could not be saved");
							Logger.logError("Could not create report file \"" + dialog.getFileName() + "\"", e);
						}
					}
				}
			}
		}

		return null;
	}
}
