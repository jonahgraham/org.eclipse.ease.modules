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
package org.eclipse.ease.modules.unittest.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

public class ExportErrorsAsSuite extends AbstractHandler implements IHandler {

	// TODO implement
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		// final TestSuiteSource instance = TestSuiteSource.getActiveInstance();
		// if (instance != null) {
		// final Object suite = instance.getCurrentState().get(TestSuiteSource.VARIABLE_TESTSUITE);
		// if (suite instanceof TestSuite) {
		// // copy suite description
		// TestSuiteModel description = new TestSuiteModel(((TestSuite) suite).getModel());
		// description.clearTestFiles();
		//
		// for (TestFile testFile : ((TestSuite) suite).getActiveTestFiles()) {
		// if ((testFile.getStatus() == TestStatus.ERROR) || (testFile.getStatus() == TestStatus.FAILURE))
		// description.addTestFile(testFile.getFile());
		// }
		//
		// // store new description to workspace
		// SaveAsDialog dialog = new SaveAsDialog(HandlerUtil.getActiveShell(event));
		// dialog.setTitle("Store new TestSuite");
		//
		// // calculate default name for new suite
		// IFile originalSuiteFile = ((TestSuite) suite).getModel().getFile();
		// String originalFileName = originalSuiteFile.getName().substring(0,
		// originalSuiteFile.getName().length() - originalSuiteFile.getFileExtension().length() - 1);
		// IFile targetFile = originalSuiteFile.getParent().getFile(new Path(originalFileName + " Errors.suite"));
		// dialog.setOriginalFile(targetFile);
		//
		// if (dialog.open() == Window.OK) {
		// InputStream content = new ByteArrayInputStream(description.toMemento().toString().getBytes());
		// IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(dialog.getResult());
		// try {
		// if (file.exists())
		// file.setContents(content, 0, new NullProgressMonitor());
		// else
		// file.create(content, 0, new NullProgressMonitor());
		// } catch (CoreException e) {
		// Logger.error("Could not store suite file.", e);
		// }
		// }
		// }
		// }

		return null;
	}
}
