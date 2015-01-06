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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

public class SetupTeardown extends AbstractEditorPage {

	private boolean fEnableChangeTracker = true;

	// UI elements
	private Text txtCode;
	private ComboViewer cmbCodeFragment;

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public SetupTeardown(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public SetupTeardown(final FormEditor editor, final String id, final String title) {
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

		final Label lblCustomCodeUsed = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblCustomCodeUsed.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(lblCustomCodeUsed, true, true);
		lblCustomCodeUsed
				.setText("Custom code used to setup test suites, sets and tests. To run custom code actions use executeUserCode(<location>); in your scripts.");

		final Label label = new Label(managedForm.getForm().getBody(), SWT.NONE);
		managedForm.getToolkit().adapt(label, true, true);
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		final Label lblLocation = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(lblLocation, true, true);
		lblLocation.setText("Location:");

		final CCombo combo_1 = new CCombo(managedForm.getForm().getBody(), SWT.READ_ONLY | SWT.FLAT);
		combo_1.setEditable(true);
		combo_1.setVisibleItemCount(10);
		final GridData gd_combo_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo_1.widthHint = 218;
		combo_1.setLayoutData(gd_combo_1);
		cmbCodeFragment = new ComboViewer(combo_1);
		managedForm.getToolkit().paintBordersFor(combo_1);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		cmbCodeFragment.setContentProvider(ArrayContentProvider.getInstance());
		cmbCodeFragment.setInput(getLocations());

		txtCode = new Text(managedForm.getForm().getBody(), SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtCode.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (fEnableChangeTracker) {
					getModel().setCodeFragment(combo_1.getText(), txtCode.getText());
					setDirty();
				}
			}
		});
		txtCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().adapt(txtCode, true, true);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		combo_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final String code = getModel().getCodeFragment(combo_1.getText());

				fEnableChangeTracker = false;
				if ((code == null) || (code.isEmpty()))
					txtCode.setText(getDefaultText(combo_1.getText()));
				else
					txtCode.setText(code);

				fEnableChangeTracker = true;
			}
		});
	}

	private static String getDefaultText(final String location) {
		if (TestSuiteModel.CODE_LOCATION_TESTSUITE_SETUP.equals(location))
			return "// TestSuite setup is run once before the tests are run.\r\n"
					+ "// User defined variables are available and can be modified if needed.\r\n"
					+ "// All variables set by this code will be available in the tests, however\r\n"
					+ "// functions defined here will not be available. To raise errors use the\r\n" + "// failure(\"Reason\") function.";

		if (TestSuiteModel.CODE_LOCATION_TESTSUITE_TEARDOWN.equals(location))
			return "// TestSuite teardown is run once after all tests are finished.";

		if (TestSuiteModel.CODE_LOCATION_TESTFILE_SETUP.equals(location))
			return "// TestFile setup is run at the beginning of a test file,\r\n" + "// right before the script code is executed.\r\n";

		if (TestSuiteModel.CODE_LOCATION_TESTFILE_TEARDOWN.equals(location))
			return "// TestFile teardown is run at the end of a test file.\r\n";

		if (TestSuiteModel.CODE_LOCATION_TEST_SETUP.equals(location))
			return "// Test setup is run when startTest() is called";

		if (TestSuiteModel.CODE_LOCATION_TEST_TEARDOWN.equals(location))
			return "// Test teardown is run when endTest() is called.\r\n" + "// If a test runs into a failure() or throws an Exception\r\n"
					+ "// this code will not be reached.";

		return "";
	}

	private Collection<String> getLocations() {
		final List<String> result = new ArrayList<String>();

		result.add(TestSuiteModel.CODE_LOCATION_TESTSUITE_SETUP);
		result.add(TestSuiteModel.CODE_LOCATION_TESTFILE_SETUP);
		result.add(TestSuiteModel.CODE_LOCATION_TEST_SETUP);
		result.add(TestSuiteModel.CODE_LOCATION_TEST_TEARDOWN);
		result.add(TestSuiteModel.CODE_LOCATION_TESTFILE_TEARDOWN);
		result.add(TestSuiteModel.CODE_LOCATION_TESTSUITE_TEARDOWN);

		for (final String key : getModel().getCodeFragments().keySet()) {
			if (!result.contains(key))
				result.add(key);
		}

		return result;
	}

	@Override
	protected String getPageTitle() {
		return "Setup / Teardown Code";
	}

	@Override
	protected void update() {
		cmbCodeFragment.setInput(getLocations());

	}
}
