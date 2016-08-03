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

import static com.google.common.collect.Iterators.filter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class TypeMatcher implements IMatcher {

	@Override
	public Collection<EObject> getElements(final String string, IEditingDomainProvider currentEditor) throws MatcherException {
		Iterator<EObject> filter = Iterators.emptyIterator();
		for (Resource r : currentEditor.getEditingDomain().getResourceSet().getResources()) {
			if (r != null) {
				filter = Iterators.concat(filter, filter(r.getAllContents(), new Predicate<EObject>() {
					@Override
					public boolean apply(EObject input) {
						List<EClass> allClasses = new LinkedList<EClass>(input.eClass().getEAllSuperTypes());
						allClasses.add(input.eClass());
						for (EClass e : allClasses) {
							if (e.getName().equalsIgnoreCase(string)) {
								return true;
							}
						}
						return false;
					}
				}));
			}
		}
		return Lists.newArrayList(filter);
	}

	@Override
	public String getText() {
		return Messages.TypeMatcher_COMBO_TEXT_TYPE;
	}

	@Override
	public String getHelp() {
		return Messages.TypeMatcher_HELP_TYPE;
	}

}
