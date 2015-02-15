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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ease.modules.unittest.components.TestEntity;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.ease.ui.tools.ToggleHandler;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleTestFilter extends ToggleHandler {

	private class Filter extends ViewerFilter {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {

			if (element instanceof TestEntity) {
				final TestStatus status = ((TestEntity) element).getStatus();
				return (status != TestStatus.PASS);
			}

			return true;
		}

		@Override
		public boolean isFilterProperty(final Object element, final String property) {
			return UnitTestView.TEST_STATUS_PROPERTY.equals(property);
		}
	}

	@Override
	protected void executeToggle(final ExecutionEvent event, final boolean checked) {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof UnitTestView) {

			if (checked) {
				final ViewerFilter[] filter = new ViewerFilter[] { new Filter() };

				((UnitTestView) part).getFileTreeViewer().setFilters(filter);
				((UnitTestView) part).getTableViewer().setFilters(filter);

			} else {
				((UnitTestView) part).getFileTreeViewer().setFilters(new ViewerFilter[0]);
				((UnitTestView) part).getTableViewer().setFilters(new ViewerFilter[0]);
			}
		}
	}
}
