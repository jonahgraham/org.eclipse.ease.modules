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

package org.eclipse.ease.modules.unittest.ui.editor;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

public class VariablesDropSupport extends ViewerDropAdapter {

	private final TestSuiteModel fModel;

	protected VariablesDropSupport(TreeViewer viewer, TestSuiteModel model) {
		super(viewer);
		fModel = model;
	}

	@Override
	public boolean performDrop(Object data) {
		final Object target = getCurrentTarget();
		IPath targetNode = null;

		if (target == null)
			targetNode = Path.ROOT;

		if (target instanceof Variable)
			return false;
		else if (target instanceof IPath)
			targetNode = (IPath) target;

		final AbstractTreeViewer viewer = (AbstractTreeViewer) super.getViewer();
		final IStructuredSelection selection = (IStructuredSelection) data;
		final VariablesTreeContentProvider treeContentProvider = (VariablesTreeContentProvider) viewer.getContentProvider();
		for (final Object element : selection.toArray()) {
			if (element instanceof Variable) {
				final Variable currentVariable = (Variable) element;
				currentVariable.setPath(targetNode);
			} else if (element instanceof IPath) {
				final IPath node = (IPath) element;
				final List<Variable> variables = fModel.getVariables();
				targetNode = targetNode.append(node.lastSegment());
				VariablesTreeContentProvider.updatePathGroup(variables, targetNode, node);
				treeContentProvider.moveSelectedPath(targetNode.makeRelative(), node);
			}
		}
		final Object[] elements = viewer.getExpandedElements();
		viewer.refresh();
		viewer.setExpandedElements(elements);
		return true;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return LocalSelectionTransfer.getTransfer().isSupportedType(transferType);
	}
}