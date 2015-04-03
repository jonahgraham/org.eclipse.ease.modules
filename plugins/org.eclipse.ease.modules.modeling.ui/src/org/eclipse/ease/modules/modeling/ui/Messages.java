/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
	public static String AttributeMatcher_HELP_ATTRIBUTE;
	public static String AttributeMatcher_INCORRECT_PATTERN;
	public static String AttributeMatcher_TEXT_COMBO_ATTRIBUTE;
	public static String AttributeMatcher_UNRECOGNIZED_PATTERN;
	public static String IdMatcher_COMBO_TEXT_ID;
	public static String IdMatcher_HELP_ID;
	public static String ModelRefactoringView_ELEMENT;
	public static String ModelRefactoringView_NAVIGATION;
	public static String ModelRefactoringView_NB_ELEMENTS_FOUND;
	public static String ModelRefactoringView_NO_ELEMENTS;
	public static String ModelRefactoringView_PATH;
	public static String ModelRefactoringView_SEARCH;
	public static String ModelRefactoringView_SELECTION_ID;
	public static String OCLMatcher_COMBO_TEXT_OCL;
	public static String OCLMatcher_CONSTRAINT_INVALID;
	public static String OCLMatcher_HELP_OCL;
	public static String SelectionUtils_NO_SELECTION_FOUND;
	public static String TypeMatcher_COMBO_TEXT_TYPE;
	public static String TypeMatcher_HELP_TYPE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
