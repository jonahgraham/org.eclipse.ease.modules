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

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

public class Variables extends AbstractEditorPage {
	private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("([a-zA-Z_$][0-9a-zA-Z_$]*)");

	private Table table_1;

	private TableViewer tableViewer;

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public Variables(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public Variables(final FormEditor editor, final String id, final String title) {
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

		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		final Label lblDefineVariablesThat = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblDefineVariablesThat.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(lblDefineVariablesThat, true, true);
		lblDefineVariablesThat.setText("Define variables that will be visible in your scripts. Ordering is important if variables depend on each other.");

		final Composite composite = new Composite(managedForm.getForm().getBody(), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		managedForm.getToolkit().adapt(composite);
		managedForm.getToolkit().paintBordersFor(composite);
		final TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		final Table table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer = new TableViewer(table);
		table_1 = tableViewer.getTable();
		table_1.setHeaderVisible(true);
		table_1.setLinesVisible(true);
		managedForm.getToolkit().paintBordersFor(table_1);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnVariable = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnVariable, new ColumnWeightData(100, 100, true));
		tblclmnVariable.setText("Variable");
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Variable) element).getName();
			}
		});
		tableViewerColumn.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(final Object element, final Object value) {
				if (checkName(value.toString())) {
					((Variable) element).setName(value.toString());
					setDirty();

					tableViewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(final Object element) {
				return ((Variable) element).getName();
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(table_1);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}
		});

		final TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnContent = tableViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnContent, new ColumnWeightData(100, 100, true));
		tblclmnContent.setText("Content");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Variable) element).getName();
			}
		});
		tableViewerColumn_1.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(final Object element, final Object value) {
				((Variable) element).setContent(value.toString());
				setDirty();

				tableViewer.update(element, null);
			}

			@Override
			protected Object getValue(final Object element) {
				return ((Variable) element).getContent();
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(table_1);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}
		});

		final TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnDescription = tableViewerColumn_3.getColumn();
		tcl_composite.setColumnData(tblclmnDescription, new ColumnWeightData(100, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnDescription.setText("Description");
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Variable) element).getDescription();
			}
		});
		tableViewerColumn_3.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(final Object element, final Object value) {
				((Variable) element).setDescription(value.toString());
				setDirty();

				tableViewer.update(element, null);
			}

			@Override
			protected Object getValue(final Object element) {
				return ((Variable) element).getDescription();
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(table_1);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}
		});
		final Button btnNewButton = new Button(managedForm.getForm().getBody(), SWT.FLAT);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getModel().addVariable(createVariable(), "my content", null);
				setDirty();

				tableViewer.refresh();
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(btnNewButton, true, true);
		btnNewButton.setText("Add");

		final Button btnDelete = new Button(managedForm.getForm().getBody(), SWT.FLAT);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				for (final Object variable : selection.toList())
					getModel().removeVariable((Variable) variable);

				tableViewer.refresh();

				setDirty();
			}
		});
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(btnDelete, true, true);
		btnDelete.setText("Remove");

		final Button btnUp = new Button(managedForm.getForm().getBody(), SWT.FLAT);
		btnUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				if (!selection.isEmpty()) {
					final Variable variable = (Variable) selection.getFirstElement();
					moveUp(variable);
					tableViewer.refresh();
					// TODO we need to change variables order in model, too

					setDirty();
				}
			}
		});
		final GridData gd_btnUp = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnUp.verticalIndent = 30;
		btnUp.setLayoutData(gd_btnUp);
		managedForm.getToolkit().adapt(btnUp, true, true);
		btnUp.setText("Up");

		final Button btnDown = new Button(managedForm.getForm().getBody(), SWT.FLAT);
		btnDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				if (!selection.isEmpty()) {
					final Variable variable = (Variable) selection.getFirstElement();
					moveDown(variable);
					tableViewer.refresh();
					// TODO we need to change variables order in model, too
					setDirty();
				}
			}
		});
		btnDown.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		managedForm.getToolkit().adapt(btnDown, true, true);
		btnDown.setText("Down");

		update();
	}

	private void moveUp(final Variable element) {
		final List<Variable> input = (List<Variable>) tableViewer.getInput();
		final int index = input.indexOf(element);
		if (index > 0) {
			input.set(index, input.get(index - 1));
			input.set(index - 1, element);
		}
	}

	private void moveDown(final Variable element) {
		final List<Variable> input = (List<Variable>) tableViewer.getInput();
		final int index = input.indexOf(element);
		if (index < (input.size() - 1)) {
			input.set(index, input.get(index + 1));
			input.set(index + 1, element);
		}
	}

	private String createVariable() {
		// find available variable name
		final String baseName = "myVariable";
		String newName = baseName;
		int index = 1;
		while (!checkName(newName))
			newName = baseName + "_" + Integer.toString(index++);

		return newName;
	}

	private boolean checkName(final String newName) {
		if (VARIABLE_NAME_PATTERN.matcher(newName).matches()) {
			// check if name already exists
			for (final Variable variable : getModel().getVariables()) {
				if (newName.equals(variable.getName()))
					return false;
			}

			return true;
		}

		return false;
	}

	@Override
	protected String getPageTitle() {
		return "Variables";
	}

	@Override
	protected void update() {
		tableViewer.setInput(getModel().getVariables());
		tableViewer.refresh();
	}
}
