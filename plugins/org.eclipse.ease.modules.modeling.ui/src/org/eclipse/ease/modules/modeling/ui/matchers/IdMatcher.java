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
package org.eclipse.ease.modules.modeling.ui.matchers;

import java.util.Collection;

import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.ease.modules.modeling.ui.utils.SelectionUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

import com.google.common.collect.Lists;

public class IdMatcher implements IMatcher {

	@Override
	public Collection<EObject> getElements(String string, IEditingDomainProvider currentEditor) throws MatcherException {
		EObject root = SelectionUtils.getSelection(currentEditor);

		Resource r = root.eResource();
		if (r != null) {
			EObject e = r.getEObject(string);
			return Lists.newArrayList(e);
		}
		return Lists.newArrayList();
	}

	@Override
	public String getText() {
		return Messages.IdMatcher_COMBO_TEXT_ID;
	}

	@Override
	public String getHelp() {
		return Messages.IdMatcher_HELP_ID;
	}

}
