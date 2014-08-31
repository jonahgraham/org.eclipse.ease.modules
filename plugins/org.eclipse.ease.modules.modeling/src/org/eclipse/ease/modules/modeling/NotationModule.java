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
import org.eclipse.ease.modules.modeling.selector.GMFNotationSelector;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.NotationPackage;

/**
 * This module help to handle Notation models This extends {@link EcoreModule}
 * 
 * @author adaussy
 * 
 */
public class NotationModule extends EcoreModule {

	public NotationModule() {
		super();

	}

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);
		initEPackage(NotationPackage.eNS_URI);
	}

	@Override
	public EObject getSelection() {
		Object selection = selectionModule.getCustomSelectionFromSelector(GMFNotationSelector.SELECTOR_ID);
		if (selection instanceof EObject) {
			return (EObject) selection;
		} else {
			String message = "Unable to retreive a EObject from the selection";
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", message);
			return null;
		}
	}
}
