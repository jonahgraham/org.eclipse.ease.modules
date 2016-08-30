/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Represents the Variables component in the Test Suite Editor. This class implements the Variables component in Test Suite Editor with a tree structure
 * allowing to classify variables in groups. New variables are added either in the back-end directly in the XML content of the source file by specifying the
 * path component for the variable or in the front end by using the context menu allowing the user to make use of the following actions: add new group, add new
 * sibling group, add variable and remove any entity.
 */
public class Variables extends AbstractEditorPage {

	public static final String VARIABLES_EDITOR_ID = "org.eclipse.ease.editor.variables";

	private static final String DEFAULT_VARIABLE_CONTENT = "\"my content\"";

	private static final String NAME_NEW_VARIABLE_GROUP = "myGroup";

	public static final String EMPTY_STRING = "";

	private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("([a-zA-Z_$][0-9a-zA-Z_$]*)");

	private Tree fTree;

	private TreeViewer fTreeViewer;

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public Variables(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public Variables(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 *
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		super.createFormContent(managedForm);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		final Label lblDefineVariablesThat = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblDefineVariablesThat.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(lblDefineVariablesThat, true, true);
		lblDefineVariablesThat.setText("Define variables that will be visible in your scripts. Ordering is important if variables depend on each other.");

		final Composite composite = new Composite(managedForm.getForm().getBody(), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		managedForm.getToolkit().adapt(composite);
		managedForm.getToolkit().paintBordersFor(composite);

		final TreeColumnLayout tcl_composite = new TreeColumnLayout();
		composite.setLayout(tcl_composite);

		final Tree tree = new Tree(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		fTreeViewer = new TreeViewer(tree);
		fTree = fTreeViewer.getTree();
		fTree.setHeaderVisible(true);
		fTree.setLinesVisible(true);
		managedForm.getToolkit().paintBordersFor(fTree);

		fTreeViewer.setContentProvider(new VariablesTreeContentProvider());
		getSite().setSelectionProvider(fTreeViewer);

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnVariable = treeViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnVariable, new ColumnWeightData(100, 100, true));
		tblclmnVariable.setText("Variable");
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Variable)
					return ((Variable) element).getName();

				if (element instanceof IPath)
					return ((IPath) element).lastSegment();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				if (element instanceof Variable) {
					return DebugUITools.getImage(IDebugUIConstants.IMG_VIEW_VARIABLES);
				} else if (element instanceof IPath) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				}

				return super.getImage(element);
			}
		});

		treeViewerColumn.setEditingSupport(new EditingSupport(fTreeViewer) {

			@Override
			protected void setValue(Object element, final Object value) {
				if (element instanceof Variable) {
					if (checkName(value.toString())) {
						final String oldVariableName = ((Variable) element).getName();
						final String newVariableName = value.toString();
						if (!oldVariableName.equals(newVariableName)) {
							((Variable) element).setName(value.toString());
							fTreeViewer.update(element, null);
							setDirty();
						}
					}
				} else if (element instanceof IPath) {
					final VariablesTreeContentProvider treeContentProvider = (VariablesTreeContentProvider) fTreeViewer.getContentProvider();
					// identified node to rename
					final IPath node = (IPath) element;
					if (!node.lastSegment().equals(value.toString())) {
						final List<Variable> variables = getModel().getVariables();
						// update the path of all variables that have the old path node to the new value value
						treeContentProvider.updateVariablesForRenamedNode(variables, node, value.toString());
						// rename the node to the new value value
						treeContentProvider.exchangePrefixPaths(node, (String) value);
						setDirty();
					}
				}
				updateTreeViewerToLevel();
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof Variable) {
					return ((Variable) element).getName();
				} else if (element instanceof IPath) {
					return ((IPath) element).lastSegment();
				}
				return EMPTY_STRING;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTree);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}
		});

		fTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				if ((e1 instanceof Variable) && (e2 instanceof Variable))
					return (((Variable) e1).getName()).compareToIgnoreCase(((Variable) e2).getName());
				else if ((e1 instanceof IPath) && (e2 instanceof IPath))
					return (e1.toString().compareTo(e2.toString()));

				return super.compare(viewer, e1, e2);
			}

			@Override
			public int category(Object element) {
				return (element instanceof IPath) ? 1 : 2;
			}
		});

		final TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnContent = treeViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnContent, new ColumnWeightData(100, 100, true));
		tblclmnContent.setText("Content");
		treeViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Variable)
					return ((Variable) element).getContent();

				if (element instanceof IPath)
					return EMPTY_STRING;

				return super.getText(element);
			}
		});

		treeViewerColumn_1.setEditingSupport(new EditingSupport(fTreeViewer) {
			@Override
			protected void setValue(final Object element, final Object value) {
				if (element instanceof Variable) {
					final String oldVariableContent = ((Variable) element).getContent();
					final String newVariableContent = value.toString();
					if (!oldVariableContent.equals(newVariableContent)) {
						((Variable) element).setContent(newVariableContent);
						setDirty();
						fTreeViewer.update(element, null);
					}
				}
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof Variable)
					return ((Variable) element).getContent();

				return EMPTY_STRING;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTree);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return (element instanceof Variable);
			}
		});

		final TreeViewerColumn treeViewerColumn_3 = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnDescription = treeViewerColumn_3.getColumn();
		tcl_composite.setColumnData(tblclmnDescription, new ColumnWeightData(100, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnDescription.setText("Description");
		treeViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Variable)
					return ((Variable) element).getDescription();

				if (element instanceof IPath)
					return EMPTY_STRING;

				return super.getText(element);
			}
		});
		treeViewerColumn_3.setEditingSupport(new EditingSupport(fTreeViewer) {

			@Override
			protected void setValue(final Object element, final Object value) {
				if (element instanceof Variable) {
					((Variable) element).setDescription(value.toString());
					setDirty();
					fTreeViewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof Variable)
					return ((Variable) element).getDescription();

				return EMPTY_STRING;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTree);
			}

			@Override
			protected boolean canEdit(final Object element) {
				return (element instanceof Variable);
			}
		});

		initializeDnD();

		update();

		final MenuManager contextMenu = new MenuManager("", VARIABLES_EDITOR_ID);
		final Menu menu = contextMenu.createContextMenu(fTreeViewer.getTree());
		contextMenu.setRemoveAllWhenShown(true);
		fTreeViewer.getTree().setMenu(menu);
		getEditorSite().registerContextMenu(VARIABLES_EDITOR_ID, contextMenu, fTreeViewer, false);
	}

	@Override
	protected String getPageTitle() {
		return "Variables";
	}

	@Override
	protected void update() {
		fTreeViewer.setInput(getModel().getVariables());
		fTreeViewer.refresh();
	}

	/**
	 * Provides drag and drop functionality for the tree structure.
	 */
	private void initializeDnD() {
		final int operations = DND.DROP_MOVE;
		final Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		fTreeViewer.addDragSupport(operations, transferTypes, new VariablesDragListener(fTreeViewer));
		final VariablesDropSupport dropSupport = new VariablesDropSupport(fTreeViewer, getModel());
		fTreeViewer.addDropSupport(operations, transferTypes, dropSupport);
	}

	private void updateTreeViewerToLevel() {
		final Object[] elements = fTreeViewer.getExpandedElements();
		fTreeViewer.refresh();
		fTreeViewer.setExpandedElements(elements);
	}

	private String createVariable() {
		// find available variable name
		final String baseName = "myVariable";
		String newName = baseName;
		int index = 1;
		while (!checkName(newName))
			newName = baseName + "_" + Integer.toString(index++);

		return newName;
	}

	private boolean checkName(final String newName) {
		if (VARIABLE_NAME_PATTERN.matcher(newName).matches()) {
			// check if name already exists
			for (final Variable variable : getModel().getVariables()) {
				if (newName.equals(variable.getName()))
					return false;
			}
			return true;
		}
		return false;
	}

	public static String createGroupName(Object[] childrenGroup) {
		final String baseName = NAME_NEW_VARIABLE_GROUP;
		String name = baseName;
		int index = 1;
		while (!checkNameSubgroup(name, childrenGroup))
			name = baseName + " " + Integer.toString(index++);

		return name + Path.ROOT.toString();
	}

	public static boolean checkNameSubgroup(String name, Object[] childrenGroup) {
		for (final Object child : childrenGroup) {
			if (child instanceof IPath) {
				final String childName = ((IPath) child).lastSegment();
				if (childName.equals(name))
					return false;
			}
		}
		return true;
	}

	private void createGroup(IPath parentGroupPath) {
		final VariablesTreeContentProvider contentProvider = (VariablesTreeContentProvider) fTreeViewer.getContentProvider();
		final String name = createGroupName(contentProvider.getChildren(parentGroupPath));
		final IPath newPath = parentGroupPath.append(name);
		contentProvider.addPath(newPath);
		updateTreeViewerToLevel();
	}

	private void addVariableToGroup() {
		final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
		if (selection.size() == 1) {
			final Object selectedNode = selection.getFirstElement();
			if (selectedNode instanceof IPath) {
				final IPath currentGroup = (IPath) selectedNode;
				getModel().addVariable(createVariable(), DEFAULT_VARIABLE_CONTENT, null, currentGroup);
			} else if (selectedNode instanceof Variable) {
				final Variable currentVariable = (Variable) selectedNode;
				getModel().addVariable(createVariable(), DEFAULT_VARIABLE_CONTENT, null, currentVariable.getPath());
			}
		} else {
			getModel().addVariable(createVariable(), DEFAULT_VARIABLE_CONTENT, null, Path.ROOT);
		}
	}

	/**
	 * Removes the selected object (i.e. group of variables or variable) in the tree.
	 */
	private void remove() {
		final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
		for (final Object node : selection.toList()) {
			if (node instanceof Variable) {
				getModel().removeVariable((Variable) node);
			} else if (node instanceof IPath) {
				removeVariableGroup((IPath) node);
			}
		}
		setDirty();
		updateTreeViewerToLevel();
	}

	private void removeVariableGroup(IPath node) {
		final List<Variable> variables = getModel().getVariables();
		final List<Variable> variablesSelected = new ArrayList<Variable>();
		for (final Variable variable : variables) {
			// remove all groups referred by node and all subgroups having the parent node
			if (variable.getPath().uptoSegment(node.segmentCount()).equals(node.makeAbsolute()))
				variablesSelected.add(variable);
		}

		for (final Variable variableSelected : variablesSelected)
			getModel().removeVariable(variableSelected);

		final VariablesTreeContentProvider treeContentProvider = (VariablesTreeContentProvider) fTreeViewer.getContentProvider();
		treeContentProvider.removeMatchingPaths(node);
	}

	public TreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	/**
	 * Adds a new group in the tree viewer in the test suite variables component.
	 *
	 * @param node
	 */
	public void addGroup(IPath node) {
		createGroup(node);
		updateTreeViewerToLevel();
	}

	/**
	 * Adds a new variable in the tree viewer in the test suite variables component.
	 */
	public void addVariable() {
		addVariableToGroup();
		setDirty();
		updateTreeViewerToLevel();
	}

	/**
	 * Removes any selected element from the tree viewer in the test suite variables component.
	 */
	public void removeSelectedNodes() {
		remove();
		setDirty();
		updateTreeViewerToLevel();
	}
}