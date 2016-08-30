/*******************************************************************************
 * Copyright (c) 2016 Madalina Hodorog and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Madalina Hodorog - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.unittest.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;

/**
 * Represents the content provider for the Variables component in the Test Suite Editor. This class implements the Tree Content Provider for the Variables
 * component content, used to make the elements in the component expandable. For the expansion property, a tree data structure is used. The
 * VariablesTreeContentProvider class defines the relationship between the parent and the children elements in the tree by overriding
 * {@link #populateElements(Object)}. The structure of the tree is defined in the AbstractVirtualTreeProvider class.
 */
public class VariablesTreeContentProvider extends AbstractVirtualTreeProvider {

	private final Collection<IPath> fAdditionalPaths = new HashSet<IPath>();

	/**
	 * Registers elements in the tree viewer from the test suites variables component.
	 *
	 * @param inputElement
	 *            List of Variables defined for the test suite to be registered in the tree viewer.
	 */
	@Override
	protected void populateElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			final List<?> elements = (List<?>) inputElement;
			// register the elements of the tree
			for (final Object convertedInputElement : elements) {
				if (convertedInputElement instanceof Variable) {
					final Variable variable = (Variable) convertedInputElement;
					final IPath path = variable.getPath();
					registerElement(path, convertedInputElement);
				}
			}
		}
		setShowRoot(false);

		for (final IPath additionalPath : fAdditionalPaths)
			registerPath(additionalPath.makeAbsolute());
	}

	public void addPath(IPath path) {
		fAdditionalPaths.add(path);
		registerPath(path);
	}

	public void removePath(IPath path) {
		fAdditionalPaths.remove(path);
	}

	/**
	 * Builds a new path entity by appending the source path with the first count segments removed to the target path.
	 */
	public static IPath buildPath(IPath sourcePath, IPath targetPath, int count) {
		sourcePath = sourcePath.removeFirstSegments(count);
		return targetPath.append(sourcePath);
	}

	/**
	 * Removes all paths matching the path prefix from the tree content provider.
	 */
	public void removeMatchingPaths(IPath pathPrefix) {
		for (final IPath currentPath : new HashSet<IPath>(fAdditionalPaths))
			if (pathPrefix.isPrefixOf(currentPath))
				removePath(currentPath);
	}

	/**
	 * Exchanges the path prefix of all paths from the tree content provider with the new value.
	 */
	public void exchangePrefixPaths(IPath pathPrefix, String newValue) {
		for (final IPath currentPath : new HashSet<IPath>(fAdditionalPaths))
			if (pathPrefix.isPrefixOf(currentPath)) {
				fAdditionalPaths.remove(currentPath);
				IPath newPath = pathPrefix.removeLastSegments(1);
				newPath = newPath.append(newValue);
				final int count = pathPrefix.segmentCount();
				fAdditionalPaths.add(buildPath(currentPath, newPath, count));
			}
	}

	/**
	 * Moves the selected path to the indicated parent instance by drag-and-drop operation.
	 *
	 * @param parentTargetDrop
	 *            Parent instance for the path to be moved by drag-and-drop operation
	 * @param selection
	 *            Path instance to be moved by drag-and-drop operation
	 */
	public void moveSelectedPath(IPath parentTargetDrop, IPath selection) {
		final int segmentsPath = selection.segmentCount();
		for (final IPath currentPath : new HashSet<IPath>(fAdditionalPaths))
			if (currentPath.uptoSegment(segmentsPath).equals(selection)) {
				final IPath oldPath = currentPath;
				final int count = selection.segmentCount();
				fAdditionalPaths.add(buildPath(oldPath, parentTargetDrop, count));
			}
		removeMatchingPaths(selection);
	}

	public void updateVariablesForRenamedNode(List<Variable> variables, IPath node, String newValue) {
		final List<Variable> variablesRenamed = new ArrayList<Variable>();
		for (final Variable variable : variables)
			if (variable.getPath().uptoSegment(node.segmentCount()).equals(node.makeAbsolute()))
				variablesRenamed.add(variable);

		for (final Variable currentVariable : variablesRenamed) {
			IPath newPath = node.removeLastSegments(1);
			newPath = newPath.append(newValue);
			final int count = node.segmentCount();
			currentVariable.setPath(buildPath(currentVariable.getPath(), newPath, count));
		}
	}

	public static void updatePathGroup(List<Variable> variables, IPath node, IPath selection) {
		final int segmentsSelection = selection.segmentCount();
		for (final Variable variable : variables)
			if (variable.getPath().uptoSegment(segmentsSelection).equals(selection.makeAbsolute())) {
				final IPath oldPath = variable.getPath();
				final int count = selection.segmentCount();
				variable.setPath(buildPath(oldPath, node, count));
			}
	}
}