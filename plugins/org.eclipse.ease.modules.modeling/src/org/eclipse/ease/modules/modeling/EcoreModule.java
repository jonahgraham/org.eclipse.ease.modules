/*******************************************************************************
 * Copyright (c) 2008 AIRBUS FRANCE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Charles David (Obeo) - initial API and implementation
 *    Vincent Hemery (Atos Origin) - removing modeler dependencies
 *    Arthur Daussy (Atos) - Update to be use in EASE
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.modeling.selector.GMFSemanticSeletor;
import org.eclipse.ease.modules.modeling.ui.UriSelectionDialog;
import org.eclipse.ease.modules.platform.ResourcesModule;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Stereotype;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * This offer to user means to handle EMF models (Create EObject, Select element, Handle resources etc..). This module need to be initialized with an nsURI
 * referencing a metamodel usin {@link EcoreModule#initEPackage(String)}.Once this module has been initialized all creation method of the factory is injected in
 * the script.
 *
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 * @author <a href="mailto:laurent.redor@obeo.fr">Laurent Redor</a>
 * @author <a href="mailto:arthur.daussy@atos.net">Laurent Redor</a>
 */
public class EcoreModule extends AbstractScriptModule {

	private String fUri;

	/**
	 * Returns the currently selected model element, either in the editor or the outline view. If several elements are selected, only the first is returned.
	 *
	 * @return the currently selected model element.
	 */
	public EObject getSelection() {
		final Object selection = getSelectionModule().getCustomSelectionFromSelector(GMFSemanticSeletor.SELECTOR_ID);
		if (selection instanceof EObject) {
			if (selection instanceof Element) {
				final EList<Stereotype> appliedStereotypes = ((Element) selection).getAppliedStereotypes();
			}
			return (EObject) selection;
		} else {
			final String message = "Unable to retreive a EObject from the selection";
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", message);
			return null;
		}
	}

	private SelectionModule getSelectionModule() {
		return getEnvironment().getModule(SelectionModule.class);
	}

	/**
	 * Return if the current instance is a instance of an EClass define by its name.
	 *
	 * @param eObject
	 *            The {@link EObject} you want to test.
	 * @param typeName
	 *            The name of the EClass defined in the metamodel
	 * @return true if the {@link EObject} is instance of typeName
	 */
	@WrapToScript
	public boolean eInstanceOf(@ScriptParameter(name = "eObject") final EObject eObject, @ScriptParameter(name = "type") final String type) {
		final EClassifier classifier = getEPackage().getEClassifier(type);
		if (classifier == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to find EClass named :" + type);
		}
		return classifier.isInstance(eObject);
	}

	protected String getUri() {
		return fUri;
	}

	/**
	 * Returns the currently selected model element in the current editor if it is an instance of the named meta-class (or a sub-class).
	 *
	 * @param type
	 *            the name of a meta-class (e.g. "Property" or "Package")
	 * @return the first element selected in the current editor if there is one and it is an instance of the named meta-class or a sub-class of it.
	 */
	@WrapToScript
	public EObject getSelection(@ScriptParameter(defaultValue = ScriptParameter.NULL, name = "type") final String type) {
		final EObject selection = getSelection();
		if (type != null) {
			if (eInstanceOf(selection, type)) {
				return selection;
			} else {
				return null;
			}
		}
		return selection;
	}

	protected String getFactoryVariableName() {
		return "__" + getEPackage().getName().toUpperCase() + "__FACTORY";
	}

	/**
	 * Filter used to match all create method from the factory
	 */
	protected static Predicate<Method> createMethodFilter = new Predicate<Method>() {

		@Override
		public boolean apply(final Method arg0) {
			if (arg0 != null) {
				return arg0.getName().startsWith("create");
			}
			return false;
		}
	};

	/**
	 * Initialized the module with the correct metamodèle. If this method is not called the module will at runtime ask with metamodel shall be used.
	 *
	 * @param nsURI
	 *            of the metamodel
	 */
	@WrapToScript
	public void initEPackage(@ScriptParameter(name = "nsURI") final String nsURI) {
		if (nsURI == null)
			initEPackageFromDialog();
		else
			fUri = nsURI;

		final EPackage ePack = getEPackage();
		if (ePack == null) {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to find metamodel with URI : " + fUri);
			return;
		}

		final EFactory factory = getFactory();
		if (factory != null) {
			getScriptEngine().setVariable(getFactoryVariableName(), factory);
			getEnvironment().wrap(factory.getClass());

		} else {
			getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to find metamodel with URI : " + fUri);
		}
	}

	/**
	 * Create a new resource to hold model elements
	 *
	 * @param name
	 *            Name of the resource (Optional parameter ask dynamically to the user)
	 * @param uri
	 *            URI locating the container of the resource (Optional parameter ask dynamically to the user)
	 * @return
	 */
	@WrapToScript
	public Resource createResource(@ScriptParameter(name = "name", defaultValue = ScriptParameter.NULL) final String name,
			@ScriptParameter(name = "uri", defaultValue = ScriptParameter.NULL) final String uri) {
		ResourceSet resourceSet = getResourceSet();
		if (resourceSet == null) {
			Logger.warning(Activator.PLUGIN_ID, "Unable to get the current resourceSet. Creating a new one...");
			resourceSet = new ResourceSetImpl();
		}
		final URI resourceURI = createURI(uri, name);
		Resource resource = null;
		try {
			resource = resourceSet.getResource(resourceURI, true);
		} catch (final Exception e) {
			resource = resourceSet.createResource(resourceURI);
		}
		return resource;
	}

	/**
	 * Create a new URI. This URI is use to locate a resource.
	 *
	 * @param containerURI
	 *            path of the container of the new resource. (Optional Ask dynamically to the user)
	 * @param fileName
	 *            name of the new resource. (Optional Ask dynamically to the user)
	 * @return
	 */
	@WrapToScript
	public URI createURI(@ScriptParameter(name = "containerURI", defaultValue = ScriptParameter.NULL) final String containerURI,
			@ScriptParameter(name = "fileName", defaultValue = ScriptParameter.NULL) String fileName) {
		URI container = null;
		if (containerURI == null) {
			// Launch dialog to get an URI
			final String location = getEnvironment().getModule(ResourcesModule.class).showFolderSelectionDialog(null,
					"Select where you want to add your resource", null);
			if ((location == null) || (location.isEmpty())) {
				getEnvironment().getModule(UIModule.class).showErrorDialog("Error", "Unable to retreive a container for the new resource from your selestion");
				return null;
			}

			final IPath containerPath = ResourceTools.toPath(location);
			container = URI.createPlatformResourceURI(containerPath.toString(), true);

		} else {
			container = URI.createFileURI(containerURI);
		}
		if (fileName == null) {
			// Launch input dialog
			fileName = getEnvironment().getModule(UIModule.class).showInputDialog("", "Give the resource name (With it's extension)", "");
		}

		container = container.appendSegment(fileName);
		return container;
	}

	/**
	 * Get the factory of selected meta model.
	 *
	 * @return
	 */
	@WrapToScript
	public EFactory getFactory() {
		if (fUri == null) {
			initEPackageFromDialog();
		}
		final EPackage ePackage = getEPackage();
		if (ePackage == null) {
			throw new RuntimeException("Unable to retreive EPackage with URI " + fUri);
		}
		return ePackage.getEFactoryInstance();
	}

	/**
	 * Get the {@link EPackage} of the selected meta model
	 *
	 * @return
	 */
	@WrapToScript
	public EPackage getEPackage() {
		if (fUri == null) {
			initEPackageFromDialog();
		}
		final EPackage ePack = EPackage.Registry.INSTANCE.getEPackage(fUri);
		return ePack;
	}

	private void initEPackageFromDialog() {
		getEnvironment().getModule(UIModule.class);
		final UriSelectionDialog dialog = new UriSelectionDialog(UIModule.getShell());
		final int returnCode = DialogModule.openDialog(dialog);
		if (returnCode == Window.OK) {
			final Object[] result = dialog.getResult();
			if ((result != null) && (result.length == 1)) {
				fUri = (String) result[0];
			}
		}
	}

	/**
	 * Add an error marker on a EObject
	 *
	 * @param eObject
	 *            The Object you want to add a error marker
	 * @param message
	 *            Message of the marker
	 * @throws CoreException
	 */
	@WrapToScript
	public void addErrorMarker(@ScriptParameter(name = "eObject") final EObject eObject, @ScriptParameter(name = "message") final String message)
			throws CoreException {
		EMFMarkerUtil.addMarkerFor(eObject, message, IMarker.SEVERITY_ERROR);
	}

	/**
	 * Add an Information marker on a EObject
	 *
	 * @param eObject
	 *            The Object you want to add a error marker
	 * @param message
	 *            Message of the marker
	 * @throws CoreException
	 */
	@WrapToScript
	public void addInfoMarker(@ScriptParameter(name = "eObject") final EObject eObject, @ScriptParameter(name = "message") final String message)
			throws CoreException {
		EMFMarkerUtil.addMarkerFor(eObject, message, IMarker.SEVERITY_INFO);
	}

	/**
	 * Add a Warning marker on a EObject
	 *
	 * @param eObject
	 *            The Object you want to add a error marker
	 * @param message
	 *            Message of the marker
	 * @throws CoreException
	 */
	@WrapToScript
	public void addWarningMarker(@ScriptParameter(name = "eObject") final EObject eObject, @ScriptParameter(name = "message") final String message)
			throws CoreException {
		EMFMarkerUtil.addMarkerFor(eObject, message, IMarker.SEVERITY_WARNING);
	}

	/**
	 * The current editor part is return or null if there is any active editor. In the case of there is any active editor a message is display to inform the
	 * user.
	 *
	 * @return IEditorPart The current editor part or null
	 */
	protected IEditorPart getCurrentEditorPart() {
		/**
		 * ActiveEditorRef
		 */
		class ActiveEditorRef {

			public IEditorPart activeEditorPart = null;
		}

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final ActiveEditorRef activeEditorRef = new ActiveEditorRef();
		final Display display = workbench.getDisplay();

		display.syncExec(() -> {
			final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

			// this can be null if you close all perspectives
			if ((window != null) && (window.getActivePage() != null) && (window.getActivePage().getActiveEditor() != null))
				activeEditorRef.activeEditorPart = window.getActivePage().getActiveEditor();
		});
		return activeEditorRef.activeEditorPart;
	}

	/**
	 * Save: The current editor if no eObject is passed in argument The resource containing the eObject passed in argument
	 *
	 * @param eObject
	 *            Help to locate the resource to save (Optional save the current editor)
	 */
	@WrapToScript
	public void save(@ScriptParameter(defaultValue = ScriptParameter.NULL, name = "target") final Object target) {
		Resource toSave = null;
		if (target instanceof EObject) {
			final EObject eObject = (EObject) target;
			toSave = eObject.eResource();
		} else if (target instanceof Resource) {
			toSave = (Resource) target;
		}
		if (toSave != null) {
			try {
				toSave.save(null);
			} catch (final IOException e) {
				e.printStackTrace();
				getEnvironment().getModule(UIModule.class).showErrorDialog("Error", e.getMessage());
				return;
			}
		} else {
			save();
		}
	}

	public void save() {
		getCurrentEditorPart().doSave(new NullProgressMonitor());
	}

	protected EditingDomain getEditingDomain() {
		final IEditorPart currentEditorPart = getCurrentEditorPart();
		if (currentEditorPart != null) {
			Object domain = currentEditorPart.getAdapter(EditingDomain.class);
			if (domain instanceof EditingDomain) {
				return (EditingDomain) domain;
			}
			domain = AdapterManager.getDefault().getAdapter(currentEditorPart, EditingDomain.class);
			if (domain instanceof EditingDomain) {
				return (EditingDomain) domain;
			}
			if (currentEditorPart instanceof IEditingDomainProvider) {
				return ((IEditingDomainProvider) currentEditorPart).getEditingDomain();
			}

		} else {
			Logger.warning(Activator.PLUGIN_ID, "Unable to retreive editing domain. There is not opened editor");
		}
		return null;
	}

	/**
	 * Return all object referencing this EObject. The return value is a collection of Array of size 2. Result[0] = EStructual feature linking the two object
	 * Result[1] = The referencing object
	 *
	 * @param eObject
	 * @return
	 */
	@WrapToScript
	public static Collection<Object[]> getUsages(@ScriptParameter(name = "eObject") final EObject eObject) {
		if (eObject == null) {
			return Collections.emptyList();
		}

		ECrossReferenceAdapter crossReferencer = ECrossReferenceAdapter.getCrossReferenceAdapter(eObject);
		if (crossReferencer == null) {
			// try to register a cross referencer at the highest level
			crossReferencer = new ECrossReferenceAdapter();
			if (eObject.eResource() != null) {
				if (eObject.eResource().getResourceSet() != null) {
					crossReferencer.setTarget(eObject.eResource().getResourceSet());
				} else {
					crossReferencer.setTarget(eObject.eResource());
				}
			} else {
				crossReferencer.setTarget(eObject);
			}
		}

		final Collection<Setting> result = crossReferencer.getInverseReferences(eObject, true);
		return Collections2.transform(result, new Function<Setting, Object[]>() {

			@Override
			public Object[] apply(final Setting arg0) {
				final Object[] setting = new Object[2];
				setting[1] = arg0.getEStructuralFeature();
				setting[0] = arg0.getEObject();

				return setting;
			}
		});
	}

	protected ResourceSet getResourceSet() {
		final EditingDomain editingDomain = getEditingDomain();
		if (editingDomain != null) {
			return editingDomain.getResourceSet();
		}
		return null;
	}

	public void runOperation(@ScriptParameter(name = "operation") final Runnable operation) {
		runOperation(operation, "Script Operation");
	}

	/**
	 * Run an operation in the current editor's command stack This is really help ful to manipulate a model using transaction
	 *
	 * @param operation
	 *            the operation to run
	 * @param operationName
	 *            the name to give to the operation execution
	 */
	@WrapToScript
	public void runOperation(@ScriptParameter(name = "operation") final Runnable operation,
			@ScriptParameter(name = "name", defaultValue = "Script Operation") final String operationName) {
		final EditingDomain domain = getEditingDomain();

		if (domain instanceof TransactionalEditingDomain) {
			((TransactionalEditingDomain) domain).getCommandStack().execute(
					new GMFtoEMFCommandWrapper(new RunnableTransactionalCommandWrapper((TransactionalEditingDomain) domain, operationName, null, operation)));
			// ((EditingDomain)domain).getCommandStack().undo();
		} else if (domain != null) {
			// execute the operation in a command
			final ChangeRecorder recorder = new ChangeRecorder();
			domain.getCommandStack().execute(new RunnableCommandWrapper(operation, domain.getResourceSet(), recorder));
			recorder.dispose();
		} else {
			// try simply running the operation
			operation.run();
		}
	}

	protected static class RunnableCommandWrapper extends ChangeCommand {

		private final Runnable operation;

		public RunnableCommandWrapper(final Runnable operation, final ResourceSet set, final ChangeRecorder recorder) {
			super(recorder, set);
			this.operation = operation;
		}

		@Override
		protected void doExecute() {
			operation.run();

		}
	}

	/**
	 * Display a dialog which ask the user to select between a list of Object
	 *
	 * @param inputs
	 *            List of choice for the user
	 * @return The selected object
	 */
	@WrapToScript
	public Object[] selectFromList(final List<? extends Object> inputs) {
		return DialogModule.selectFromList(inputs.toArray(), new LabelProvider() {

			@Override
			public String getText(final Object element) {
				if (element instanceof EObject) {
					return getLabelProvider((EObject) element).getText(element);
				}
				return element.toString();
			}
		});
	}

	private static ComposedAdapterFactory adapter = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

	/**
	 * Print an {@link EObject} using label providers
	 *
	 * @param target
	 * @return
	 */
	@WrapToScript
	public String ePrint(final EObject target) {
		final IItemLabelProvider labelProvider = getLabelProvider(target);
		if (labelProvider != null) {
			return labelProvider.getText(target);
		}
		return "[ERRO] Unable to print this EObject";
	}

	private IItemLabelProvider getLabelProvider(final EObject target) {
		final IItemLabelProvider labelProvider = (IItemLabelProvider) adapter.adapt(target, IItemLabelProvider.class);
		return labelProvider;
	}

	protected static class RunnableTransactionalCommandWrapper extends AbstractTransactionalCommand {

		public RunnableTransactionalCommandWrapper(final TransactionalEditingDomain domain, final String label, final List affectedFiles,
				final Runnable operation) {
			super(domain, label, affectedFiles);
			this.operation = operation;
		}

		private final Runnable operation;

		@Override
		protected CommandResult doExecuteWithResult(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
			operation.run();
			return CommandResult.newOKCommandResult();
		}
	}

	public ICodeFactory getCodeFactory() {
		return ScriptService.getCodeFactory(getScriptEngine());
	}
}
