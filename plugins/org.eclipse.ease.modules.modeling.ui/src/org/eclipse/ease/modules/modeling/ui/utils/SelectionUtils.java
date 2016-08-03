/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui.utils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

public class SelectionUtils {

	/**
	 * Returns the first resource found with the given extension in the resource set of the domain provider
	 * 
	 * @param domainProvider
	 * @param extension
	 *            can be null in this case the first resource is returned
	 * @return
	 * @throws MatcherException
	 */
	public static EObject getSelection(IEditingDomainProvider currentEditor) throws MatcherException {
		ISelection selection = null;
		if (currentEditor instanceof ISelectionProvider) {
			ISelectionProvider sp = (ISelectionProvider) currentEditor;
			selection = sp.getSelection();
		} else {
			selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getSelectionProvider().getSelection();
		}
		EObject eobject = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof EObject) {
				eobject = (EObject) element;
			}
			if (eobject == null) {
				if (element instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) element;
					eobject = (EObject) adaptable.getAdapter(EObject.class);
				}
			}
			if (eobject == null) {
				eobject = (EObject) Platform.getAdapterManager().getAdapter(element, EObject.class);
			}
		}
		if (eobject == null) {
			throw new MatcherException(Messages.SelectionUtils_NO_SELECTION_FOUND);
		}
		return eobject;
	}
}
