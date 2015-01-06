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
package org.eclipse.ease.modules.unittest.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

public class Description extends AbstractEditorPage {

	private Text txtDescription;

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public Description(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public Description(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 *
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		super.createFormContent(managedForm);

		managedForm.getForm().getBody().setLayout(new GridLayout(1, false));

		final Label lblCustomCodeUsed = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblCustomCodeUsed.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(lblCustomCodeUsed, true, true);
		lblCustomCodeUsed.setText("Description of the current TestSuite. For informational purposes only");

		txtDescription = new Text(managedForm.getForm().getBody(), SWT.WRAP | SWT.MULTI);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().adapt(txtDescription, true, true);

		// initialize field
		txtDescription.setText(getModel().getDescription());

		// add listener after initializing content
		txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				getModel().setDescription(txtDescription.getText());
				setDirty();
			}
		});
	}

	@Override
	protected String getPageTitle() {
		return "TestSuite Description";
	}

	@Override
	protected void update() {
		txtDescription.setText(getModel().getDescription());
	}
}
