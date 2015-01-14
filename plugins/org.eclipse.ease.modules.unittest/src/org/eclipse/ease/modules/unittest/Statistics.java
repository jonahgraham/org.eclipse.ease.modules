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
package org.eclipse.ease.modules.unittest;

import org.eclipse.ease.modules.unittest.components.TestEntity;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestSuite;

public class Statistics {

	private int mTestSets = 0;
	private int mTests = 0;
	private int mErrors = 0;
	private int mFailures = 0;
	private int mOverallTestFiles = 0;
	private int mValidCount = 0;

	public void reset() {
		mTestSets = 0;
		mTests = 0;
		mErrors = 0;
		mFailures = 0;
		mValidCount = 0;
	}

	public void incrementTestSetCount() {
		mTestSets++;
	}

	public void incrementTestCount() {
		mTests++;
	}

	public void incrementErrorCount() {
		mErrors++;
	}

	public void incrementFailureCount() {
		mFailures++;
	}

	public int getTestSetCount() {
		return mTestSets;
	}

	public int getOverallTestSetCount() {
		return mOverallTestFiles;
	}

	public int getTestCount() {
		return mTests;
	}

	public int getErrorCount() {
		return mErrors;
	}

	public int getFailureCount() {
		return mFailures;
	}

	public void setOverallTestFileCount(final int sets) {
		mOverallTestFiles = sets;
	}

	public int getPassCount() {
		return mValidCount;
	}

	public void load(final TestSuite testSuite) {
		reset();

		mOverallTestFiles += testSuite.getChildren().size();

		for (final TestFile testFile : testSuite.getChildren()) {
			for (final TestEntity test : testFile.getChildren()) {
				switch (test.getStatus()) {
				case PASS:
					incrementValidCount();
					break;
				case ERROR:
					incrementErrorCount();
					break;
				case FAILURE:
					incrementFailureCount();
					break;
				default:
					break;
				}
			}
		}
	}

	public void incrementValidCount() {
		mValidCount++;
	}
}
