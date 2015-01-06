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

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public class RecentSuiteFilesContributionFactory extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator fServiceLocator;

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		fServiceLocator = serviceLocator;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IFile> files = Activator.getDefault().getRecentFiles();
		final IContributionItem[] items = new IContributionItem[files.size()];
		int index = 0;

		for (final IFile file : files) {
			// set parameter for command
			final HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put(LoadSuite.PARAMETER_SUITE_NAME, file.getFullPath().toString());

			final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(fServiceLocator, null, LoadSuite.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			contributionParameter.parameters = parameters;

			// name is the filename without file extension
			String name = file.getName();
			final String extension = file.getFileExtension();
			if (extension != null)
				name = name.substring(0, name.length() - extension.length() - 1);

			contributionParameter.label = name;

			contributionParameter.visibleEnabled = true;
			Activator.getDefault();
			contributionParameter.icon = Activator.getImageDescriptor("/images/test_suite.gif");

			items[index++] = new CommandContributionItem(contributionParameter);
		}

		return items;
	}
}
