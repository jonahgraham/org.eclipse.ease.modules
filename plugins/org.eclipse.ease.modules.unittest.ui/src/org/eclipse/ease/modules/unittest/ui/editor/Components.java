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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class Components extends AbstractEditorPage {

	private static final String VISUALIZATION_GUI = "Unit Test View";
	private static final String VISUALIZATION_SYSOUT = "System.out";
	private static final String VISUALIZATION_NONE = "None";

	protected String fVisualizationIdentifier = VISUALIZATION_GUI;

	// UI elements
	private ContainerCheckedTreeViewer fTestTree;
	private Spinner spinner;
	private Combo combo;
	private Button btnPromoteErrorsToFailures;
	private Button btnStopSuiteOnFailure;
	private Button fbutton;

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public Components(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public Components(final FormEditor editor, final String id, final String title) {
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

		final GridLayout gridLayout = new GridLayout(5, false);
		managedForm.getForm().getBody().setLayout(gridLayout);

		final Label lblNewLabel = new Label(managedForm.getForm().getBody(), SWT.NONE);
		final GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
		gd_lblNewLabel.verticalIndent = 10;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		managedForm.getToolkit().adapt(lblNewLabel, true, true);
		lblNewLabel.setText("Select all test files that should be included in this suite.");
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		final Section sctnIncludedTests = managedForm.getToolkit().createSection(managedForm.getForm().getBody(), ExpandableComposite.TITLE_BAR);
		sctnIncludedTests.setDescription("");
		sctnIncludedTests.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7));
		managedForm.getToolkit().paintBordersFor(sctnIncludedTests);
		sctnIncludedTests.setText("Included Tests");

		final Composite composite = managedForm.getToolkit().createComposite(sctnIncludedTests, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnIncludedTests.setClient(composite);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		final Tree tree = managedForm.getToolkit().createTree(composite, SWT.NONE | SWT.CHECK);
		managedForm.getToolkit().paintBordersFor(tree);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "Use at max.", SWT.NONE);

		// tree viewer for tests
		fTestTree = new ContainerCheckedTreeViewer(tree);
		fTestTree.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					final Object element = selection.getFirstElement();
					if (element instanceof IFile) {
						try {
							new UIModule().showEditor(element);
						} catch (final PartInitException e) {
							// TODO handle this exception (but for now, at least know it happened)
							throw new RuntimeException(e);
						}
					}
				}
			}
		});
		fTestTree.setContentProvider(new WorkbenchContentProvider());
		fTestTree.setLabelProvider(new WorkbenchLabelProvider());
		fTestTree.addFilter(new ViewerFilter() {

			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				// hide config elements

				if (element instanceof IResource) {
					if (((IResource) element).getName().startsWith("."))
						return false;

					// show containers
					if (element instanceof IContainer)
						return containsScript((IContainer) element);

					// check for script
					return isScript((IResource) element);
				}

				return true;
			}

			private boolean isScript(final IResource resource) {
				return (resource instanceof IFile) && (resource.toString().toLowerCase().endsWith(".js"));
			}

			private boolean containsScript(final IContainer container) {
				try {
					for (final IResource resource : container.members()) {
						if (resource instanceof IContainer) {
							if (containsScript((IContainer) resource))
								return true;
						} else if (isScript(resource))
							return true;
					}
				} catch (final CoreException e) {
				}

				return false;
			}
		});
		fTestTree.setComparator(new ViewerComparator() {
			@Override
			public int category(final Object element) {
				return (element instanceof IContainer) ? 0 : 1;
			}
		});

		spinner = new Spinner(managedForm.getForm().getBody(), SWT.BORDER);
		spinner.setPageIncrement(5);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		spinner.setMinimum(1);
		spinner.setSelection(1);
		managedForm.getToolkit().adapt(spinner);
		managedForm.getToolkit().paintBordersFor(spinner);
		spinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {

				final int maxThreads = getModel().getFlag(TestSuiteModel.FLAG_MAX_THREADS, 1);
				if (spinner.getSelection() != maxThreads) {
					getModel().setFlag(TestSuiteModel.FLAG_MAX_THREADS, spinner.getSelection());
					setDirty();
				}
			}
		});

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "instances in parallel", SWT.NONE);
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		final Label lblTestVisualization = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblTestVisualization.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		managedForm.getToolkit().adapt(lblTestVisualization, true, true);
		lblTestVisualization.setText("Test Visualization");
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		combo = new Combo(managedForm.getForm().getBody(), SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				fVisualizationIdentifier = combo.getText();
			}
		});
		final GridData gd_combo = new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1);
		gd_combo.horizontalIndent = 20;
		combo.setLayoutData(gd_combo);
		managedForm.getToolkit().adapt(combo);
		managedForm.getToolkit().paintBordersFor(combo);
		combo.setItems(new String[] { VISUALIZATION_GUI, VISUALIZATION_SYSOUT, VISUALIZATION_NONE });
		combo.setText(combo.getItem(0));
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		btnPromoteErrorsToFailures = managedForm.getToolkit().createButton(managedForm.getForm().getBody(), "Promote errors to failures (stops test file)",
				SWT.CHECK);
		btnPromoteErrorsToFailures.setToolTipText("Raised a failure instead of an error. If set, test file execution will stop on the first error.");
		btnPromoteErrorsToFailures.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getModel().setFlag(TestSuiteModel.FLAG_PROMOTE_ERRORS_TO_FAILURES, btnPromoteErrorsToFailures.getSelection());
				setDirty();
			}
		});
		btnPromoteErrorsToFailures.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1));
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		btnStopSuiteOnFailure = managedForm.getToolkit().createButton(managedForm.getForm().getBody(), "Stop suite on failure", SWT.CHECK);
		btnStopSuiteOnFailure.setToolTipText("Stops test suite execution upon a failure. If not set, the next test file will be executed.");
		btnStopSuiteOnFailure.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getModel().setFlag(TestSuiteModel.FLAG_STOP_SUITE_ON_FAILURE, btnStopSuiteOnFailure.getSelection());
				setDirty();
			}
		});
		btnStopSuiteOnFailure.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1));
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		fbutton = managedForm.getToolkit().createButton(managedForm.getForm().getBody(), "Execute teardown on failure", SWT.CHECK);
		fbutton.setSelection(true);
		fbutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getModel().setFlag(TestSuiteModel.FLAG_EXECUTE_TEARDOWN_ON_FAILURE, fbutton.getSelection());
				setDirty();
			}
		});
		fbutton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1));

		fTestTree.setInput(getModel().getFile().getProject());

		fTestTree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(final CheckStateChangedEvent event) {
				final TestSuiteModel model = getModel();
				model.getTestFiles().clear();

				for (final Object object : fTestTree.getCheckedElements()) {
					if (object instanceof IFile)
						model.addTestFile(ResourceTools.toProjectRelativeLocation(object, null));
				}

				setDirty();
			}
		});
	}

	@Override
	public void setFocus() {
		super.setFocus();

		update();
	}

	@Override
	protected void update() {
		final TestSuiteModel model = getModel();

		// FIXME if resource is out of sync model will be null, raising an exception here!!!
		fTestTree.setCheckedElements(new Object[0]);
		for (final String fileLocation : model.getTestFiles()) {
			final Object file = ResourceTools.resolveFile(fileLocation, model.getFile(), true);
			if (file != null)
				fTestTree.setChecked(file, true);
		}

		final int maxThreads = model.getFlag(TestSuiteModel.FLAG_MAX_THREADS, 1);
		spinner.setSelection(maxThreads);

		btnPromoteErrorsToFailures.setSelection(model.getFlag(TestSuiteModel.FLAG_PROMOTE_ERRORS_TO_FAILURES, false));
		btnStopSuiteOnFailure.setSelection(model.getFlag(TestSuiteModel.FLAG_STOP_SUITE_ON_FAILURE, false));
	}

	@Override
	protected String getPageTitle() {
		return "Test Components";
	}
}
