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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.examples.domain.elements.DomainType;
import org.eclipse.ocl.examples.domain.evaluation.DomainModelManager;
import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
import org.eclipse.ocl.examples.pivot.ParserException;
import org.eclipse.ocl.examples.pivot.PivotPackage;
import org.eclipse.ocl.examples.pivot.Type;
import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;

public class ModelExtentMap implements DomainModelManager {

	private final Map<DomainType, Set<EObject>> modelManager = new HashMap<DomainType, Set<EObject>>();

	private final MetaModelManager metaModelManager;

	private boolean generatedErrorMessage = false;

	private final Collection<EObject> roots = new HashSet<EObject>();

	/**
	 * Initializes me with the context element of an OCL expression evaluation.
	 * I discover the scope of the model from this element.
	 * 
	 * @param context
	 *            my context element
	 */

	public ModelExtentMap(MetaModelManager metamodelmanager, EObject context) {
		this.metaModelManager = metamodelmanager;
		EObject container = EcoreUtil.getRootContainer(context);
		roots.add(container);
	}

	/**
	 * Lazily computes the extent of the specified class <code>key</code>.
	 * 
	 * @param type
	 *            a class in the model
	 */
	public Set<EObject> get(DomainType type) {
		Set<EObject> result = modelManager.get(type);
		if (result == null) {
			synchronized (modelManager) {
				result = modelManager.get(type);
				if (result == null) {
					result = new HashSet<EObject>();
					modelManager.put(type, result);
					for (Iterator<EObject> iter = EcoreUtil
							.getAllContents(roots); iter.hasNext();) {
						EObject next = iter.next();
						if ((next != null) && isInstance(type, next)) {
							result.add(next);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Implemented by subclasses to determine whether the specified element is
	 * an instance of the specified class, according to the metamodel semantics
	 * implemented by the environment that created this extent map.
	 * 
	 * @param type
	 *            a class in the model
	 * @param element
	 *            a potential run-time (M0) instance of that class
	 * @return <code>true</code> if this element is an instance of the given
	 *         class; <code>false</code> otherwise
	 */
	protected boolean isInstance(DomainType requiredType, EObject eObject) {
		EClass eClass = eObject.eClass();
		EPackage ePackage = eClass.getEPackage();
		Type objectType = null;
		if (ePackage == PivotPackage.eINSTANCE) {
			String name = DomainUtil.nonNullEMF(eClass.getName());
			objectType = metaModelManager.getPivotType(name);
		} else {
			try {
				objectType = metaModelManager.getPivotOf(Type.class, eClass);
			} catch (ParserException e) {
				if (!generatedErrorMessage) {
					generatedErrorMessage = true;
				}
			}
		}
		return (objectType != null)
				&& objectType.conformsTo(metaModelManager, requiredType);
	}

	@Override
	public String toString() {
		return modelManager.toString();
	}
}
