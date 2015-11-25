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

package org.eclipse.ease.modules.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ScriptingModuleTest {

	@Test
	public void storeTemporaryObject() {
		Object testObject = new Object();

		// mocked script engine
		IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		try {
			module.setSharedObject("temp", testObject, false, false);
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}

		// retrieve the object
		assertEquals(testObject, module.getSharedObject("temp"));

		// capture execution listener
		ArgumentCaptor<IExecutionListener> argument = ArgumentCaptor.forClass(IExecutionListener.class);
		verify(mockEngine).addExecutionListener(argument.capture());

		// terminate engine
		argument.getValue().notify(mockEngine, null, IExecutionListener.ENGINE_END);

		// make sure the object got removed
		assertNull(module.getSharedObject("temp"));
	}

	@Test
	public void storePermanentObject() {
		Object testObject = new Object();

		// mocked script engine
		IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		try {
			module.setSharedObject("perm", testObject, true, false);

			// set another temp object to make sure the execution listener gets installed
			module.setSharedObject("anotherTemp", testObject, false, false);
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}

		// retrieve the object
		assertEquals(testObject, module.getSharedObject("perm"));

		// capture execution listener
		ArgumentCaptor<IExecutionListener> argument = ArgumentCaptor.forClass(IExecutionListener.class);
		verify(mockEngine).addExecutionListener(argument.capture());

		// terminate engine
		argument.getValue().notify(mockEngine, null, IExecutionListener.ENGINE_END);

		// make sure the object got removed
		assertEquals(testObject, module.getSharedObject("perm"));
	}

	@Test(expected = IllegalAccessException.class)
	public void overwriteForeignObject() throws IllegalAccessException {
		Object testObject = new Object();

		// mocked script engine
		IScriptEngine creatorEngine = mock(IScriptEngine.class);
		IScriptEngine modifierEngine = mock(IScriptEngine.class);

		// initialize modules
		ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(creatorEngine, null);

		ScriptingModule modifierModule = new ScriptingModule();
		modifierModule.initialize(modifierEngine, null);

		// set the object
		try {
			creatorModule.setSharedObject("foreign", testObject, false, false);
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}

		modifierModule.setSharedObject("foreign", testObject, false, false);
	}

	@Test
	public void overwriteForeignUnlockedObject() throws IllegalAccessException {
		Object testObject = new Object();

		// mocked script engine
		IScriptEngine creatorEngine = mock(IScriptEngine.class);
		IScriptEngine modifierEngine = mock(IScriptEngine.class);

		// initialize modules
		ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(creatorEngine, null);

		ScriptingModule modifierModule = new ScriptingModule();
		modifierModule.initialize(modifierEngine, null);

		// set the object
		creatorModule.setSharedObject("foreignShared", testObject, false, true);

		// reset the object
		modifierModule.setSharedObject("foreignShared", testObject, false, false);
	}
}
