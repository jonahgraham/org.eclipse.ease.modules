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
package org.eclipse.ease.modules.unittest.ui.decorators;

import java.util.Collection;

import org.eclipse.ease.modules.unittest.components.TestComposite;
import org.eclipse.ease.modules.unittest.components.TestEntity;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.ease.modules.unittest.ui.views.UnitTestView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class TestDecorator implements ILightweightLabelDecorator {

	private static final String IMAGE_OK = "decorator_valid.gif";
	private static final String IMAGE_ERROR = "decorator_error.gif";
	private static final String IMAGE_FAILURE = "decorator_failure.png";
	private static final String IMAGE_RUNNING = "decorator_running.gif";

	@Override
	public void addListener(final ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return UnitTestView.TEST_STATUS_PROPERTY.equals(property);
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (element instanceof TestComposite) {
			final TestStatus status = ((TestComposite) element).getStatus();
			addOverlay(status, decoration);

			if (status != TestStatus.PASS) {
				final Collection<? extends TestEntity> testEntities = ((TestComposite) element).getChildren();
				int valid = 0;
				for (final TestEntity entity : testEntities) {
					if (entity.getStatus() == TestStatus.PASS)
						valid++;
				}

				// if (!element.isActive())
				// decoration.setForegroundColor(new Color(Display.getDefault(), 180, 180, 180));

				if ((testEntities.size() > 0) && (valid != testEntities.size()))
					decoration.addSuffix(" (" + valid + "/" + testEntities.size() + " valid)");
			}
		}
	}

	private ImageDescriptor getImage(final String image) {
		return Activator.getImageDescriptor("/images/" + image);
	}

	private void addOverlay(final TestStatus status, final IDecoration decoration) {
		switch (status) {
		case PASS:
			decoration.addOverlay(getImage(IMAGE_OK), IDecoration.BOTTOM_LEFT);
			break;
		case ERROR:
			decoration.addOverlay(getImage(IMAGE_ERROR), IDecoration.BOTTOM_LEFT);
			break;
		case FAILURE:
			decoration.addOverlay(getImage(IMAGE_FAILURE), IDecoration.BOTTOM_LEFT);
			break;
		case RUNNING:
			decoration.addOverlay(getImage(IMAGE_RUNNING), IDecoration.BOTTOM_LEFT);
			break;
		default:
			// nothing to do
			break;
		}
	}
}
