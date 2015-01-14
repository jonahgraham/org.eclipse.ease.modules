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
package org.eclipse.ease.modules.unittest.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class TestSuiteCreationPage extends WizardNewFileCreationPage {

	public TestSuiteCreationPage(final String pageName, final IStructuredSelection selection) {
		super(pageName, selection);
	}

	@Override
	protected InputStream getInitialContents() {
		return new ByteArrayInputStream(new TestSuiteModel().toMemento().toString().getBytes());
	}
}
