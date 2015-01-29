/*******************************************************************************
 * Copyright (c) 2015 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Guillaume Renier - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml.blocks.Block;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This module help to handle UML models.
 */
public class SYSMLModule extends UMLModule {

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
	public org.eclipse.papyrus.sysml.blocks.Block createBlock() {
		return (Block) createSysML("SysML::Blocks::Block");
	}

	/**
	 * Get the UML model from the current active editor
	 *
	 * @return
	 */
	@WrapToScript
	public EObject createSysML(final String qualifiedName) {
		Class clazz = ((UMLFactory) getFactory()).createClass();
		EList<Stereotype> stereotypes = clazz.getApplicableStereotypes();
		for (Stereotype s : stereotypes) {
			if (s.getQualifiedName().equals(qualifiedName)) {
				EObject sysml = clazz.applyStereotype(s);
				return sysml;
			}
		}
		return null;
	}
}
