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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

public class TestSuiteEditor extends FormEditor {

	public static final String EDITOR_ID = "org.eclipse.ease.editor.suiteEditor";

	private StructuredTextEditor fSourceEditor;
	private int fSourceEditorIndex;

	/** Keeps track of dirty code from source editor. */
	private boolean fSourceDirty = false;

	private TestSuiteModel fModel;
	private int fCurrentPage = -1;
	private boolean fDirty = false;

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);

		try {
			fModel = new TestSuiteModel(((FileEditorInput) input).getFile());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		firePropertyChange(PROP_TITLE);
	}

	@Override
	protected void addPages() {

		fSourceEditor = new StructuredTextEditor();
		fSourceEditor.setEditorPart(this);

		try {
			addPage(new Components(this, "some ID", "Components"));
			addPage(new Description(this, "some id4", "Description"));
			addPage(new Variables(this, "some ID2", "Variables"));
			addPage(new SetupTeardown(this, "some id3", "Setup"));

			// add source page
			fSourceEditorIndex = addPage(fSourceEditor, getEditorInput());
			setPageText(fSourceEditorIndex, "Source");
		} catch (final PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// add listener for changes of the document source
		getDocument().addDocumentListener(new IDocumentListener() {

			@Override
			public void documentAboutToBeChanged(final DocumentEvent event) {
				// nothing to do
			}

			@Override
			public void documentChanged(final DocumentEvent event) {
				// TODO we need to do something to update the editor
				fSourceDirty = true;
			}
		});
	}

	@Override
	public void doSaveAs() {
		// not allowed
		if (getActivePage() != fSourceEditorIndex)
			updateSourceFromModel();

		fSourceEditor.doSaveAs();

		// make sure sourceEditor and forms editor use the same input
		setInput(fSourceEditor.getEditorInput());
		editorDirtyStateChanged();
		fDirty = false;

		// re-initialize by loading the new model
		try {
			fModel = new TestSuiteModel(((FileEditorInput) getEditorInput()).getFile());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update editor title
		firePropertyChange(PROP_TITLE);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		if (getActivePage() != fSourceEditorIndex)
			updateSourceFromModel();

		fSourceEditor.doSave(monitor);
		fDirty = false;
		editorDirtyStateChanged();
	}

	@Override
	protected void pageChange(final int newPageIndex) {
		// check for update from the source code
		if ((fCurrentPage == fSourceEditorIndex) && (fSourceDirty))
			updateModelFromSource();

		// check for updates to be propagated to the source code
		if ((newPageIndex == fSourceEditorIndex) && (fDirty))
			// update source code for source viewer using new model data
			updateSourceFromModel();

		// switch page
		super.pageChange(newPageIndex);

		// update page if needed
		final IFormPage page = getActivePageInstance();
		if (page != null)
			page.setFocus();

		fCurrentPage = getActivePage();
	}

	private void updateModelFromSource() {
		// TODO update model from source page before switching to a forms page

		fModel.load(getDocument().get());
		fSourceDirty = false;
	}

	private void updateSourceFromModel() {
		getDocument().set(fModel.toMemento().toString());

		fDirty = false;
	}

	private IDocument getDocument() {
		final IDocumentProvider provider = fSourceEditor.getDocumentProvider();
		return provider.getDocument(getEditorInput());
	}

	public TestSuiteModel getModel() {
		return fModel;
	}

	public void setDirty() {
		fDirty = true;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() | fDirty;
	}

	@Override
	public String getTitle() {
		if (getModel() != null)
			return getModel().getFile().getName();

		return super.getTitle();
	}
}
