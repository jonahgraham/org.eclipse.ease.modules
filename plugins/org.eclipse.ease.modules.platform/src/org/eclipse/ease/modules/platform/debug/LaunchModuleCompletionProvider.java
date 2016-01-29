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
package org.eclipse.ease.modules.platform.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.platform.PluginConstants;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;

public class LaunchModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		if (context.getType() == Type.STRING_LITERAL) {
			final String caller = context.getCaller();
			final int param = context.getParameterOffset();
			if (caller.endsWith("launch") || caller.endsWith("launchUI")) {
				return param == 0 || param == 1;
			}
			if (caller.endsWith("getLaunchConfiguration")) {
				return param == 0;
			}
		}
		return false;
	}

	@Override
	protected void prepareProposals(ICompletionContext context) {
		if (context.getParameterOffset() == 0) {

			try {
				final ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
				for (final ILaunchConfiguration configuration : configurations) {
					final String name = configuration.getName();
					final ILaunchConfigurationType type = configuration.getType();
					final String typeName = type.getName();
					final String display = name + " - " + typeName;
					addProposal(display, name, DebugUITools.getDefaultImageDescriptor(configuration), 0, null);
				}
			} catch (final CoreException e) {
				Logger.warning(PluginConstants.PLUGIN_ID, "Code Completion: could not read launch configurations", e);
			}

		} else {
			// TODO: Make this parameter dependent on the selected launch config
			// to only populate the relevant modes
			final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();
			final ILaunchGroup[] launchGroups = DebugUITools.getLaunchGroups();
			final Map<String, ILaunchGroup> modes = new HashMap<String, ILaunchGroup>();
			for (final ILaunchGroup launchGroup : launchGroups) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
			for (final ILaunchGroup launchGroup : modes.values()) {
				final String display = launchGroup.getLabel().replace("&", "");
				final String name = launchGroup.getMode();
				final ImageDescriptor imageDescriptor = launchGroup.getImageDescriptor();
				addProposal(display, name, imageDescriptor, 0, null);
			}

		}
	}
}