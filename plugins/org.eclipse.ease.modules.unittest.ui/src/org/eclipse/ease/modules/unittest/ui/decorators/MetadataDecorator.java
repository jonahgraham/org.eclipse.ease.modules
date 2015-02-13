package org.eclipse.ease.modules.unittest.ui.decorators;

import org.eclipse.ease.modules.unittest.components.Test;
import org.eclipse.ease.modules.unittest.components.TestComposite;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class MetadataDecorator implements ILightweightLabelDecorator {

	private static final String IMAGE_METADATA = "metadata.png";

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
		return true;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (element instanceof TestComposite) {
			for (Test test : ((TestComposite) element).getTests()) {
				if (!test.getMetaData().isEmpty())
					decoration.addOverlay(TestDecorator.getImage(IMAGE_METADATA), IDecoration.TOP_RIGHT);
			}
		}
	}
}
