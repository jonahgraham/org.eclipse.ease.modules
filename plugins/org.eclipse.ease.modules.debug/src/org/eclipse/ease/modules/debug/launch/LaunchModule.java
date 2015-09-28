/*******************************************************************************
 * Copyright (c) 2015 Jonah Graham and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.debug.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.swt.widgets.Display;

/**
 * Methods in the launch module ease writing scripts to perform complicated
 * launch set-up and control. The key method is {@link #launch(String, String)}.
 * <p>
 * A simple example of its use is:
 *
 * <pre>
 * loadModule("/System/Launch")
 * launch("Client", "debug")
 * </pre>
 *
 * where "Client" is the name of a launch configuration in the workbench and
 * "debug" is the launch mode to use.
 * <p>
 * More examples of using the launch method are available in JavaScript
 * Snippets.
 */
public class LaunchModule extends AbstractScriptModule {
	public static final String MODULE_NAME = "/System/Launch";

	/**
	 * Obtain the platform launch manager. This allows access to the Eclipse
	 * debug core launch manager, allowing control over all non-UI aspects of
	 * launches. The most valuable of these should be wrapped for ideal script
	 * usage and made available in the module itself.
	 *
	 * @return the launch manager
	 */
	@WrapToScript
	public ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns an array of all the Launch Configuration Names known to the
	 * Launch Manager. These names can be used as the argument to the
	 * {@link #getLaunchConfiguration(String)}, {@link #launch(String, String)}
	 * and {@link #launchUI(String, String)} methods.
	 *
	 * @return array of launch configuration names.
	 * @throws CoreException
	 */
	@WrapToScript
	public String[] getLaunchConfigurationNames() throws CoreException {
		ILaunchConfiguration[] configurations = getLaunchConfigurations();
		String[] names = new String[configurations.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = configurations[i].getName();
		}
		return names;
	}

	/**
	 * Returns an array of all the Launch Configurations known to the Launch
	 * Manager. These can be used as the argument to
	 * {@link #launch(ILaunchConfiguration, String)} and
	 * {@link #launchUI(ILaunchConfiguration, String)} methods.
	 *
	 * @return array of launch configurations
	 * @throws CoreException
	 */
	@WrapToScript
	public ILaunchConfiguration[] getLaunchConfigurations() throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
	}

	/**
	 * Return the launch configuration given by name parameter. The launch
	 * configuration can be edited or otherwise operated on.
	 *
	 * @param name
	 *            the launch configuration name
	 * @return the named launch configuration
	 * @throws IllegalArgumentException
	 *             if the name is not known to the launch manager
	 * @throws CoreException
	 * @see {@link ILaunchConfiguration#getWorkingCopy()}.
	 */
	@WrapToScript
	public ILaunchConfiguration getLaunchConfiguration(String name) throws CoreException, IllegalArgumentException {
		ILaunchConfiguration[] configurations = getLaunchConfigurations();
		for (ILaunchConfiguration configuration : configurations) {
			if (configuration.getName().equals(name)) {
				return configuration;
			}
		}

		throw new IllegalArgumentException(
				"Unknown launch configuration name, use getLaunchConfigurationNames() to obtain all names");
	}

	/**
	 * Launch the configuration given by name and return the ILaunch for further
	 * processing.
	 * <p>
	 * This is the way to launch a configuration within a script which is itself
	 * launched. Consider using {@link #launchUI(String, String)} if a full UI
	 * style launch is required, for example when invoked from the interactive
	 * console.
	 *
	 * @param name
	 *            the launch configuration name
	 * @param mode
	 *            the launch mode, normally "debug" or "run" but can be any of
	 *            the launch modes available on the platform. The default value
	 *            is "run".
	 * @return the reslting launch
	 * @throws IllegalArgumentException
	 *             if the name is not known to the launch manager
	 * @throws CoreException
	 */
	@WrapToScript
	public ILaunch launch(String name, @ScriptParameter(defaultValue = "run") String mode) throws CoreException {
		return launch(getLaunchConfiguration(name), mode);
	}

	/**
	 * Launch the configuration and return the ILaunch for further processing.
	 * <p>
	 * This is the way to launch a configuration within a script which is itself
	 * launched. Consider using {@link #launchUI(String, String)} if a full UI
	 * style launch is required, for example when invoked from the interactive
	 * console.
	 *
	 * @param configuration
	 *            the launch configuration
	 * @param mode
	 *            the launch mode, normally "debug" or "run" but can be any of
	 *            the launch modes available on the platform. The default value
	 *            is "run".
	 * @return the reslting launch
	 * @throws CoreException
	 */
	@WrapToScript
	public ILaunch launch(final ILaunchConfiguration configuration,
			@ScriptParameter(defaultValue = "run") final String mode) throws CoreException {
		return configuration.launch(mode, new NullProgressMonitor());
	}

	/**
	 * Launch the configuration given by name in the UI thread. This method
	 * respects the workspace settings for things like building before
	 * launching.
	 * <p>
	 * This method, unlike {@link #launch(String, String)}, does not return the
	 * ILaunch because it is delegated to run via the UI thread and perform UI
	 * tasks before the launch (such as prompting the user).
	 *
	 * @param name
	 *            the launch configuration name
	 * @param mode
	 *            the launch mode, normally "debug" or "run" but can be any of
	 *            the launch modes available on the platform. The default value
	 *            is "run".
	 * @throws CoreException
	 */
	@WrapToScript
	public void launchUI(String name, @ScriptParameter(defaultValue = "run") String mode) throws CoreException {
		final ILaunchConfiguration configuration = getLaunchConfiguration(name);
		launchUI(configuration, mode);
	}

	/**
	 * Launch the configuration in the UI thread. This method respects the
	 * workspace settings for things like building before launching.
	 * <p>
	 * This method, unlike {@link #launch(String, String)}, does not return the
	 * ILaunch because it is delegated to run via the UI thread and perform UI
	 * tasks before the launch (such as prompting the user).
	 *
	 * @param name
	 *            the launch configuration name
	 * @param mode
	 *            the launch mode, normally "debug" or "run" but can be any of
	 *            the launch modes available on the platform. The default value
	 *            is "run".
	 * @throws CoreException
	 */
	@WrapToScript
	public void launchUI(final ILaunchConfiguration configuration,
			@ScriptParameter(defaultValue = "run") final String mode) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugUITools.launch(configuration, mode);
			}
		});
	}

}
