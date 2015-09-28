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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;

public class LaunchModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		if (context.getType() == Type.STRING_LITERAL) {
			String caller = context.getCaller();
			int param = context.getParameterOffset();
			if (caller.endsWith("launch")) {
				return param == 0 || param == 1;
			}
			if (caller.endsWith("getLaunchConfiguration")) {
				return param == 0;
			}
		}
		return false;
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		if (context.getParameterOffset() == 0) {

			final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

			try {
				ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager()
						.getLaunchConfigurations();
				for (ILaunchConfiguration configuration : configurations) {
					String name = configuration.getName();
					ILaunchConfigurationType type = configuration.getType();
					String typeName = type.getName();
					String display = name + " - " + typeName;
					addProposal(proposals, context, display, name,
							DebugUITools.getDefaultImageDescriptor(configuration), 0);
				}
				return proposals;
			} catch (CoreException e) {
				return Collections.emptyList();
			}
		} else {
			// TODO: Make this parameter dependent on the selected launch config
			// to only populate the relevant modes
			final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();
			ILaunchGroup[] launchGroups = DebugUITools.getLaunchGroups();
			Map<String, ILaunchGroup> modes = new HashMap<String, ILaunchGroup>();
			for (ILaunchGroup launchGroup : launchGroups) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
			for (ILaunchGroup launchGroup : modes.values()) {
				String display = launchGroup.getLabel().replace("&", "");
				String name = launchGroup.getMode();
				ImageDescriptor imageDescriptor = launchGroup.getImageDescriptor();
				addProposal(proposals, context, display, name, imageDescriptor, 0);
			}

			return proposals;
		}
	}
}