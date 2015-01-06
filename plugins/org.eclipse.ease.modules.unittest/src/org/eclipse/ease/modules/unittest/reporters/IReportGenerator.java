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

import org.eclipse.ease.modules.unittest.components.TestSuite;

/**
 * Interface for a test report generator.
 */
public interface IReportGenerator {
	/**
	 * Creates report data as string.
	 *
	 * @param title
	 *            report title
	 * @param description
	 *            generic description
	 * @param testSuite
	 *            testuite data to be exported
	 * @return String containing full test report
	 */
	String createReport(final String title, final String description, final TestSuite testSuite);

	/**
	 * Returns the default file extension to be used for this kind of report without the preceeding dot (eg "txt").
	 *
	 * @return file extension to be used
	 */
	String getDefaultExtension();
}
