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
package org.eclipse.ease.modules.unittest.ui.launching;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.modules.unittest.ITestListener;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.launching.LaunchConstants;
import org.eclipse.ease.ui.tools.AbstractLaunchDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class TestSuiteLaunchDelegate extends AbstractLaunchDelegate {

	private static final String LAUNCH_CONFIGURATION_ID = "org.eclipse.ease.unittest.launchConfigurationType";

	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch, final IProgressMonitor monitor) throws CoreException {

		final Object resource = ResourceTools.resolveFile(getFileLocation(configuration), null, true);
		if (resource instanceof IFile) {
			try {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);
					}
				});

				// create testsuite
				final TestSuite suiteToRun = new TestSuite((IFile) resource);

				// TODO activate only if visualization is set to "view"
				// activate view
				final IViewPart view = UIModule.showView(UnitTestView.VIEW_ID);
				if (view instanceof ITestListener)
					suiteToRun.addTestListener((ITestListener) view);

				// set debug options
				if (ILaunchManager.DEBUG_MODE.equals(mode)) {
					suiteToRun.setDebugOptions(launch);
				}

				suiteToRun.run();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// could not open view
				throw new RuntimeException(e);
			}
		}

		// TODO allow to run from external resources too (FIle / InputStream)
	}

	@Override
	protected ILaunchConfiguration createLaunchConfiguration(final IResource file, final String mode) throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_ID);

		final ILaunchConfigurationWorkingCopy configuration = type.newInstance(null, file.getName());
		configuration.setAttribute(LaunchConstants.FILE_LOCATION, ResourceTools.toAbsoluteLocation(file, null));

		// save and return new configuration
		configuration.doSave();

		return configuration;
	}

	@Override
	protected String getFileLocation(final ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LaunchConstants.FILE_LOCATION, "");
	}

	@Override
	protected String getLaunchConfigurationId() {
		return LAUNCH_CONFIGURATION_ID;
	}
}
