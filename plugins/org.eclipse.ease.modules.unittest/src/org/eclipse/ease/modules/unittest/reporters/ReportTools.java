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
package org.eclipse.ease.modules.unittest.reporters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ReportTools {

	private static final String EXTENSION_REPORTS_ID = "org.eclipse.ease.modules.unittest.report";
	private static final String EXTENSION_GENERATOR = "generator";
	private static final String EXTENSION_GENERATOR_NAME = "name";
	private static final String EXTENSION_GENERATOR_CLASS = "class";

	/**
	 * Get a list of available report generators.
	 *
	 * @return list of report generator names (sorted)
	 */
	public static List<String> getReportTemplates() {
		final List<String> templates = new ArrayList<String>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_REPORTS_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(EXTENSION_GENERATOR))
				// report generator detected
				templates.add(e.getAttribute(EXTENSION_GENERATOR_NAME));
		}

		Collections.sort(templates);

		return templates;
	}

	/**
	 * Get an instance of a specific report generator.
	 *
	 * @param name
	 *            report generator name
	 * @return report generator instance
	 */
	public static IReportGenerator getReport(String name) {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_REPORTS_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(EXTENSION_GENERATOR)) {
				// report generator detected
				if (e.getAttribute(EXTENSION_GENERATOR_NAME).equals(name)) {
					try {
						final Object generator = e.createExecutableExtension(EXTENSION_GENERATOR_CLASS);
						if (generator instanceof IReportGenerator)
							return (IReportGenerator) generator;
					} catch (final CoreException e1) {
						// try next one
					}
				}
			}
		}

		return null;
	}
}
