/*******************************************************************************
 * Copyright (c) 2015 Dominic Pirker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dominic Pirker - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.svn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

public class SVNModule extends AbstractScriptModule {

	/**
	 * Creates repository location.
	 *
	 * Creates repository location with rooturl, if location already exists: return existing location
	 *
	 * @param rootUrl
	 *            defines the root URL of the repository
	 * @param username
	 *            registered username of the repository
	 * @param password
	 *            password depending to the username
	 * @return repository location which is created, or the existing repository location
	 */
	@WrapToScript
	public IRepositoryLocation createRepositoryLocation(final String rootUrl, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String username,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String password) {

		IRepositoryLocation[] locations = SVNRemoteStorage.instance().getRepositoryLocations();

		for (IRepositoryLocation location : locations) {
			if ((location.getUrlAsIs().equals(rootUrl)) || (location.getUrl().equals(rootUrl))) {
				return location;
			}
		}

		IRepositoryLocation location = SVNRemoteStorage.instance().newRepositoryLocation();

		location.setUrl(rootUrl);
		location.setTrunkLocation("trunk");
		location.setTagsLocation("tags");
		location.setBranchesLocation("branches");
		location.setStructureEnabled(true);

		if (username != null)
			location.setUsername(username);
		if (password != null)
			location.setPassword(password);

		location.setPasswordSaved(true);

		AddRepositoryLocationOperation operation = new AddRepositoryLocationOperation(location);
		final CompositeOperation op = new CompositeOperation(operation.getId(), operation.getMessagesClass());
		op.add(operation);
		op.add(new SaveRepositoryLocationsOperation());

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				UIMonitorUtility.doTaskNowDefault(op, false);
			}
		});

		return location;
	}

	/**
	 * Imports project from repository location.
	 *
	 * Imports project (given with projectLocation) from repository location.
	 *
	 * @param rootLocation
	 *            can be a string (-> generate RepositoryLocation automatically) or already a RepositoryLocation
	 * @param projectLocations
	 *            array from relative paths to project locations
	 */
	@WrapToScript
	public void importProjectFromSVN(Object rootLocation, final String[] projectLocations) throws Exception {
		if (rootLocation instanceof IRepositoryResource) {
		} else {
			rootLocation = createRepositoryLocation(rootLocation.toString(), null, null);
		}

		List<IRepositoryResource> doCeckout_tmp = new ArrayList<IRepositoryResource>();
		for (String location : projectLocations) {
			IRepositoryResource projectResource = SVNRemoteStorage.instance().asRepositoryResource((IRepositoryLocation) rootLocation,
					((IRepositoryLocation) rootLocation).getUrl() + "/" + location, false);
			doCeckout_tmp.add(projectResource);
		}

		final IRepositoryResource[] doCeckout = doCeckout_tmp.toArray(new IRepositoryResource[] {});

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				Shell sh = Display.getDefault().getActiveShell();
				IActionOperation op = ExtensionsManager.getInstance().getCurrentCheckoutFactory().getCheckoutOperation(sh, doCeckout, null, true, null,
						SVNDepth.INFINITY, false);
				UIMonitorUtility.doTaskNowDefault(op, true);
			}
		});
	}

	/**
	 * Get the revision for a given resource.
	 *
	 * @param resource
	 *            resource to get revision for
	 * @return revision number
	 */
	@WrapToScript
	public long getRevision(final Object resource) {
		IResource lookup = null;
		Object file = ResourceTools.resolveFile(resource, getScriptEngine().getExecutedFile(), true);
		if (file instanceof IResource)
			lookup = (IResource) file;

		else {
			Object folder = ResourceTools.resolveFolder(resource, getScriptEngine().getExecutedFile(), true);
			if (folder instanceof IContainer)
				lookup = (IResource) folder;
		}

		if (lookup != null) {
			ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(lookup);
			return localResource.getRevision();
		}

		return -1;
	}
}
