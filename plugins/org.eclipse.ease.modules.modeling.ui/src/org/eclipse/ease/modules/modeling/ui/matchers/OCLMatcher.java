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
import java.util.Collections;

import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.ease.modules.modeling.ui.utils.ModelExtentMap;
import org.eclipse.ease.modules.modeling.ui.utils.SelectionUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ocl.examples.domain.values.SetValue;
import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
import org.eclipse.ocl.examples.pivot.OCL;
import org.eclipse.ocl.examples.pivot.ParserException;
import org.eclipse.ocl.examples.pivot.helper.OCLHelper;
import org.eclipse.ocl.examples.pivot.utilities.PivotEnvironmentFactory;

public class OCLMatcher implements IMatcher {

	@Override
	public Collection<EObject> getElements(String oclString,
			IEditingDomainProvider currentEditor) throws MatcherException {

		EPackage.Registry registry = EPackage.Registry.INSTANCE;
		EObject root = SelectionUtils.getSelection(currentEditor);
		PivotEnvironmentFactory environmentFactory = new PivotEnvironmentFactory(
				registry, null);

		OCL ocl = OCL.newInstance(environmentFactory);
		ocl.setModelManager(new ModelExtentMap(ocl.getMetaModelManager(), root));
		OCLHelper helper = ocl.createOCLHelper(root.eClass());

		try {
			ExpressionInOCL query = helper.createQuery(oclString);
			Object queryResult = ocl.evaluate(root, query);
			if (queryResult instanceof SetValue) {
				return (Collection<EObject>) ((SetValue) queryResult)
						.asCollection();
			}
		} catch (ParserException e) {
			e.printStackTrace();
			throw new MatcherException(Messages.OCLMatcher_CONSTRAINT_INVALID);
		}

		return Collections.emptyList();
	}

	@Override
	public String getText() {
		return Messages.OCLMatcher_COMBO_TEXT_OCL;
	}

	@Override
	public String getHelp() {
		return Messages.OCLMatcher_HELP_OCL;
	}

}
