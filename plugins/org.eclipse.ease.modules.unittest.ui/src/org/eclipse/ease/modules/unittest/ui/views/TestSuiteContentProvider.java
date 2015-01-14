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
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;

public class TestSuiteContentProvider extends AbstractVirtualTreeProvider {

	@Override
	protected void populateElements(final Object inputElement) {
		final TestSuite suite = (TestSuite) ((Object[]) inputElement)[0];

		for (final TestFile testfile : suite.getChildren()) {
			final Object file = testfile.getFile();
			if (file instanceof IFile)
				registerElement(((IFile) file).getParent().getProjectRelativePath(), testfile);
		}

		registerNodeReplacement(ROOT, suite);
		setShowRoot(true);
	}
}
