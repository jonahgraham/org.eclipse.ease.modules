/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.ui.launching;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.launching.LaunchConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

	private boolean fDisableUpdate = false;

	private Text txtSourceFile;

	private Button chkSuspendOnScript;

	private Button chkShowDynamicScript;

	private ComboViewer comboViewer;
	private Button chkSuspendOnStartup;

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConstants.FILE_LOCATION, "");
		configuration.setAttribute(LaunchConstants.SCRIPT_ENGINE, "");
		configuration.setAttribute(LaunchConstants.SUSPEND_ON_STARTUP, false);
		configuration.setAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, false);
		configuration.setAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, false);
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		fDisableUpdate = true;

		txtSourceFile.setText("");
		chkShowDynamicScript.setSelection(false);
		chkSuspendOnStartup.setSelection(false);
		chkSuspendOnScript.setSelection(false);

		try {
			txtSourceFile.setText(configuration.getAttribute(LaunchConstants.FILE_LOCATION, ""));
			populateScriptEngines();
			// TODO select correct engine from configuration
			final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
			final EngineDescription engineDescription = scriptService.getEngineByID(configuration.getAttribute(LaunchConstants.SCRIPT_ENGINE, ""));
			if (engineDescription != null)
				comboViewer.setSelection(new StructuredSelection(engineDescription));

			chkShowDynamicScript.setSelection(configuration.getAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, false));
			chkSuspendOnStartup.setSelection(configuration.getAttribute(LaunchConstants.SUSPEND_ON_STARTUP, false));
			chkSuspendOnScript.setSelection(configuration.getAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, false));

			chkSuspendOnScript.setEnabled(chkSuspendOnStartup.getSelection());

		} catch (final CoreException e) {
		}

		fDisableUpdate = false;
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConstants.FILE_LOCATION, txtSourceFile.getText());

		final IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
		if (!selection.isEmpty()) {
			final EngineDescription engineDescription = (EngineDescription) selection.getFirstElement();
			configuration.setAttribute(LaunchConstants.SCRIPT_ENGINE, engineDescription.getID());
		}

		configuration.setAttribute(LaunchConstants.SUSPEND_ON_STARTUP, chkSuspendOnStartup.getSelection());
		configuration.setAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, chkSuspendOnScript.getSelection() & chkSuspendOnStartup.getSelection());
		configuration.setAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, chkShowDynamicScript.getSelection());
	}

	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		// allow launch when a file is selected and file exists
		try {
			final boolean resourceExists = ResourceTools.exists(launchConfig.getAttribute(LaunchConstants.FILE_LOCATION, ""));
			if (resourceExists) {
				// check engine
				final String selectedEngineID = launchConfig.getAttribute(LaunchConstants.SCRIPT_ENGINE, "");
				final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
				for (final EngineDescription description : scriptService.getEngines()) {
					if (description.getID().equals(selectedEngineID))
						return true;
				}

				setErrorMessage("Invalid script engine selected.");

			} else
				setErrorMessage("Invalid source file selected.");

		} catch (final CoreException e) {
			setErrorMessage("Invalid launch configuration detected.");
		}

		return false;
	}

	@Override
	public boolean canSave() {
		// allow save when a file location is entered - no matter if the file exists or not and an engine is selected
		return (!txtSourceFile.getText().isEmpty()) && (!comboViewer.getSelection().isEmpty());
	}

	@Override
	public String getMessage() {
		return "Please select a script file.";
	}

	@Override
	public String getName() {
		return "Global";
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(final Composite parent) {
		final Composite topControl = new Composite(parent, SWT.NONE);
		topControl.setLayout(new GridLayout(1, false));

		final Group grpScriptSource = new Group(topControl, SWT.NONE);
		grpScriptSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpScriptSource.setText("Testsuite Source");
		grpScriptSource.setLayout(new GridLayout(2, false));

		txtSourceFile = new Text(grpScriptSource, SWT.BORDER);
		txtSourceFile.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				populateScriptEngines();
			}
		});
		txtSourceFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		final Button btnBrowseProject = new Button(grpScriptSource, SWT.NONE);
		btnBrowseProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(), new WorkbenchLabelProvider(),
						new FileFilterContentProvider(new String[] { "suite" }));
				dialog.setTitle("Select testsuite source file");
				dialog.setMessage("Select the testsuite file to execute:");
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				if (dialog.open() == Window.OK)
					txtSourceFile.setText("workspace:/" + ((IFile) dialog.getFirstResult()).getFullPath().toPortableString());
			}
		});
		btnBrowseProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnBrowseProject.setText("Browse Workspace...");

		final Button btnBrowseFilesystem = new Button(grpScriptSource, SWT.NONE);
		btnBrowseFilesystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.suite", "*.*" });
				dialog.setFilterNames(new String[] { "Test Suites", "All Files" });
				final String fileName = dialog.open();
				txtSourceFile.setText(new File(fileName).toURI().toString());
			}
		});
		btnBrowseFilesystem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnBrowseFilesystem.setText("Browse Filesystem...");

		final Group grpExecutionEngine = new Group(topControl, SWT.NONE);
		grpExecutionEngine.setLayout(new GridLayout(1, false));
		grpExecutionEngine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpExecutionEngine.setText("Execution Engine");

		comboViewer = new ComboViewer(grpExecutionEngine, SWT.NONE);
		final Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});

		// options only available in debug mode
		final Group group = new Group(topControl, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		group.setText("Debug Options");

		chkShowDynamicScript = new Button(group, SWT.CHECK);
		chkShowDynamicScript.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!fDisableUpdate)
					updateLaunchConfigurationDialog();
			}
		});
		chkShowDynamicScript.setText("Show dynamic script content");

		chkSuspendOnStartup = new Button(group, SWT.CHECK);
		chkSuspendOnStartup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				chkSuspendOnScript.setEnabled(chkSuspendOnStartup.getSelection());

				if (!fDisableUpdate)
					updateLaunchConfigurationDialog();
			}
		});
		chkSuspendOnStartup.setText("Suspend on startup");

		chkSuspendOnScript = new Button(group, SWT.CHECK);
		final GridData gd_chkSuspendOnScript = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_chkSuspendOnScript.horizontalIndent = 20;
		chkSuspendOnScript.setLayoutData(gd_chkSuspendOnScript);
		chkSuspendOnScript.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!fDisableUpdate)
					updateLaunchConfigurationDialog();
			}
		});
		chkSuspendOnScript.setText("Suspend on script load");

		setControl(topControl);
	}

	protected void populateScriptEngines() {

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		ScriptType scriptType = null;

		// resolve script type by file extension
		final String sourceFile = txtSourceFile.getText();
		scriptType = scriptService.getScriptType(sourceFile);

		// if (scriptType != null) {
		// final List<EngineDescription> engines = scriptService.getEngines(scriptType.getName());
		final Collection<EngineDescription> engines = scriptService.getEngines();
		comboViewer.setInput(engines);
		comboViewer.refresh();

		// set preferred engine
		// if (!engines.isEmpty())
		// comboViewer.setSelection(new StructuredSelection(engines.get(0)));
		// }

		updateLaunchConfigurationDialog();
	}
}
