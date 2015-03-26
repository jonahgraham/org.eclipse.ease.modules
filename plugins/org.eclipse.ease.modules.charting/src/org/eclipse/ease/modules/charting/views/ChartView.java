/*******************************************************************************
 * Copyright (c) 2015 Domjan Sansovic and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Domjan Sansovic - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.charting.views;

import org.eclipse.ease.modules.charting.charts.Chart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ChartView extends ViewPart {
	private Chart chart;

	public ChartView() {
	}

	public static final String VIEW_ID = "org.eclipse.ease.modules.charting.views.ChartView";

	@Override
	public void setFocus() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		chart = new Chart(parent, SWT.NONE);
	}

	public Chart getChart() {
		return chart;
	}

	public void setViewName(String partName) {
		setPartName(partName);
	}
}
