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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.eclipse.ease.IScriptEngine;
import org.junit.Test;

public class ResourcesModuleTest extends AbstractModuleTest {

	@Override
	protected Object getModuleClass() {
		return ResourcesModule.class;
	}

	@Override
	protected Object getModuleID() {
		return ResourcesModule.MODULE_ID;
	}

	@Test
	public void getFile() throws IOException {
		File tempFile = File.createTempFile("ease_unittest_", "");

		// mocked script engine
		IScriptEngine mockEngine = mock(IScriptEngine.class);
		when(mockEngine.getExecutedFile()).thenReturn(tempFile);

		// initialize module
		ResourcesModule module = new ResourcesModule();
		module.initialize(mockEngine, null);

		// test
		Object file = module.getFile(tempFile.toString(), true);
		assertEquals(tempFile, file);

		// cleanup
		tempFile.delete();
	}
}
