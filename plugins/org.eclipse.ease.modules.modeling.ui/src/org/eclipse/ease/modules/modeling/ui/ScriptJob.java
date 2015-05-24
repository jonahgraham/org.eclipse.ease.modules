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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ui.scripts.repository.IScript;

public class ScriptJob extends Job {

	private IScript script;

	public ScriptJob(IScript script) {
		super("Script " + script.getName());
		this.script = script;
		setUser(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		script.run();
		return Status.OK_STATUS;
	}

}
