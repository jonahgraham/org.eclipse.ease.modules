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
package org.eclipse.ease.modules.unittest.ui.sourceprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ease.modules.unittest.ITestListener;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;

public class TestSuiteSource extends AbstractSourceProvider implements ITestListener {

	public static final String VARIABLE_TESTSUITE = Activator.PLUGIN_ID + ".testsuite";
	private static final String[] SOURCES = new String[] { VARIABLE_TESTSUITE };

	private Object fCurrentSuite = IEvaluationContext.UNDEFINED_VARIABLE;

	public static TestSuiteSource getActiveInstance() {

		try {
			final ISourceProviderService sourceService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			final Object testSuiteSource = sourceService.getSourceProvider(TestSuiteSource.VARIABLE_TESTSUITE);
			if (testSuiteSource instanceof TestSuiteSource)
				return (TestSuiteSource) testSuiteSource;
		} catch (final IllegalStateException e) {
			// no workbench available, we might be running headless
		}

		return null;
	}

	public TestSuiteSource() {
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		final Map<String, Object> result = new HashMap<>();

		result.put(VARIABLE_TESTSUITE, fCurrentSuite);

		return result;
	}

	public void setActiveSuite(final TestSuite suite) {
		if (fCurrentSuite.equals(suite))
			// nothing changed
			return;

		if (fCurrentSuite instanceof TestSuite)
			((TestSuite) fCurrentSuite).removeTestListener(this);

		fCurrentSuite = (suite != null) ? suite : IEvaluationContext.UNDEFINED_VARIABLE;
		if (fCurrentSuite instanceof TestSuite)
			((TestSuite) fCurrentSuite).addTestListener(this);

		fireSourceChanged(ISources.ACTIVE_PART, VARIABLE_TESTSUITE, fCurrentSuite);

		final IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
		evaluationService.requestEvaluation(VARIABLE_TESTSUITE);
	}

	@Override
	public String[] getProvidedSourceNames() {
		return SOURCES;
	}

	@Override
	public void notify(final Object testObject, final TestStatus status) {
		if (testObject.equals(fCurrentSuite)) {
			fireSourceChanged(ISources.ACTIVE_PART, VARIABLE_TESTSUITE, fCurrentSuite);

			final IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
			evaluationService.requestEvaluation(VARIABLE_TESTSUITE);
		}
	}
}
