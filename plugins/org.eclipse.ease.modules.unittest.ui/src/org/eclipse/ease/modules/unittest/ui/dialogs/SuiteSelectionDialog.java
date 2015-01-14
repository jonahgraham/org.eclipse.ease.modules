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
package org.eclipse.ease.modules.unittest.ui.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class SuiteSelectionDialog extends ElementTreeSelectionDialog {

	public SuiteSelectionDialog(final Shell parent) {
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());

		setAllowMultiple(false);

		addFilter(new ViewerFilter() {

			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				if (element instanceof IResource) {
					if (element instanceof IContainer) {
						if ((element instanceof IProject) && (!((IProject) element).isOpen()))
							return false;

						return containsSuites((IContainer) element);
					}

					return isSuite((IResource) element);
				}

				return false;
			}

			private boolean containsSuites(final IContainer element) {
				try {
					for (final IResource resource : element.members()) {
						if (resource instanceof IContainer) {
							if (containsSuites((IContainer) resource))
								return true;
						} else {
							if (isSuite(resource))
								return true;
						}
					}
				} catch (final CoreException e) {
					// ignore
				}

				return false;
			}

			private boolean isSuite(final IResource resource) {
				return "suite".equals(resource.getProjectRelativePath().getFileExtension());
			}
		});

		setInput(ResourcesPlugin.getWorkspace().getRoot());
	}

	public IFile getSuiteFile() {
		final Object result = getFirstResult();
		if (result instanceof IFile)
			return (IFile) result;

		return null;
	}
}
