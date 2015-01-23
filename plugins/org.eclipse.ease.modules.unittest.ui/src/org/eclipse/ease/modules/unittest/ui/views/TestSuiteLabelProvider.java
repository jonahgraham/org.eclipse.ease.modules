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
package org.eclipse.ease.modules.unittest.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TestSuiteLabelProvider extends LabelProvider {
	private final LocalResourceManager fResourceManager;

	public TestSuiteLabelProvider(final LocalResourceManager resourceManager) {
		fResourceManager = resourceManager;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof TestSuite) {
			final IFile file = ((TestSuite) element).getModel().getFile();
			return (file != null) ? file.getName() : "<dynamic>";

		} else if (element instanceof IPath)
			return ((IPath) element).lastSegment();

		return super.getText(element);
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof TestSuite)
			return fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_TEST_SUITE));

		if (element instanceof IPath)
			return fResourceManager.createImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));

		if (element instanceof TestFile)
			return fResourceManager.createImage(PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(((TestFile) element).getFile().toString()));

		return super.getImage(element);
	}
}
