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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.ICreationCommand;
import org.eclipse.papyrus.commands.OpenDiagramCommand;
import org.eclipse.papyrus.infra.core.extension.commands.ICreationCondition;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.gmfdiag.navigation.CreatedNavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.ExistingNavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.NavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.NavigationHelper;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeManager;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeRequest;
import org.eclipse.papyrus.infra.services.controlmode.IControlModeManager;
import org.eclipse.papyrus.uml.diagram.clazz.ClassDiagramCreationCondition;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.uml2.uml.Element;

/**
 * Module used to interact with Papyrus Editor.
 *
 * @author adaussy
 *
 */
public class PapyrusModule extends UMLModule {

	private final NotationModule notationModule = new NotationModule();

	/**
	 * Return the model set (ResourceSet) of the current model open in Papyrus
	 *
	 * @return
	 */
	@WrapToScript
	public ModelSet getModelSet() {
		EditingDomain editingDomain = TransactionUtil.getEditingDomain(getModel());
		if (editingDomain == null) {
			Logger.logError("Unable to get the editing domain");
			return null;
		}
		ResourceSet resourceSet = editingDomain.getResourceSet();
		if (resourceSet instanceof ModelSet) {
			return (ModelSet) resourceSet;

		}
		Logger.logError("The resource set is not a model set");
		return null;
	}

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);
		notationModule.initialize(engine, environment);
	}

	/**
	 * Return the select view element (Notation metamodel)
	 *
	 * @return
	 */
	@WrapToScript
	public View getSelectionView() {
		EObject v = notationModule.getSelection();
		if (v instanceof View) {
			return (View) v;
		}
		return null;
	}

	/**
	 * Return the UML element from the selection
	 *
	 * @return
	 */
	@WrapToScript
	public Element getSelectionElement() {
		EObject elem = getSelection();
		if (elem instanceof Element) {
			return (Element) elem;
		}
		return null;
	}

	/**
	 * Create a new empty diagram WARNING: For now only Package and class diagram are implemented.
	 *
	 * @param semanticElement
	 *            UML or Sysml element of the diagram
	 * @param newDiagram
	 *            The name of the diagram (Optional set the name to newDiagram)
	 * @param open
	 *            True if the diagram shall be open (Optional default = false)
	 */
	@WrapToScript
	public void createDiagram(final EObject semanticElement, @ScriptParameter(name = "diagramType") final String diagramType,
			@ScriptParameter(name = "diagramName", defaultValue = "NewDiagram") final String newDiagram, @ScriptParameter(name = "open") final boolean open) {
		if ("Class".equals(diagramType)) {
			createDiagram(getModelSet(), new CreateClassDiagramCommand(), new ClassDiagramCreationCondition(), semanticElement, newDiagram, open);
		}
	}

	/**
	 * Use the control function of papyrus. That is to say that all contained element diagrams will be stored in a different resource.
	 *
	 * @param semanticElement
	 *            The semantic element to control (That is to say an UML element)
	 * @param fileName
	 *            The name of the new file
	 */
	@WrapToScript
	public void control(final EObject semanticElement, @ScriptParameter(name = "fileName") String fileName) {
		if (fileName == null) {
			fileName = semanticElement.eResource().getURIFragment(semanticElement);
		}
		URI baseURI = semanticElement.eResource().getURI();
		URI createURI = baseURI.trimSegments(1).appendSegment(fileName + ".uml");
		ControlModeRequest controlRequest = ControlModeRequest.createUIControlModelRequest(getEditingDomain(), semanticElement, createURI);
		controlRequest.setIsUIAction(false);
		IControlModeManager controlMng = ControlModeManager.getInstance();
		ICommand controlCommand = controlMng.getControlCommand(controlRequest);
		getEditingDomain().getCommandStack().execute(new GMFtoEMFCommandWrapper(controlCommand));
	}

	private void createDiagram(final ModelSet modelSet, final ICreationCommand creationCommand, final ICreationCondition creationCondition,
			final EObject target, final String diagramName, final boolean openDiagram) {
		NavigableElement navElement = getNavigableElementWhereToCreateDiagram(creationCondition, target);
		if ((navElement != null) && (modelSet != null)) {
			CompositeCommand command = getLinkCreateAndOpenNavigableDiagramCommand(navElement, creationCommand, diagramName, modelSet, openDiagram);
			// modelSet.getTransactionalEditingDomain().getCommandStack().execute(new GMFtoEMFCommandWrapper(command));
			try {
				command.execute(new NullProgressMonitor(), null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
	}

	private NavigableElement getNavigableElementWhereToCreateDiagram(final ICreationCondition creationCondition, final EObject selectedElement) {

		if (selectedElement != null) {
			// First check if the current element can host the requested diagram
			if (creationCondition.create(selectedElement)) {
				return new ExistingNavigableElement(selectedElement, null);
			} else {
				List<NavigableElement> navElements = NavigationHelper.getInstance().getAllNavigableElements(selectedElement);
				// this will sort elements by navigation depth
				Collections.sort(navElements);

				for (NavigableElement navElement : navElements) {
					// ignore existing elements because we want a hierarchy to
					// be created if it is not on the current element
					if ((navElement instanceof CreatedNavigableElement) && creationCondition.create(navElement.getElement())) {
						return navElement;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected TransactionalEditingDomain getEditingDomain() {
		return (TransactionalEditingDomain) super.getEditingDomain();
	}

	public static CompositeCommand getLinkCreateAndOpenNavigableDiagramCommand(final NavigableElement navElement,
			final ICreationCommand creationCommandInterface, final String diagramName, final ModelSet modelSet, final boolean openDiagram) {
		CompositeCommand compositeCommand = new CompositeCommand("Create diagram");

		if (navElement instanceof CreatedNavigableElement) {
			compositeCommand.add(new AbstractTransactionalCommand(modelSet.getTransactionalEditingDomain(), "Create hierarchy", null) {

				@Override
				protected CommandResult doExecuteWithResult(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
					NavigationHelper.linkToModel((CreatedNavigableElement) navElement);
					NavigationHelper.setBaseName((CreatedNavigableElement) navElement, "");
					return CommandResult.newOKCommandResult();
				}
			});
		}

		ICommand createDiagCommand = creationCommandInterface.getCreateDiagramCommand(modelSet, navElement.getElement(), diagramName);
		compositeCommand.add(createDiagCommand);
		if (openDiagram) {
			compositeCommand.add(new OpenDiagramCommand(modelSet.getTransactionalEditingDomain(), createDiagCommand));
		}

		return compositeCommand;
	}

	/**
	 * The same as eInstanceOf of the Ecore model. However it will look into UML and Notation metamodel
	 */
	@WrapToScript
	@Override
	public boolean eInstanceOf(@ScriptParameter(name = "eObject") final EObject eObject, @ScriptParameter(name = "type") final String type) {
		EClassifier classifier = getEPackage().getEClassifier(type);
		if (classifier == null) {
			classifier = notationModule.getEPackage().getEClassifier(type);
		}
		return classifier.isInstance(eObject);
	}

	/**
	 * The the current Papyrus editor. The object parameter is useless
	 */
	@Override
	@WrapToScript
	public void save(@ScriptParameter(name = "object", defaultValue = ScriptParameter.NULL) final Object object) {
		save();
	}
}
