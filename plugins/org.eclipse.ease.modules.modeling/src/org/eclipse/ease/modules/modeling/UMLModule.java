/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This module help to handle UML models
 *
 * @author adaussy
 *
 */
public class UMLModule extends EcoreModule {

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);
		initEPackage(UMLPackage.eNS_URI);
	}

	/**
	 * Get the UML model from the current active editor
	 *
	 * @return
	 */
	@WrapToScript
	public Model getModel() {
		EditingDomain editingDomain = getEditingDomain();
		if (editingDomain == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to retreive editing domain");
		}
		ResourceSet resourceSet = editingDomain.getResourceSet();
		if (resourceSet == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to retreive the resource set");
		}
		for (Resource r : resourceSet.getResources()) {
			Model result = lookForModel(r);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Model lookForModel(final Resource r) {
		URI resourceURI = r.getURI();
		if (resourceURI != null) {
			if (UMLPackage.eNS_PREFIX.equals(resourceURI.fileExtension())) {
				EList<EObject> content = r.getContents();
				if (!content.isEmpty()) {
					EObject root = content.get(0);
					if (root instanceof Model) {
						return (Model) root;
					}
				}
			}
		}
		return null;
	}
}
