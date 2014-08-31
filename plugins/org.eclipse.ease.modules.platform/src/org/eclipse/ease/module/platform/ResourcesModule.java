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
package org.eclipse.ease.module.platform;

import java.io.ByteArrayInputStream;
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

	@WrapToScript
	public static final int READ = IFileHandle.READ;

	@WrapToScript
	public static final int WRITE = IFileHandle.WRITE;

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
		IProject project = getProject(name);
		if (!project.exists()) {
			try {
				project.create(new NullProgressMonitor());
				project.open(new NullProgressMonitor());
			} catch (CoreException e) {
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
		Object folder = ResourceTools.resolveFolder(location, getScriptEngine().getExecutedFile(), false);
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
	 * @throws CoreException
	 * @throws IOException
	 */
	@WrapToScript
	public Object createFile(final Object location) throws CoreException, IOException {
		Object file = ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), false);

		if (file instanceof IFile) {
			if (!((IFile) file).exists()) {
				((IFile) file).create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
				return file;
			}

		} else if (file instanceof File) {
			if (!((File) file).exists())
				if (((File) file).createNewFile())
					return file;
		}

		return null;
	}

	/**
	 * Opens a file from the workspace or the file system. If the file does not exist and we open it for writing, the file is created automatically.
	 *
	 * @param location
	 *            file location
	 * @param mode
	 *            one of {@value #READ}, {@value #WRITE}, {@value #APPEND}
	 * @return file handle instance to be used for file modification commands
	 */
	@WrapToScript
	public IFileHandle openFile(final String location, @ScriptParameter(optional = true, defaultValue = "1") final int mode) {
		IFileHandle handle = getFileHandle(location, mode);

		if ((handle == null) && (mode != IFileHandle.READ)) {
			try {
				Object file = createFile(location);
				return getFileHandle(file, mode);
			} catch (Exception e) {
				// file did not exist and could not be created, giving up
			}
		}

		return handle;
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
	 * @throws IOException
	 */
	@WrapToScript
	public String readFile(final Object location, @ScriptParameter(optional = true, defaultValue = "-1") final int bytes) throws IOException {
		IFileHandle handle = getFileHandle(location, IFileHandle.READ);

		if (handle != null) {
			String result = handle.read(bytes);
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
	 * @throws IOException
	 */
	@WrapToScript
	public String readLine(final Object location) throws IOException {
		IFileHandle handle = getFileHandle(location, IFileHandle.READ);

		if (handle != null) {
			String result = handle.readLine();
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
	 * @return file handle to continue write operations
	 */
	@WrapToScript
	public IFileHandle writeFile(final Object location, final String data) {
		IFileHandle handle = getFileHandle(location, IFileHandle.WRITE);

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
	 * @return file handle to continue write operations
	 */
	@WrapToScript
	public IFileHandle writeLine(final Object location, final String data) {
		IFileHandle handle = getFileHandle(location, IFileHandle.WRITE);

		if (handle != null)
			handle.write(data + LINE_DELIMITER);

		return handle;
	}

	private IFileHandle getFileHandle(final Object location, final int mode) {
		if (location instanceof IFileHandle)
			return (IFileHandle) location;

		if (location instanceof File)
			return new FilesystemHandle((File) location, mode);

		if (location instanceof IFile)
			return new ResourceHandle((IFile) location, mode);

		if (location != null)
			return getFileHandle(ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), true), mode);

		return null;
	}

	/**
	 * Opens a file dialog. Depending on the <i>rootFolder</i> a workspace dialog or a file system dialog will be used. If the folder cannot be located, the
	 * workspace root folder is used by default. When type is set to WRITE or APPEND a save dialog will be shown instead of the default open dialog.
	 *
	 * @param rootFolder
	 *            root folder path to use
	 * @param type
	 *            dialog type to use (WRITE|APPEND for save dialog, other for open dialog)
	 * @return full path to selected file
	 */
	@WrapToScript
	public String showFileSelectionDialog(@ScriptParameter(optional = true, defaultValue = ScriptParameter.NULL) final Object rootFolder,
			@ScriptParameter(optional = true, defaultValue = "0") final int type) {

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

			RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), mode);
					dialog.setFilterPath(dialogRoot.getAbsolutePath());
					setResult(dialog.open());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();

		} else if (root instanceof IContainer) {
			// workspace
			final IContainer dialogRoot = (IContainer) root;

			RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					if ((type == WRITE) || (type == APPEND)) {
						// open a save as dialog
						SaveAsDialog dialog = new SaveAsDialog(Display.getDefault().getActiveShell());
						// set default filename if a subfolder is selected
						if (!dialogRoot.equals(getWorkspace()))
							dialog.setOriginalFile(dialogRoot.getFile(new Path("newFile")));

						if (dialog.open() == Window.OK)
							setResult("workspace:/" + dialog.getResult().toString());

					} else {
						// open a select file dialog

						final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(Display.getDefault().getActiveShell(),
								new WorkbenchLabelProvider(), new WorkbenchContentProvider());
						dialog.setTitle("Select file");
						dialog.setMessage("Select a file from the workspace:");
						dialog.setInput(dialogRoot);

						if (dialog.open() == Window.OK)
							setResult("workspace:/" + ((IFile) dialog.getFirstResult()).getFullPath().toPortableString());
					}
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}

		return null;
	}

	/**
	 * /** Opens a dialog box which allows the user to select a container (project or folder) in the workspace.
	 *
	 * @param title
	 *            the title to use for the dialog box
	 * @param message
	 *            a message to show in the dialog box
	 * @return array of selected objects
	 */
	@WrapToScript
	public String showFolderSelectionDialog(@ScriptParameter(optional = true, defaultValue = ScriptParameter.NULL) final Object rootFolder) {

		// FIXME currently we cannot resolve folders within the workspace
		// therefore this call fails when a subfolder is requested by the user

		Object root = ResourceTools.resolveFolder(rootFolder, getScriptEngine().getExecutedFile(), true);
		if (rootFolder == null)
			root = getWorkspace();

		if (root instanceof File) {

			final File dialogRoot = (File) root;

			RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell());
					dialog.setFilterPath(dialogRoot.getAbsolutePath());
					setResult(dialog.open());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();

		} else if (root instanceof IContainer) {
			// workspace
			final IContainer dialogRoot = (IContainer) root;

			RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

				@Override
				public void run() {
					ContainerSelectionDialog dialog = new ContainerSelectionDialog(Display.getDefault().getActiveShell(), null, true, null);
					dialog.setInitialSelections(new Object[] { dialogRoot });
					if (dialog.open() == Window.OK)
						setResult("workspace:/" + ((IContainer) dialog.getResult()[0]).getFullPath().toPortableString());
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}

		return null;
	}

	/**
	 * Return all the file of a workspace matching a pattern
	 *
	 * @param patternString
	 *            A pattern as define in {@link Pattern}
	 * @return An array of all the file into the workspace matching this pattern
	 */
	@WrapToScript
	public IFile[] findFiles(final String pattern, @ScriptParameter(optional = true, defaultValue = ScriptParameter.NULL) final Object rootFolder,
			@ScriptParameter(optional = true, defaultValue = "true") final boolean recursive) {
		Pattern regExp = Pattern.compile(pattern);

		List<IFile> result = new ArrayList<IFile>();
		Collection<IContainer> toVisit = new HashSet<IContainer>();

		// locate root folder to start with
		if (rootFolder == null)
			toVisit.add(ResourcesPlugin.getWorkspace().getRoot());

		// FIXME locate workspace folder, best create an exported method for it
		// else
		// toVisit.add(getFolder(rootFolder));

		do {
			IContainer container = toVisit.iterator().next();
			toVisit.remove(container);

			try {
				for (IResource child : container.members()) {
					if (child instanceof IFile) {
						if (regExp.matcher(child.getName()).matches())
							result.add((IFile) child);

					} else if ((recursive) && (child instanceof IContainer))
						toVisit.add((IContainer) child);
				}
			} catch (CoreException e) {
				// cannot parse container, skip and continue with next one
			}

		} while (!toVisit.isEmpty());

		return result.toArray(new IFile[result.size()]);
	}
}
