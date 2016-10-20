/*******************************************************************************
 * Copyright (c) 2016 Madalina Hodorog and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Madalina Hodorog - Unit Tests for the VariablesTreeContentProvider Class
 *******************************************************************************/

package org.eclipse.ease.modules.unittest.ui.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel.Variable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VariablesTreeContentProviderTest {

	// helper constants
	private static final String ID_GROUP1_SUBGROUP1_SUBGROUP11 = "/group1/subgroup1/subgroup11";
	private static final String ID_GROUP1_SUBGROUP1 = "/group1/subgroup1";
	private static final String ID_GROUP1 = "/group1";

	private static final String ID_GROUP_SUBGROUP_SUBGROUP = "/group/group/group";
	private static final String ID_GROUP_SUBGROUP = "/group/group";
	private static final String ID_GROUP = "/group";

	// helper fields
	private VariablesTreeContentProvider fVariablesTreeContentProvider;
	private final Collection<IPath> fAdditionalPathsTest = new HashSet<IPath>();

	private List<Variable> fVariablesList;
	private TestSuiteModel fTestSuiteModel;

	// helper methods
	private void createAdditionalPaths(boolean identicalNames) {
		if (!identicalNames) {
			fAdditionalPathsTest.add(new Path(ID_GROUP1));
			fAdditionalPathsTest.add(new Path(ID_GROUP1_SUBGROUP1));
			fAdditionalPathsTest.add(new Path(ID_GROUP1_SUBGROUP1_SUBGROUP11));
		} else {
			fAdditionalPathsTest.add(new Path(ID_GROUP));
			fAdditionalPathsTest.add(new Path(ID_GROUP_SUBGROUP));
			fAdditionalPathsTest.add(new Path(ID_GROUP_SUBGROUP_SUBGROUP));
		}
	}

	private void createPaths() {
		final Variable testVariable1 = fTestSuiteModel.new Variable("frodo", "creative", "hobbit", new Path("/fellowshipOfTheRing/hobbits"));
		final Variable testVariable2 = fTestSuiteModel.new Variable("sam", "wisebeyondage", "hobbit", new Path("/fellowshipOfTheRing/hobbits"));
		final Variable testVariable3 = fTestSuiteModel.new Variable("legolas", "fierceful", "elf", new Path("/fellowshipOfTheRing/elfs"));
		fVariablesList.add(testVariable1);
		fVariablesList.add(testVariable2);
		fVariablesList.add(testVariable3);
	}

	private void removeFoundElements(List<Variable> expectedVariables, Object[] actualVariables) {
		for (final Object element : actualVariables)
			if (expectedVariables.contains(element))
				expectedVariables.remove(element);
	}

	// -------------------------------Unit Tests------------------------------------------------------------

	@Before
	public void setUp() throws Exception {
		fVariablesTreeContentProvider = new VariablesTreeContentProvider();
		fVariablesList = new ArrayList<>();
		fTestSuiteModel = new TestSuiteModel();
		createAdditionalPaths(false);
	}

	@After
	public void tearDown() throws Exception {
		fAdditionalPathsTest.clear();
	}

	// -------------------------------exchangePrefixPaths() test cases------------------------------------------------------------
	@Test
	public void exchangePrefixPaths() {
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1_SUBGROUP11));

		fVariablesTreeContentProvider.exchangePrefixPaths(new Path(ID_GROUP1), "renamedGroup");

		final Object[] renamedPathsGroup = fVariablesTreeContentProvider.getElements(new HashSet<IPath>());
		assertEquals(1, renamedPathsGroup.length);
		assertEquals(new Path("renamedGroup"), renamedPathsGroup[0]);

		final Object[] renamedPathsSubgroup = fVariablesTreeContentProvider.getChildren(new Path("renamedGroup"));
		assertEquals(1, renamedPathsSubgroup.length);
		assertEquals(new Path("renamedGroup/subgroup1"), renamedPathsSubgroup[0]);

		final Object[] renamedPathsSubsubgroup = fVariablesTreeContentProvider.getChildren(new Path("renamedGroup/subgroup1"));
		assertEquals(1, renamedPathsSubsubgroup.length);
		assertEquals(new Path("renamedGroup/subgroup1/subgroup11"), renamedPathsSubsubgroup[0]);
	}

	@Test
	public void exchangePrefixPathsIdenticalNames() {
		createAdditionalPaths(true);
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP_SUBGROUP));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP_SUBGROUP_SUBGROUP));

		fVariablesTreeContentProvider.exchangePrefixPaths(new Path("/group"), "renamedGroup");

		final Object[] renamedPathsGroup = fVariablesTreeContentProvider.getElements(new HashSet<IPath>());
		assertEquals(1, renamedPathsGroup.length);
		assertEquals(new Path("renamedGroup"), renamedPathsGroup[0]);

		final Object[] renamedPathsSubgroup = fVariablesTreeContentProvider.getChildren(new Path("renamedGroup"));
		assertEquals(1, renamedPathsSubgroup.length);
		assertEquals(new Path("renamedGroup/group"), renamedPathsSubgroup[0]);

		final Object[] renamedPathsSubsubgroup = fVariablesTreeContentProvider.getChildren(new Path("renamedGroup/group"));
		assertEquals(1, renamedPathsSubsubgroup.length);
		assertEquals(new Path("renamedGroup/group/group"), renamedPathsSubsubgroup[0]);
	}

	// -------------------------------moveSelectedPath() test cases------------------------------------------------------------
	@Test
	public void moveSelectedPathToParentPath() {
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1_SUBGROUP11));

		fVariablesTreeContentProvider.moveSelectedPath(new Path("/group1/subgroup11"), new Path("/group1/subgroup1/subgroup11"));

		final Object[] actualAdditionalPathSubgroup1 = fVariablesTreeContentProvider.getChildren(new Path(ID_GROUP1).makeRelative());
		assertEquals(2, actualAdditionalPathSubgroup1.length);
		assertEquals(new Path("group1/subgroup1"), actualAdditionalPathSubgroup1[0]);
		assertEquals(new Path("group1/subgroup11"), actualAdditionalPathSubgroup1[1]);

		assertFalse(fVariablesTreeContentProvider.hasChildren(new Path(ID_GROUP1_SUBGROUP1)));
	}

	@Test
	public void moveSelectedPathToRootPath() {
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1));

		fVariablesTreeContentProvider.moveSelectedPath(new Path("/subgroup1"), new Path("/group1/subgroup1"));

		fVariablesTreeContentProvider.getElements(new HashSet<IPath>());
		final Object[] actualAdditionalPathRoot = fVariablesTreeContentProvider.getChildren(new Path(""));
		assertEquals(2, actualAdditionalPathRoot.length);
		assertEquals(new Path("group1"), actualAdditionalPathRoot[0]);
		assertEquals(new Path("subgroup1"), actualAdditionalPathRoot[1]);

		assertFalse(fVariablesTreeContentProvider.hasChildren(new Path(ID_GROUP1).makeRelative()));
	}

	// -------------------------------updateVariablesForRenamedNode() test cases------------------------------------------------------------
	@Test
	public void updateVariablesForRenamedNode() {
		createPaths();
		fVariablesTreeContentProvider.updateVariablesForRenamedNode(fVariablesList, new Path("/fellowshipOfTheRing/hobbits"), "popularHobbits");

		assertEquals(new Path("/fellowshipOfTheRing/popularHobbits"), fVariablesList.get(0).getPath());
		assertEquals(new Path("/fellowshipOfTheRing/popularHobbits"), fVariablesList.get(1).getPath());
	}

	// -------------------------------updatePathGroup() test cases------------------------------------------------------------
	@Test
	public void updatePathGroup() {
		createPaths();
		VariablesTreeContentProvider.updatePathGroup(fVariablesList, new Path("/hobbits"), new Path("fellowshipOfTheRing/hobbits"));

		assertEquals(new Path("/hobbits"), fVariablesList.get(0).getPath());
		assertEquals(new Path("/hobbits"), fVariablesList.get(1).getPath());
	}

	// -------------------------------populateElements() test cases------------------------------------------------------------
	@Test
	public void populateElementsAdditionalPaths() {
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1));
		fVariablesTreeContentProvider.addPath(new Path(ID_GROUP1_SUBGROUP1_SUBGROUP11));

		final Object[] actualAdditionalPaths = fVariablesTreeContentProvider.getElements(fAdditionalPathsTest);
		assertEquals(1, actualAdditionalPaths.length);

		final Object[] actualAdditionalPathSubgroup1 = fVariablesTreeContentProvider.getChildren(new Path(ID_GROUP1).makeRelative());
		assertEquals(1, actualAdditionalPathSubgroup1.length);
		assertEquals(new Path(ID_GROUP1_SUBGROUP1).makeRelative(), actualAdditionalPathSubgroup1[0]);

		final Object[] actualAdditionalPathSubgroup11 = fVariablesTreeContentProvider.getChildren(new Path(ID_GROUP1_SUBGROUP1).makeRelative());
		assertEquals(1, actualAdditionalPathSubgroup11.length);
		assertEquals(new Path(ID_GROUP1_SUBGROUP1_SUBGROUP11).makeRelative(), actualAdditionalPathSubgroup11[0]);

	}

	@Test
	public void populateElementsEmptyList() {
		final Object[] actualEmptyPaths = fVariablesTreeContentProvider.getElements(new HashSet<IPath>());
		assertTrue(actualEmptyPaths.length == 0);
	}

	@Test
	public void populateElementsPaths() {
		final List<Variable> expectedVariables = fVariablesList;
		createPaths();

		final Object[] actualPaths = fVariablesTreeContentProvider.getElements(fVariablesList);
		assertEquals(1, actualPaths.length);
		assertEquals(new Path("fellowshipOfTheRing"), actualPaths[0]);

		final Object[] actualPathSubgroup0 = fVariablesTreeContentProvider.getChildren(new Path("fellowshipOfTheRing"));
		assertEquals(2, actualPathSubgroup0.length);

		final Object[] actualVariablesSubgroup0 = fVariablesTreeContentProvider.getChildren(new Path("fellowshipOfTheRing/hobbits"));
		assertEquals(2, actualVariablesSubgroup0.length);
		removeFoundElements(expectedVariables, actualVariablesSubgroup0);

		final Object[] actualVariablesSubgroup1 = fVariablesTreeContentProvider.getChildren(new Path("fellowshipOfTheRing/elfs"));
		assertEquals(1, actualVariablesSubgroup1.length);
		removeFoundElements(expectedVariables, actualVariablesSubgroup1);

		assertTrue(expectedVariables.isEmpty());
	}
}