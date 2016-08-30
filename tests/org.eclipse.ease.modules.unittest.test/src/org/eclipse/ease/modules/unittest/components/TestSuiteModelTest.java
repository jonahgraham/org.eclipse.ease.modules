/*******************************************************************************
 * Copyright (c) 2016 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

public class TestSuiteModelTest {

	String[] fVariableIndentifier;
	String[] fVariableContent;
	String[] fVariableDescription;
	String[] fVariablePath;

	private static final int ELEMENTS = 3;

	@Before
	public void setUp() {

		fVariableIndentifier = new String[ELEMENTS];
		fVariableContent = new String[ELEMENTS];
		fVariableDescription = new String[ELEMENTS];
		fVariablePath = new String[ELEMENTS];

		for (int i = 0; i < ELEMENTS; i++) {
			fVariableIndentifier[i] = "variableIndentifier" + i;
			fVariableContent[i] = "variableContent" + i;
			fVariableDescription[i] = "variableDescription" + i;
			fVariablePath[i] = "/" + "variablePath" + i;
		}
	}

	@Test
	public void getVariableCount() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();
		assertEquals(0, testSuiteModel.getVariables().size());
		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
			assertEquals(index + 1, testSuiteModel.getVariables().size());
		}
	}

	@Test
	public void hasVariable() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();
		testSuiteModel.setVariable(fVariableIndentifier[0], fVariableContent[0], fVariableDescription[0], new Path(fVariablePath[0]));
		assertTrue(testSuiteModel.hasVariable(fVariableIndentifier[0]));
	}

	@Test
	public void addVariable() {
		final TestSuiteModel testSuiteModel = new TestSuiteModel();

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.addVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
			assertEquals(fVariableIndentifier[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getName());
			assertEquals(fVariableContent[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getContent());
			assertEquals(fVariableDescription[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getDescription());
			assertEquals(new Path(fVariablePath[index]), testSuiteModel.getVariable(fVariableIndentifier[index]).getPath());
		}
	}

	@Test
	public void getVariable() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();

		assertNull(testSuiteModel.getVariable("dummyIdentifier"));

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
		}

		for (int index = 0; index < ELEMENTS; index++) {
			assertEquals(fVariableIndentifier[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getName());
			assertEquals(fVariableContent[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getContent());
			assertEquals(fVariableDescription[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getDescription());
			assertEquals(new Path(fVariablePath[index]), testSuiteModel.getVariable(fVariableIndentifier[index]).getPath());
		}
	}

	@Test
	public void setVariable() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
			assertEquals(fVariableIndentifier[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getName());
			assertEquals(fVariableContent[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getContent());
			assertEquals(fVariableDescription[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getDescription());
			assertEquals(new Path(fVariablePath[index]), testSuiteModel.getVariable(fVariableIndentifier[index]).getPath());
		}
	}

	@Test
	public void updateVariableContent() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();
		final String postFix = "Changed";

		for (int index = 0; index < (ELEMENTS - 1); index++) {
			testSuiteModel.addVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
		}

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index] + postFix, fVariableDescription[index] + postFix,
					new Path(fVariablePath[index] + postFix));
			assertEquals(fVariableIndentifier[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getName());
			assertEquals(fVariableContent[index] + postFix, testSuiteModel.getVariable(fVariableIndentifier[index]).getContent());
			assertEquals(fVariableDescription[index] + postFix, testSuiteModel.getVariable(fVariableIndentifier[index]).getDescription());
			assertEquals(new Path(fVariablePath[index] + postFix), testSuiteModel.getVariable(fVariableIndentifier[index]).getPath());
		}

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index] + postFix + postFix, null, null);
			assertEquals(fVariableIndentifier[index], testSuiteModel.getVariable(fVariableIndentifier[index]).getName());
			assertEquals(fVariableContent[index] + postFix + postFix, testSuiteModel.getVariable(fVariableIndentifier[index]).getContent());
			assertEquals(fVariableDescription[index] + postFix, testSuiteModel.getVariable(fVariableIndentifier[index]).getDescription());
			assertEquals(new Path(fVariablePath[index] + postFix), testSuiteModel.getVariable(fVariableIndentifier[index]).getPath());
		}
	}

	@Test
	public void removeVariable() {

		final TestSuiteModel testSuiteModel = new TestSuiteModel();

		for (int index = 0; index < ELEMENTS; index++) {
			testSuiteModel.setVariable(fVariableIndentifier[index], fVariableContent[index], fVariableDescription[index], new Path(fVariablePath[index]));
		}

		for (int index = 0; index < ELEMENTS; index++) {
			assertNotNull(testSuiteModel.getVariable(fVariableIndentifier[index]));
			testSuiteModel.removeVariable(testSuiteModel.getVariable(fVariableIndentifier[index]));
			assertFalse(testSuiteModel.hasVariable(fVariableIndentifier[index]));
		}
	}
}
