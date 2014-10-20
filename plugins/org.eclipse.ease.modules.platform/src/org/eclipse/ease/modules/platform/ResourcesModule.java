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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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

public class ResourcesModule extends AbstractScriptModule {

	/** Access modifier for read mode. */
	@WrapToScript
	public static final int READ = IFileHandle.READ;

	/** Access modifier for write mode. */
	@WrapToScript
	public static final int WRITE = IFileHandle.WRITE;

	/** Access modifier for append mode. */
	@WrapToScript
	public static final int APPEND = IFileHandle.APPEND;

	private static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	/**
	 * Get the workspace root.
	 *
	 * @return workspace root
	 */
	@WrapToScript
	public IWorkspaceRoot getWorkspace() {
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
	public IProject getProject(final String name) {
		return getWorkspace().getProject(name);
	}

	/**
	 * Create a new workspace project. Will create a new project if it now already exists. If creation fails, <code>null</code> is returned.
	 *
	 * @param name
	 *            name or project to create
	 * @return <code>null</code> or project
	 */
	@WrapToScript
	public IProject createProject(final String name) {
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
	 *            one of {@value #READ}, {@value #WRITE}, {@value #APPEND}
	 * @return file handle instance to be used for file modification commands
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle openFile(final Object location, @ScriptParameter(defaultValue = "1") final int mode) throws Exception {
		return getFileHandle(location, mode);
	}

	/**
	 * Close a file. Releases system resources bound by an open file.
	 *
	 * @param handle
	 *            handle to be closed
	 */
	@WrapToScript
	public void closeFile(final IFileHandle handle) {
		handle.close();
	}

	/**
	 * Read data from a file. To repeatedly read from a file retrieve a {@link IFileHandle} first using {@link #openFile(String, int)} and use the handle for
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
	 * Read a single line from a file. To repeatedly read from a file retrieve a {@link IFileHandle} first using {@link #openFile(String, int)} and use the
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
	 * created.
	 *
	 * @param location
	 *            file location
	 * @param data
	 *            data to be written
	 * @param mode
	 *            write mode (WRITE/APPEND)
	 * @return file handle to continue write operations
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle writeFile(final Object location, final String data, @ScriptParameter(defaultValue = "2") final int mode) throws Exception {
		final IFileHandle handle = getFileHandle(location, mode);

		if (handle != null)
			handle.write(data);

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
	 *            write mode (WRITE/APPEND)
	 * @return file handle to continue write operations
	 * @throws Exception
	 *             problems on file access
	 */
	@WrapToScript
	public IFileHandle writeLine(final Object location, final String data, @ScriptParameter(defaultValue = "2") final int mode) throws Exception {
		final IFileHandle handle = getFileHandle(location, mode);

		if (handle != null)
			handle.write(data + LINE_DELIMITER);

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
	 * workspace root folder is used by default. When type is set to WRITE or APPEND a save dialog will be shown instead of the default open dialog.
	 *
	 * @param rootFolder
	 *            root folder path to use
	 * @param type
	 *            dialog type to use (WRITE|APPEND for save dialog, other for open dialog)
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

						if (dialog.open() == Window.OK)
							setResult("workspace:/" + ((IPath) dialog.getFirstResult()).toPortableString());
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
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String title, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String message) {

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
}
