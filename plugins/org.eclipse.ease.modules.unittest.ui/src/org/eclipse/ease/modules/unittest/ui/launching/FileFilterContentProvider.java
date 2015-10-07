package org.eclipse.ease.modules.unittest.ui.launching;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.ui.model.WorkbenchContentProvider;

public class FileFilterContentProvider extends WorkbenchContentProvider {

	private final List<String> fExtensions;

	public FileFilterContentProvider(final String[] extensions) {
		fExtensions = Arrays.asList(extensions);
	}

	@Override
	public Object[] getChildren(final Object element) {
		if (element instanceof IContainer)
			return (containsFiles((IContainer) element)) ? super.getChildren(element) : new Object[0];

		return super.getChildren(element);
	}

	private boolean containsFiles(final IContainer container) {
		try {
			for (IResource resource : container.members()) {
				// first parse files to avoid deep traversal
				if (resource instanceof IFile) {
					String fileExtension = resource.getFullPath().getFileExtension();
					if (fExtensions.contains(fileExtension))
						return true;
				}
			}

			// no file found, parse directories
			for (IResource resource : container.members()) {
				// first parse files to avoid deep traversal
				if (resource instanceof IContainer) {
					if (containsFiles((IContainer) resource))
						return true;
				}
			}
		} catch (CoreException e) {
			// traversal problem, better return too many folders instead of too few
			Logger.error(Activator.PLUGIN_ID, "Could not traverse " + container, e);
			return true;
		}

		return false;
	}
}
