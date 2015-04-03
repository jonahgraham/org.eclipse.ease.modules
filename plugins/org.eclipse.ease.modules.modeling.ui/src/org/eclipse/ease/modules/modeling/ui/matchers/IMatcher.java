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

import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

public interface IMatcher {
	Collection<EObject> getElements(String string,
			IEditingDomainProvider currentEditor) throws MatcherException;

	String getText();

	String getHelp();
}
