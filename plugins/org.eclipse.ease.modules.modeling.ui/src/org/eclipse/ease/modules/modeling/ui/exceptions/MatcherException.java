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
package org.eclipse.ease.modules.modeling.ui.exceptions;

/**
 * Exception occuring during matching operation
 * 
 * @author a185132
 *
 */
public class MatcherException extends Exception {

	public MatcherException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
