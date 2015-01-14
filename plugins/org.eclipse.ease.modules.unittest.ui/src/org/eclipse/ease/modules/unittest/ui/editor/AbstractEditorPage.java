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

import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

public abstract class AbstractEditorPage extends FormPage {

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public AbstractEditorPage(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public AbstractEditorPage(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 *
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();
		final Composite body = form.getBody();

		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);

		form.setText(getPageTitle());

		// add menu items
		final ToolBarManager manager = (ToolBarManager) form.getToolBarManager();
		final IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
		menuService.populateContributionManager(manager, "toolbar:" + TestSuiteEditor.EDITOR_ID);
		manager.update(true);
	}

	@Override
	public void setFocus() {
		super.setFocus();

		update();
	}

	protected TestSuiteModel getModel() {
		return ((TestSuiteEditor) getEditor()).getModel();
	}

	protected void setDirty() {
		((TestSuiteEditor) getEditor()).setDirty();
	}

	protected abstract String getPageTitle();

	protected abstract void update();
}
