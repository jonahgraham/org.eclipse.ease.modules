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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class VariablesDragListener implements DragSourceListener {
	private final TreeViewer fTreeViewer;

	public VariablesDragListener(TreeViewer viewer) {
		fTreeViewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		LocalSelectionTransfer.getTransfer().setSelection(null);
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		LocalSelectionTransfer.getTransfer().setSelection(fTreeViewer.getSelection());
	}
}