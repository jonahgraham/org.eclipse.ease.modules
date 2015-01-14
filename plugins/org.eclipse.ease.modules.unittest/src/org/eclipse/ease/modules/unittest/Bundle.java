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

import org.eclipse.core.runtime.Platform;

public interface Bundle {
	String PLUGIN_ID = "org.eclipse.ease.modules.unittest";

	String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);
}
