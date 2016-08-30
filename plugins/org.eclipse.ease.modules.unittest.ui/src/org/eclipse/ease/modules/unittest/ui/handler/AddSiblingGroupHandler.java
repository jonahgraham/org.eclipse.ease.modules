/*******************************************************************************
 * Copyright (c) 2016 Madalina Hodorog and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Madalina Hodorog - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.ease.modules.unittest.ui.editor.TestSuiteEditor;
import org.eclipse.ease.modules.unittest.ui.editor.Variables;
import org.eclipse.ease.modules.unittest.ui.editor.VariablesTreeContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddSiblingGroupHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IEditorPart editorWindow = HandlerUtil.getActiveEditor(event);
		if (editorWindow instanceof TestSuiteEditor) {
			final TestSuiteEditor testSuiteEditorWindow = (TestSuiteEditor) editorWindow;
			if (testSuiteEditorWindow.getSelectedPage() instanceof Variables) {
				final Variables variablesEditorWindow = (Variables) testSuiteEditorWindow.getSelectedPage();
				final VariablesTreeContentProvider treeContentProvider = (VariablesTreeContentProvider) variablesEditorWindow.getTreeViewer()
						.getContentProvider();
				final IStructuredSelection currentSelection = (IStructuredSelection) variablesEditorWindow.getTreeViewer().getSelection();
				if (currentSelection.size() == 1) {
					final Object element = currentSelection.getFirstElement();
					if (element instanceof IPath) {
						final IPath parentNode = (IPath) treeContentProvider.getParent(element);
						variablesEditorWindow.addGroup(parentNode);
					} else if (element instanceof Variable) {
						variablesEditorWindow.addGroup(((Variable) element).getPath().makeRelative());
					}
				}
			}
		}
		return null;
	}
}