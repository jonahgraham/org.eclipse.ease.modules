/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * Provides file access for workspace and file system resources. Methods accepting location objects can deal with {@link String}, {@link URI}, {@link IFile} and
 * {@link File} instances.
 */
public class ResourcesModule extends AbstractScriptModule {

	/** Module identifier. */
	public static final String MODULE_ID = "/System/Resources";

	/** Access modifier for read mode (1). */
	@WrapToScript
	public static final int READ = IFileHandle.READ;

	/** Access modifier for write mode (2). */
	@WrapToScript
	public static final int WRITE = IFileHandle.WRITE;

	/** Access modifier for append mode (4). */
	@WrapToScript
	public static final int APPEND = IFileHandle.APPEND;

	/**
	 * Monitor to wake up listeners when done.
	 */
	private class ProgressMonitor extends NullProgressMonitor {
		private boolean fDone = false;

		@Override
		public void done() {
			fDone = true;

			synchronized (this) {
				notifyAll();
			}
		}

		public boolean isDone() {
			return fDone;
		}
	}

	private static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	/**
	 * Get the workspace root.
	 *
	 * @return workspace root
	 */
	@WrapToScript
	public static IWorkspaceRoot getWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Get a project instance.
	 *
	 * @param name
	 *            project name
	 * @return project or <code>null</code>
	 */
	@WrapToScript
	public static IProject getProject(final String name) {
		return getWorkspace().getProject(name);
	}

	/**
	 * Get a workspace or file system file. Resolves relative and absolute file locations. Relative files are resolved against the current script file. If
	 * <i>exists</i> is <code>false</code> this method also returns files that do not exist yet. If <code>true</code> only existing instances are returned.
	 *
	 * @scriptExample getFile("workspace://my project/some folder/file.txt") to get the file.txt resource from the workspace
	 * @scriptExample getFile("project://some folder/file.txt") to get the file.txt resource as a project relative path
	 *
	 * @param location
	 *            file location/path to resolve
	 * @param exists
	 *            whether the resolved file needs to exist
	 * @return resolved {@link IFile} or {@link File} instance
	 */
	@WrapToScript
	public Object getFile(final String location, @ScriptParameter(defaultValue = "true") final boolean exists) {
		return ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), exists);
	}

	/**
	 * Create a new workspace project. Will create a new project if it now already exists. If creation fails, <code>null</code> is returned.
	 *
	 * @param name
	 *            name or project to create
	 * @return <code>null</code> or project
	 */
	@WrapToScript
	public static IProject createProject(final String name) {
		final IProject project = getProject(name);
		if (!project.exists()) {
			try {
				project.create(new NullProgressMonitor());
				project.open(new NullProgressMonitor());
			} catch (final CoreException e) {
				return null;
			}
		}

		return project;
	}

	/**
	 * Create a new folder in the workspace or the file system.
	 *
	 * @param location
	 *            folder location
	 * @return {@link IFolder}, {@link File} or <code>null</code> in case of error
	 * @throws CoreException
	 */
	@WrapToScript
	public Object createFolder(final Object location) throws CoreException {
		final Object folder = ResourceTools.resolveFolder(location, getScriptEngine().getExecutedFile(), false);
		if (folder instanceof IFolder) {
			if (!((IFolder) folder).exists()) {
				((IFolder) folder).create(true, true, new NullProgressMonitor());
				return folder;
			}

		} else if (folder instanceof File) {
			if (!((File) folder).exists())
				if (((File) folder).mkdirs())
					return folder;
		}

		return null;
	}

	/**
	 * Create a new file in the workspace or the file system.
	 *
	 * @param location
	 *            file location
	 * @return {@link IFile}, {@link File} or <code>null</code> in case of error
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public Object createFile(final Object location) throws Exception {
		final IFileHandle handle = getFileHandle(location, IFileHandle.WRITE);
		return handle.getFile();
	}

	/**
	 * Opens a file from the workspace or the file system. If the file does not exist and we open it for writing, the file is created automatically.
	 *
	 * @param location
	 *            file location
	 * @param mode
	 *            one of {@module #READ}, {@module #WRITE}, {@module #APPEND}
	 * @return file handle instance to be used for file modification commands
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle openFile(final Object location, @ScriptParameter(defaultValue = "1") final int mode) throws Exception {
		return getFileHandle(location, mode);
	}

	/**
	 * Verifies that a specific file exists.
	 *
	 * @param location
	 *            file location to verify
	 * @return <code>true</code> if file exists
	 */
	@WrapToScript
	public boolean fileExists(final Object location) {
		return ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), true) != null;
	}

	/**
	 * Close a file. Releases system resources bound by an open file.
	 *
	 * @param handle
	 *            handle to be closed
	 */
	@WrapToScript
	public static void closeFile(final IFileHandle handle) {
		handle.close();
	}

	/**
	 * Read data from a file. To repeatedly read from a file retrieve a {@link IFileHandle} first using {@module #openFile(String, int)} and use the handle for
	 * <i>location</i>.
	 *
	 * @param location
	 *            file location, file handle or file instance
	 * @param bytes
	 *            amount of bytes to read (-1 for whole file)
	 * @return file data or <code>null</code> if EOF is reached
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public String readFile(final Object location, @ScriptParameter(defaultValue = "-1") final int bytes) throws Exception {
		final IFileHandle handle = getFileHandle(location, IFileHandle.READ);

		if (handle != null) {
			final String result = handle.read(bytes);
			if (!(location instanceof IFileHandle))
				handle.close();

			return result;
		}

		throw new IOException("File \"" + location + "\" not found");
	}

	/**
	 * Copies a file from location to targetLocation.
	 *
	 * @param location
	 *            file location, file handle or file instance
	 * @param targetLocation
	 *            file location, file handle or file instance
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public void copyFile(final Object sourceLocation, final Object targetLocation) throws Exception {

		final IFileHandle handle = writeFile(targetLocation, readFile(sourceLocation, -1), IFileHandle.WRITE);
		if (handle != null)
			handle.close();
	}

	/**
	 * Delete a file from the workspace or local file system.
	 *
	 * @param source
	 *            file to be deleted
	 * @throws CoreException
	 *             on deletion errors
	 */
	@WrapToScript
	public void deleteFile(final Object source) throws CoreException {
		final Object file = ResourceTools.resolveFile(source, getScriptEngine().getExecutedFile(), true);
		if (file instanceof IFile)
			((IFile) file).delete(true, new NullProgressMonitor());

		else if ((file instanceof File) && (((File) file).isFile()))
			((File) file).delete();
	}

	/**
	 * Delete a folder from the workspace or local file system.
	 *
	 * @param source
	 *            folder to be deleted
	 * @throws CoreException
	 *             on deletion errors
	 */
	@WrapToScript
	public void deleteFolder(final Object source) throws CoreException {
		final Object folder = ResourceTools.resolveFolder(source, getScriptEngine().getExecutedFile(), true);
		if (folder instanceof IFolder)
			((IFolder) folder).delete(true, new NullProgressMonitor());

		else if ((folder instanceof File) && (((File) folder).isFile()))
			((File) folder).delete();
	}

	/**
	 * Delete a project from the workspace.
	 *
	 * @param source
	 *            project to be deleted
	 * @throws CoreException
	 *             on deletion errors
	 */
	@WrapToScript
	public void deleteProject(final Object source) throws CoreException {
		final Object project = ResourceTools.resolveFolder(source, getScriptEngine().getExecutedFile(), true);
		if (project instanceof IProject)
			((IProject) project).delete(true, new NullProgressMonitor());

		else if (source != null) {
			final IProject localProject = getProject(source.toString());
			if (localProject != null)
				localProject.delete(true, new NullProgressMonitor());
		}
	}

	/**
	 * Read a single line from a file. To repeatedly read from a file retrieve a {@link IFileHandle} first using {@module #openFile(String, int)} and use the
	 * handle for <i>location</i>.
	 *
	 * @param location
	 *            file location, file handle or file instance
	 * @return line of text or <code>null</code> if EOF is reached
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public String readLine(final Object location) throws Exception {
		final IFileHandle handle = getFileHandle(location, IFileHandle.READ);

		if (handle != null) {
			final String result = handle.readLine();
			if (!(location instanceof IFileHandle))
				handle.close();

			return result;
		}

		throw new IOException("File \"" + location + "\" not found");
	}

	/**
	 * Write data to a file. When not using an {@link IFileHandle}, previous file content will be overridden. Files that do not exist yet will be automatically
	 * created. After the write operation the file remains open. It needs to be closed explicitly using the {@module #closeFile(IFileHandle)} command
	 *
	 * @param location
	 *            file location
	 * @param data
	 *            data to be written
	 * @param mode
	 *            write mode ({@module #WRITE}/{@module #APPEND})
	 * @return file handle to continue write operations
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle writeFile(final Object location, final Object data, @ScriptParameter(defaultValue = "2") final int mode) throws Exception {
		final IFileHandle handle = getFileHandle(location, mode);

		if (handle != null) {
			if (data instanceof byte[])
				handle.write((byte[]) data);
			else if (data != null)
				handle.write(data.toString());

		} else
			throw new IOException("Could not access resource: " + location);

		return handle;
	}

	/**
	 * Write a line of data to a file. When not using an {@link IFileHandle}, previous file content will be overridden. Files that do not exist yet will be
	 * automatically created.
	 *
	 * @param location
	 *            file location
	 * @param data
	 *            data to be written
	 * @param mode
	 *            write mode ({@module #WRITE}/{@module #APPEND})
	 * @return file handle to continue write operations
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle writeLine(final Object location, final String data, @ScriptParameter(defaultValue = "2") final int mode) throws Exception {
		final IFileHandle handle = getFileHandle(location, mode);

		if (handle != null)
			handle.write(data + LINE_DELIMITER);
		else
			throw new IOException("Could not access resource: " + location);

		return handle;
	}

	private IFileHandle getFileHandle(final Object location, final int mode) throws Exception {
		IFileHandle handle = null;
		if (location instanceof IFileHandle)
			handle = (IFileHandle) location;

		else if (location instanceof File)
			handle = new FilesystemHandle((File) location, mode);

		else if (location instanceof IFile)
			handle = new ResourceHandle((IFile) location, mode);

		else if (location != null)
			handle = getFileHandle(ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), mode == IFileHandle.READ), mode);

		if ((handle != null) && (!handle.exists())) {
			// create file if it does not exist yet
			handle.createFile(true);
		}

		return handle;
	}

	/**
	 * Opens a file dialog. Depending on the <i>rootFolder</i> a workspace dialog or a file system dialog will be used. If the folder cannot be located, the
	 * workspace root folder is used by default. When type is set to {@module #WRITE} or {@module #APPEND} a save dialog will be shown instead of the default
	 * open dialog.
	 *
	 * @param rootFolder
	 *            root folder path to use
	 * @param type
	 *            dialog type to use ({@module #WRITE}/ {@module #APPEND} for save dialog, other for open dialog)
	 * @param title
	 *            dialog title
	 * @param message
	 *            dialog message
	 * @return full path to selected file
	 */
	@WrapToScript
	public String showFileSelectionDialog(@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object rootFolder,
			@ScriptParameter(defaultValue = "0") final int type, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String title,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String message) {

		Object root = ResourceTools.resolveFolder(rootFolder, getScriptEngine().getExecutedFile(), true);
		if (rootFolder == null)
			root = getWorkspace();

		if (root instanceof File) {
			// file system
			final int mode;
			switch (type) {
			case WRITE:
				// fall through
			case APPEND:
				mode = SWT.SAVE;
				break;
			default:
				mode = SWT.OPEN;
				break;
			}
			final File dialogRoot = (File) root;

			final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), mode);

					if (title != null)
						dialog.setText(title);

					dialog.setFilterPath(dialogRoot.getAbsolutePath());
					setResult(dialog.open());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();

		} else if (root instanceof IContainer) {
			// workspace
			final IContainer dialogRoot = (IContainer) root;

			final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					if ((type == WRITE) || (type == APPEND)) {
						// open a save as dialog
						final SaveAsDialog dialog = new SaveAsDialog(Display.getDefault().getActiveShell());
						// set default filename if a subfolder is selected
						if (!dialogRoot.equals(getWorkspace()))
							dialog.setOriginalFile(dialogRoot.getFile(new Path("newFile")));

						dialog.setTitle(title);
						dialog.setMessage(message);

						if (dialog.open() == Window.OK)
							setResult("workspace:/" + dialog.getResult().toPortableString());

					} else {
						// open a select file dialog

						final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(Display.getDefault().getActiveShell(),
								new WorkbenchLabelProvider(), new WorkbenchContentProvider());
						dialog.setTitle(title);
						dialog.setMessage(message);
						dialog.setInput(dialogRoot);
						dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

						if (dialog.open() == Window.OK)
							setResult("workspace:/" + ((IResource) dialog.getFirstResult()).getFullPath().toPortableString());
					}
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}

		return null;
	}

	/**
	 * Opens a dialog box which allows the user to select a container (project or folder). Workspace paths will always display the workspace as root object.
	 *
	 * @param rootFolder
	 *            root folder to display: for workspace paths this will set the default selection
	 * @param title
	 *            dialog title
	 * @param message
	 *            dialog message
	 *
	 * @return path to selected folder
	 */
	@WrapToScript
	public String showFolderSelectionDialog(@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object rootFolder,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String title,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String message) {

		Object root = ResourceTools.resolveFolder(rootFolder, getScriptEngine().getExecutedFile(), true);
		if (rootFolder == null)
			root = getWorkspace();

		if (root instanceof File) {

			final File dialogRoot = (File) root;

			final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					final DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell());

					if (title != null)
						dialog.setText(title);

					if (message != null)
						dialog.setMessage(message);

					dialog.setFilterPath(dialogRoot.getAbsolutePath());
					setResult(dialog.open());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();

		} else if (root instanceof IContainer) {
			// workspace
			final IContainer dialogRoot = (IContainer) root;

			final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					final ContainerSelectionDialog dialog = new ContainerSelectionDialog(Display.getDefault().getActiveShell(), dialogRoot, true, message);
					dialog.setTitle(title);

					if (dialog.open() == Window.OK)
						setResult("workspace:/" + ((IPath) dialog.getResult()[0]).toPortableString());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}

		return null;
	}

	/**
	 * Return files matching a certain pattern.
	 *
	 * @param pattern
	 *            search pattern: use * and ? as wildcards. If the pattern starts with '^' then a regular expression can be used.
	 * @param rootFolder
	 *            root folder to start your search from. <code>null</code> for workspace root
	 * @param recursive
	 *            searches subfolders when set to <code>true</code>
	 * @return An array of all matching files
	 */
	@WrapToScript
	public Object[] findFiles(final String pattern, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Object rootFolder,
			@ScriptParameter(defaultValue = "true") final boolean recursive) {

		// evaluate expression to look for
		Pattern regExp;
		if (!pattern.startsWith("^"))
			regExp = Pattern.compile(pattern.replace("*", ".*").replace('?', '.'));
		else
			regExp = Pattern.compile(pattern);

		final List<Object> result = new ArrayList<Object>();

		// locate root folder to start with
		Object root = ResourceTools.resolveFolder(rootFolder, getScriptEngine().getExecutedFile(), true);
		if (root == null)
			root = getWorkspace();

		if (root instanceof IContainer) {
			// search in workspace
			final Collection<IContainer> toVisit = new HashSet<IContainer>();
			toVisit.add((IContainer) root);

			do {
				final IContainer container = toVisit.iterator().next();
				toVisit.remove(container);

				try {
					for (final IResource child : container.members()) {
						if (child instanceof IFile) {
							if (regExp.matcher(child.getName()).matches())
								result.add(child);

						} else if ((recursive) && (child instanceof IContainer))
							toVisit.add((IContainer) child);
					}
				} catch (final CoreException e) {
					// cannot parse container, skip and continue with next one
				}

			} while (!toVisit.isEmpty());

		} else if (root instanceof File) {
			// search in file system
			final Collection<File> toVisit = new HashSet<File>();
			toVisit.add((File) root);

			do {
				final File container = toVisit.iterator().next();
				toVisit.remove(container);

				for (final File child : container.listFiles()) {
					if (child.isFile()) {
						if (regExp.matcher(child.getName()).matches())
							result.add(child);

					} else if ((recursive) && (child.isDirectory()))
						toVisit.add(child);
				}

			} while (!toVisit.isEmpty());
		}

		return result.toArray(new Object[result.size()]);
	}

	/**
	 * Links a project into the current workspace. Does not copy resources to the workspace.
	 *
	 * @param location
	 *            location of project (needs to contain <i>.project</i> file)
	 * @return link result
	 */
	@WrapToScript
	public boolean linkProject(final String location) {
		final Object resolvedLocation = ResourceTools.resolveFolder(location, getScriptEngine().getExecutedFile(), true);

		if (resolvedLocation instanceof IContainer) {
			Logger.warning(PluginConstants.PLUGIN_ID, "The folder to link is already part of the workspace: " + location);
			return false;

		} else if (resolvedLocation instanceof File) {
			final Path projectPath = new Path(((File) resolvedLocation).getAbsoluteFile() + File.separator + ".project");
			try {
				final IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
				final IProject project = getProject(description.getName());
				project.create(description, null);
				project.open(null);

				return true;
			} catch (final CoreException e) {
				Logger.error(PluginConstants.PLUGIN_ID, "Could not link to project", e);
				return false;
			}

		} else {
			Logger.warning(PluginConstants.PLUGIN_ID, "Could not resolve location: " + location);
			return false;
		}
	}

	/**
	 * Refresh a given resource and all its child elements.
	 *
	 * @scriptExample updateResource(getProject("my project")) to update the project and all its subfolders
	 * @param resource
	 *            {@link IFile}, {@link IFolder}, {@link IProject} or workspace root to update
	 * @throws CoreException
	 *             if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Resource changes are disallowed during certain types of resource change event notification. See {@link IResourceChangeEvent} for more
	 *             details.</li>
	 *             </ul>
	 */
	@WrapToScript
	public void refreshResource(final IResource resource) throws CoreException {

		final ProgressMonitor monitor = new ProgressMonitor();
		resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);

		while (!monitor.isDone()) {
			synchronized (monitor) {
				try {
					// check every second for completion
					System.out.println("waiting");
					monitor.wait(1000);
					System.out.println("wakeup");
				} catch (final InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Read from an InputStream into a string. Consumes an {@link InputStream} and stores all available data in a string. Usually a stream is only readable
	 * once.
	 *
	 * @param input
	 *            input stream to read from
	 * @return string content of stream.
	 * @throws IOException
	 *             on read error on the stream
	 */
	@WrapToScript
	public String readStream(final InputStream input) throws IOException {
		return ResourceTools.toString(input);
	}

	/**
	 * Create a problem marker on a file resource.
	 *
	 * @param severity
	 *            one of <i>error</i>/<i>warning</i>/<i>info</i>
	 * @param location
	 *            file resource to create marker for
	 * @param lineNumber
	 *            line number to set marker on
	 * @param message
	 *            message to be added to the marker
	 * @param type
	 *            marker type to create, needs to match an existing type
	 * @param permanent
	 *            <code>true</code> for permanent markers, <code>false</code> for temporary markers
	 * @throws CoreException
	 *             when marker cannot be created
	 */
	@WrapToScript
	public void createProblemMarker(final String severity, final Object location, final int lineNumber, final String message,
			@ScriptParameter(defaultValue = "org.eclipse.core.resources.problemmarker") final String type,
			@ScriptParameter(defaultValue = "true") final boolean permanent) throws CoreException {
		Object file = ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), true);

		if (file instanceof IFile) {

			int intSeverity = IMarker.SEVERITY_INFO;
			if ("error".equals(severity))
				intSeverity = IMarker.SEVERITY_ERROR;

			if ("warning".equals(severity))
				intSeverity = IMarker.SEVERITY_WARNING;

			final HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put(IMarker.LINE_NUMBER, lineNumber);
			attributes.put(IMarker.SEVERITY, intSeverity);
			attributes.put(IMarker.MESSAGE, message);
			attributes.put(IMarker.TRANSIENT, !permanent);

			MarkerUtilities.createMarker((IFile) file, attributes, type);
		}
	}
}
