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
package org.eclipse.ease.modules.modeling.ui.views;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ease.modules.modeling.ui.Activator;
import org.eclipse.ease.modules.modeling.ui.MatcherRegistry;
import org.eclipse.ease.modules.modeling.ui.Messages;
import org.eclipse.ease.modules.modeling.ui.exceptions.MatcherException;
import org.eclipse.ease.modules.modeling.ui.matchers.IMatcher;
import org.eclipse.ease.modules.modeling.ui.utils.SelectionUtils;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.Lists;

public class ModelRefactoringView extends ViewPart implements ISelectionListener {

	/**
	 * 
	 */
	private static final String ICONS_START_TASK_1_GIF = "icons/start_task-1.gif";
	/**
	 * 
	 */
	private static final String ICONS_DELETE_OBJ_GIF = "icons/delete_obj.gif";
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.ease.modules.modeling.ui.view"; //$NON-NLS-1$
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	List<StructuredViewer> viewers = new LinkedList<StructuredViewer>();
	ComposedAdapterFactory factory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
	private Text text;
	private Form frmNavigation;
	private Text selectionId;
	private Table table_Search;
	private TableViewer tableViewer_Search;
	private ComboViewer comboViewer;

	/**
	 * The constructor.
	 */
	public ModelRefactoringView() {
	}

	@Override
	public void dispose() {
		ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		service.removeSelectionListener(this);
		super.dispose();
	}

	protected boolean gotoInEditor(IEditingDomainProvider editor, EObject e) throws MatcherException {
		if (editor == null) {
			return false;
		}
		if (e != null) {
			if (editor instanceof IViewerProvider) {
				IViewerProvider provider = (IViewerProvider) editor;
				provider.getViewer().setSelection(new TreeSelection(getTreePath(e)), true);
				return true;
			} else if (editor instanceof IGotoMarker) {
				IGotoMarker gotoMarker = (IGotoMarker) editor;
				EObject root = SelectionUtils.getSelection(editor);
				Resource r = root.eResource();
				IFile f = WorkspaceSynchronizer.getFile(r);
				IMarker marker = null;
				try {
					marker = f.createMarker(EValidator.MARKER);
					marker.setAttribute(EValidator.URI_ATTRIBUTE, EcoreUtil.getURI(e).toString());
					marker.setAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
					gotoMarker.gotoMarker(marker);
				} catch (CoreException e1) {
					e1.printStackTrace();
				} finally {
					if (marker != null) {
						try {
							marker.delete();
						} catch (CoreException e1) {
						}
					}
				}

			}
		}
		return false;
	}

	private TreePath getTreePath(EObject e) {
		LinkedList<Object> result = new LinkedList<Object>();
		while (e != null) {
			result.addFirst(e);
			e = e.eContainer();
		}
		return new TreePath(result.toArray());
	}

	protected IEditingDomainProvider getCurrentEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				if (activePage.getActiveEditor() instanceof IEditingDomainProvider) {
					IEditingDomainProvider editor = (IEditingDomainProvider) activePage.getActiveEditor();
					return editor;
				}
			}
		}
		return null;
	}

	protected void handleGoto() {
		String theText = text.getText();
		frmNavigation.setMessage(null);
		ISelection selec = comboViewer.getSelection();
		if (selec instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selec;
			if (structured.getFirstElement() instanceof IMatcher) {
				IMatcher matcher = (IMatcher) structured.getFirstElement();
				Collection<EObject> toUse;
				try {
					toUse = matcher.getElements(theText, getCurrentEditor());
					toUse.remove(null);
					if (toUse.isEmpty()) {
						if (frmNavigation.getMessage() == null) {
							frmNavigation.setMessage(Messages.ModelRefactoringView_NO_ELEMENTS, IMessageProvider.WARNING);
						}
					}
					tableViewer_Search.setInput(toUse);
					frmNavigation.setMessage(toUse.size() + Messages.ModelRefactoringView_NB_ELEMENTS_FOUND, IMessageProvider.INFORMATION);
				} catch (MatcherException e) {
					frmNavigation.setMessage(e.getMessage(), IMessageProvider.ERROR);
					e.printStackTrace();
				}

			}
		}

	}

	private Image getFromRegistry(String key, ImageDescriptor desc) {
		Image i = Activator.getDefault().getImageRegistry().get(key);
		if (i == null) {
			Activator.getDefault().getImageRegistry().put(key, desc);
			i = Activator.getDefault().getImageRegistry().get(key);
		}
		return i;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		service.addSelectionListener(this);
		frmNavigation = formToolkit.createForm(parent);
		formToolkit.paintBordersFor(frmNavigation);
		frmNavigation.setText(Messages.ModelRefactoringView_NAVIGATION);
		frmNavigation.getBody().setLayout(new GridLayout(1, false));

		TabFolder tabFolder = new TabFolder(frmNavigation.getBody(), SWT.NONE);
		tabFolder.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		formToolkit.adapt(tabFolder);
		formToolkit.paintBordersFor(tabFolder);

		TabItem tbtmSearch = new TabItem(tabFolder, SWT.NONE);
		tbtmSearch.setText(Messages.ModelRefactoringView_SEARCH);

		Composite composite_2 = formToolkit.createComposite(tabFolder, SWT.NONE);
		tbtmSearch.setControl(composite_2);
		formToolkit.paintBordersFor(composite_2);
		composite_2.setLayout(new GridLayout(4, false));

		Label lblSelectionId = new Label(composite_2, SWT.NONE);
		lblSelectionId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		formToolkit.adapt(lblSelectionId, true, true);
		lblSelectionId.setText(Messages.ModelRefactoringView_SELECTION_ID);

		selectionId = new Text(composite_2, SWT.BORDER);
		selectionId.setEditable(false);
		selectionId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		formToolkit.adapt(selectionId, true, true);

		comboViewer = new ComboViewer(composite_2, SWT.READ_ONLY);
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IMatcher) element).getText();
			}
		});
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selec = (IStructuredSelection) event.getSelection();
					if (selec.getFirstElement() instanceof IMatcher) {
						final IMatcher matcher = (IMatcher) selec.getFirstElement();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								text.setToolTipText(matcher.getHelp());
							}
						});
					}
				}
			}
		});
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setInput(MatcherRegistry.getMatchers());
		comboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		formToolkit.adapt(comboViewer.getCombo(), true, true);

		Button btnGo = new Button(composite_2, SWT.NONE);
		btnGo.setImage(getFromRegistry(ICONS_START_TASK_1_GIF,
				AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ease.modules.modeling.ui", ICONS_START_TASK_1_GIF)));
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleGoto();
			}

		});
		formToolkit.adapt(btnGo, true, true);
		Button btnDel = new Button(composite_2, SWT.NONE);
		btnDel.setImage(getFromRegistry(ICONS_DELETE_OBJ_GIF,
				AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ease.modules.modeling.ui", ICONS_DELETE_OBJ_GIF)));
		btnDel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearResult();
			}

		});
		formToolkit.adapt(btnDel, true, true);

		text = new Text(composite_2, SWT.BORDER);
		text.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR)) {
					handleGoto();
				}
			}

		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		formToolkit.adapt(text, true, true);

		tableViewer_Search = new TableViewer(composite_2, SWT.BORDER | SWT.MULTI | SWT.VIRTUAL);
		tableViewer_Search.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection) event.getSelection();
					if (structured.getFirstElement() instanceof EObject) {
						EObject eo = (EObject) structured.getFirstElement();
						try {
							gotoInEditor(getCurrentEditor(), eo);
						} catch (MatcherException e) {
							e.printStackTrace();
						}
					}
				}

			}
		});
		table_Search = tableViewer_Search.getTable();
		table_Search.setHeaderVisible(true);
		table_Search.setLinesVisible(true);
		table_Search.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		formToolkit.paintBordersFor(table_Search);

		TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(tableViewer_Search, SWT.NONE);
		TableColumn tblclmnElement_1 = tableViewerColumn_5.getColumn();
		tblclmnElement_1.setWidth(100);
		tblclmnElement_1.setText(Messages.ModelRefactoringView_ELEMENT);

		TableViewerColumn tableViewerColumn_6 = new TableViewerColumn(tableViewer_Search, SWT.NONE);
		TableColumn tblclmnPath = tableViewerColumn_6.getColumn();
		tblclmnPath.setWidth(229);
		tblclmnPath.setText(Messages.ModelRefactoringView_PATH);

		tableViewer_Search.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer_Search.setLabelProvider(new DefaultTableLabelProvider() {
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return p.getImage(element);
				}
				return super.getColumnImage(element, columnIndex);
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return p.getText(element);
				case 1:
					if (element instanceof EObject) {
						EObject e = ((EObject) element).eContainer();
						StringBuilder result = new StringBuilder();
						while (e != null) {
							result = new StringBuilder(p.getText(e)).append("\\").append(result); //$NON-NLS-1$
							e = e.eContainer();
						}
						return result.toString();
					}
				}
				return super.getColumnText(element, columnIndex);
			}
		});
		getSite().setSelectionProvider(new ISelectionProvider() {

			@Override
			public void setSelection(ISelection selection) {
				tableViewer_Search.setSelection(selection);
			}

			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				tableViewer_Search.removeSelectionChangedListener(listener);
			}

			@Override
			public ISelection getSelection() {
				return tableViewer_Search.getSelection();
			}

			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				tableViewer_Search.addSelectionChangedListener(listener);
			}
		});
	}

	protected void clearResult() {
		frmNavigation.setMessage(null);
		tableViewer_Search.setInput(Lists.newArrayList());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		tableViewer_Search.getControl().setFocus();
	}

	public ISelection getSelection() {
		return tableViewer_Search.getSelection();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		frmNavigation.setMessage(null);
		if (part != this) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structured = (IStructuredSelection) selection;
				if (structured.getFirstElement() instanceof EObject) {
					EObject eobject = (EObject) structured.getFirstElement();
					handleSelectionCHanged(eobject);
				} else if (structured.getFirstElement() instanceof IAdaptable) {
					IAdaptable iadaptable = (IAdaptable) structured.getFirstElement();
					EObject eobject = (EObject) iadaptable.getAdapter(EObject.class);
					if (eobject != null) {
						handleSelectionCHanged(eobject);
					}
				}
			}
		}
	}

	private void handleSelectionCHanged(EObject eobject) {
		selectionId.setText(eobject.eResource().getURIFragment(eobject).toString());
		for (StructuredViewer v : viewers) {
			v.setInput(eobject);
		}
	}

	public class DefaultContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return null;
		}

	}

	public class DefaultTableLabelProvider implements ITableLabelProvider {
		protected AdapterFactoryLabelProvider p = new AdapterFactoryLabelProvider(factory);

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return p.getImage(element);
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return p.getText(element);
			}
			return ""; //$NON-NLS-1$
		}
	}

	public void setSelectionProvider() {
		getSite().setSelectionProvider(tableViewer_Search);
	}
}