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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.reporters.IReportGenerator;
import org.eclipse.ease.modules.unittest.reporters.ReportTools;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class CreateReportDialog extends Dialog {
	private static final String HISTORY_FILE = "export_history.xml";
	private static final String FILELOCATION = "filelocation";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String TYPE = "type";

	private Text mTxtFileName;
	private Text mTxtTitle;
	private Text mTxtDescription;

	private String mFileName = null;
	private String mTitle = "";
	private String mDescription = "";
	private Button btnOpenReportAfter;
	private boolean mOpenAfterSave;
	private Combo mCmbType;
	private String mType;

	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public CreateReportDialog(final Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Map<String, String> history = loadHistory();

		final Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 3;

		final Label lblType = new Label(container, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type:");

		mCmbType = new Combo(container, SWT.READ_ONLY);
		mCmbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		mCmbType.setItems(ReportTools.getReportTemplates().toArray(new String[0]));
		if ((history.containsKey(TYPE)) && (history.get(TYPE) != null))
			mTxtFileName.setText(history.get(TYPE));
		else
			mCmbType.setText(mCmbType.getItem(0));

		new Label(container, SWT.NONE);

		final Label lblFile = new Label(container, SWT.NONE);
		lblFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFile.setText("File:");

		mTxtFileName = new Text(container, SWT.BORDER);
		mTxtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if ((history.containsKey(FILELOCATION)) && (history.get(FILELOCATION) != null))
			mTxtFileName.setText(history.get(FILELOCATION));

		final Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText("Save Report to File");
				dialog.setOverwrite(true);

				// try to set default names & filters
				final IReportGenerator report = ReportTools.getReport(mCmbType.getText());
				if (report != null) {
					dialog.setFileName("report." + report.getDefaultExtension());
					dialog.setFilterExtensions(new String[] { "*." + report.getDefaultExtension() });
				} else {
					dialog.setFileName("report");
					dialog.setFilterExtensions(new String[] { "*.*" });
				}

				final String location = dialog.open();
				if (location != null)
					mTxtFileName.setText(location);
			}
		});
		btnBrowse.setText("Browse...");

		final Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		final Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title:");

		mTxtTitle = new Text(container, SWT.BORDER);
		mTxtTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if ((history.containsKey(TITLE)) && (history.get(TITLE) != null))
			mTxtTitle.setText(history.get(TITLE));

		final Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblDescription.setText("Description:");

		mTxtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		mTxtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		if ((history.containsKey(DESCRIPTION)) && (history.get(DESCRIPTION) != null))
			mTxtDescription.setText(history.get(DESCRIPTION));

		new Label(container, SWT.NONE);

		btnOpenReportAfter = new Button(container, SWT.CHECK);
		btnOpenReportAfter.setSelection(true);
		btnOpenReportAfter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnOpenReportAfter.setText("Open report after saving");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		final Button clearButton = createButton(parent, IDialogConstants.BACK_ID, "Clear", false);
		clearButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				mTxtFileName.setText("");
				mTxtTitle.setText("");
				mTxtDescription.setText("");
				btnOpenReportAfter.setSelection(false);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		mType = mCmbType.getText();
		mFileName = mTxtFileName.getText();
		mTitle = mTxtTitle.getText();
		mDescription = mTxtDescription.getText();
		mOpenAfterSave = btnOpenReportAfter.getSelection();

		final Map<String, String> history = new HashMap<String, String>();
		history.put(FILELOCATION, mTxtFileName.getText());
		history.put(TITLE, mTxtTitle.getText());
		history.put(DESCRIPTION, mTxtDescription.getText());
		history.put(TYPE, mCmbType.getText());
		saveHistory(history);

		super.okPressed();
	}

	public String getFileName() {
		return mFileName;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public boolean isOpenReport() {
		return mOpenAfterSave;
	}

	public IReportGenerator getReport() {
		return ReportTools.getReport(mType);
	}

	private Map<String, String> loadHistory() {
		final Map<String, String> result = new HashMap<String, String>();

		try {
			final IPath location = Activator.getDefault().getStateLocation();
			final File file = location.append(HISTORY_FILE).toFile();
			if (file.exists()) {
				final FileReader reader = new FileReader(file);
				final IMemento memento = XMLMemento.createReadRoot(reader);
				reader.close();

				for (final String key : new String[] { FILELOCATION, TITLE, DESCRIPTION }) {
					final IMemento node = memento.getChild(key);
					if (node != null)
						result.put(key, node.getTextData());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			// could not load history, ignore
		}

		return result;
	}

	private void saveHistory(final Map<String, String> data) {
		final XMLMemento memento = XMLMemento.createWriteRoot("history");
		for (final Entry<String, String> entry : data.entrySet())
			memento.createChild(entry.getKey()).putTextData(entry.getValue());

		try {
			final IPath location = Activator.getDefault().getStateLocation();
			final File file = location.append(HISTORY_FILE).toFile();
			final FileWriter writer = new FileWriter(file);
			memento.save(writer);
			writer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
