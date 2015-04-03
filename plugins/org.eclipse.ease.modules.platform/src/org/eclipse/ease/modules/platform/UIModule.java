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

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Methods invoking code interfering with the UI thread.
 */
public class UIModule extends AbstractScriptModule {

	/**
	 * Run code in UI thread. Needed to interact with SWT elements. Might not be supported by some engines. Might be disabled by the user. Code will be executed
	 * synchronously and stall UI updates while executed.
	 *
	 * @param code
	 *            code/object to execute
	 * @return execution result
	 */
	@WrapToScript
	public Object executeUI(final Object code) {
		return getEnvironment().getScriptEngine().injectUI(code);
	}

	/**
	 * Returns <code>true</code> when executed in the UI thread.
	 *
	 * @return <code>true</code> in UI thread
	 */
	@WrapToScript
	public static boolean isUIThread() {
		return Thread.currentThread().equals(Display.getDefault().getThread());
	}

	/**
	 * Displays an info dialog. Needs UI to be available.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript(alias = "showMessageDialog")
	public static void showInfoDialog(final String message, @ScriptParameter(defaultValue = "Info") final String title) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	/**
	 * Displays a question dialog. Contains yes/no buttons. Needs UI to be available.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 * @return <code>true</code> when 'yes' was pressed, <code>false</code> otherwise
	 */
	@WrapToScript
	public static boolean showQuestionDialog(final String message, @ScriptParameter(defaultValue = "Question") final String title) {

		final RunnableWithResult<Boolean> runnable = new RunnableWithResult<Boolean>() {

			@Override
			public void run() {
				setResult(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, message));
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Displays an input dialog. Contains yes/no buttons. Needs UI to be available.
	 *
	 * @param message
	 *            dialog message
	 * @param initialValue
	 *            default value used to populate input box
	 * @param title
	 *            dialog title
	 * @return <code>true</code> when 'yes' was pressed, <code>false</code> otherwise
	 */
	@WrapToScript
	public static String showInputDialog(final String message, @ScriptParameter(defaultValue = "") final String initialValue,
			@ScriptParameter(defaultValue = "Information request") final String title) {

		final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

			@Override
			public void run() {
				final InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(), title, message, initialValue, null);
				if (dialog.open() == Window.OK)
					setResult(dialog.getValue());
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Displays a confirmation dialog.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 * @return <code>true</code> when accepted
	 */
	@WrapToScript
	public static boolean showConfirmDialog(final String message, @ScriptParameter(defaultValue = "Confirmation") final String title) {
		final RunnableWithResult<Boolean> runnable = new RunnableWithResult<Boolean>() {

			@Override
			public void run() {
				setResult(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), title, message));
			}
		};
		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Displays a warning dialog.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript
	public static void showWarningDialog(final String message, @ScriptParameter(defaultValue = "Warning") final String title) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	/**
	 * Displays an error dialog.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript
	public static void showErrorDialog(final String message, @ScriptParameter(defaultValue = "Error") final String title) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	/**
	 * Close the application. On unsaved editors user will be asked to save before closing.
	 */
	@WrapToScript
	public static void exitApplication() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				PlatformUI.getWorkbench().close();
			}
		});
	}

	/**
	 * Opens a view by given Name or id. When <i>name</i> does not match any known view id we try to match it with a view title. When found the view is opened.
	 * If the view is already visible it will be given focus.
	 *
	 * @param name
	 *            name or id of view to open
	 * @return view instance or <code>null</code>
	 * @throws PartInitException
	 *             when view cannot be created
	 */
	public static IViewPart showView(final String name) throws PartInitException {
		return showView(name, null, IWorkbenchPage.VIEW_ACTIVATE);
	}

	/**
	 * Shows a view in this page with the given id and secondary id. The Behavior of this method varies based on the supplied mode. If
	 * <code>VIEW_ACTIVATE</code> is supplied, the view is given focus. If <code>VIEW_VISIBLE</code> is supplied, then it is made visible but not given focus.
	 * Finally, if <code>VIEW_CREATE</code> is supplied the view is created and will only be made visible if it is not created in a folder that already contains
	 * visible views.
	 * <p>
	 * This allows multiple instances of a particular view to be created. They are disambiguated using the secondary id. If a secondary id is given, the view
	 * must allow multiple instances by having specified allowMultiple="true" in its extension.
	 * </p>
	 *
	 * @param viewId
	 *            the id of the view extension to use
	 * @param secondaryId
	 *            the secondary id to use, or <code>null</code> for no secondary id, Default is <code>null</code>
	 * @param mode
	 *            the activation mode. Must be {@link #VIEW_ACTIVATE}, {@link #VIEW_VISIBLE} or {@link #VIEW_CREATE}, Default is {@link #VIEW_ACTIVATE}
	 * @return a view
	 * @exception PartInitException
	 *                if the view could not be initialized
	 * @exception IllegalArgumentException
	 *                if the supplied mode is not valid
	 * @since 3.0
	 */
	@WrapToScript(alias = "openView")
	public static IViewPart showView(final String name, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String secondaryId,
			@ScriptParameter(defaultValue = "" + IWorkbenchPage.VIEW_ACTIVATE) final int mode) throws PartInitException {

		// find view ID
		final String viewID = getIDForName(name);

		if (viewID != null) {
			final RunnableWithResult<IViewPart> runnable = new RunnableWithResult<IViewPart>() {

				@Override
				public void run() {
					try {
						try {
							setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID, secondaryId, mode));
						} catch (final NullPointerException e) {
							if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0)
								setResult(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().showView(viewID, secondaryId, mode));
						}
					} catch (final PartInitException e) {
					}
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}

		return null;
	}

	/**
	 * Opens a file in an editor.
	 *
	 * @param location
	 *            file location to open, either {@link IFile} or workspace file location
	 * @return editor instance or <code>null</code>
	 * @throws PartInitException
	 *             when editor cannot be created
	 */
	@WrapToScript(alias = "openEditor")
	public IEditorPart showEditor(final Object location) throws PartInitException {
		final Object file = ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), true);
		if (file instanceof IFile)
			return showEditor((IFile) file);

		return null;
	}

	/**
	 * Opens a file in an editor.
	 *
	 * @param file
	 *            file location to open
	 *
	 * @return editor instance or <code>null</code>
	 * @throws PartInitException
	 *             when editor cannot be created
	 */
	public static IEditorPart showEditor(final IFile file) throws PartInitException {
		IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		if (descriptor == null)
			descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);

		if (descriptor != null) {
			final IEditorDescriptor editorDescriptor = descriptor;
			final RunnableWithResult<IEditorPart> runnable = new RunnableWithResult<IEditorPart>() {

				@Override
				public void run() {
					try {
						try {

							setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
									.openEditor(new FileEditorInput(file), editorDescriptor.getId()));
						} catch (final NullPointerException e) {
							if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0)
								setResult(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().openEditor(new FileEditorInput(file),
										editorDescriptor.getId()));
						}
					} catch (final PartInitException e) {
						// cannot handle that one, giving up
					}
				}
			};

			Display.getDefault().syncExec(runnable);

			return runnable.getResult();
		}

		return null;
	}

	/**
	 * Get the current selection. If <i>partID</i> is provided, the selection of the given part is returned. Otherwise the selection of the current active part
	 * is returned.
	 *
	 * @param name
	 *            name or ID of part to get selection from
	 * @return current selection
	 */
	@WrapToScript
	public static ISelection getSelection(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String name) {
		final ISelectionService selectionService = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getSelectionService();

		if ((name != null) && (!name.isEmpty())) {
			final String partID = getIDForName(name);
			if (partID != null)
				return selectionService.getSelection(partID);

			return null;
		}

		// current selection needs to be accessed from Display thread
		final RunnableWithResult<ISelection> runnable = new RunnableWithResult<ISelection>() {

			@Override
			public void run() {
				setResult(selectionService.getSelection());
			}
		};
		Display.getDefault().syncExec(runnable);
		return runnable.getResult();
	}

	/**
	 * Converts selection to a consumable form. Table/Tree selections are transformed into Object[], Text selections into the selected String.
	 *
	 * @param selection
	 *            selection to convert
	 * @return converted elements
	 */
	@WrapToScript
	public static Object convertSelection(final ISelection selection) {
		if (selection instanceof IStructuredSelection)
			return ((IStructuredSelection) selection).toArray();

		if (selection instanceof ITextSelection)
			return ((ITextSelection) selection).getText();

		return null;
	}

	/**
	 * Find ID for a given view name. If <i>name</i> already contains a valid id, it will be returned.
	 *
	 * @param name
	 *            name of view
	 * @return view ID or <code>null</code>
	 */
	private static String getIDForName(final String name) {
		String id = null;

		final IViewRegistry viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
		for (final IViewDescriptor descriptor : viewRegistry.getViews()) {
			if (descriptor.getId().equals(name)) {
				id = descriptor.getId();
				break;
			} else if (descriptor.getLabel().equals(name)) {
				id = descriptor.getId();
				// continue as we might have a match with an ID later
			}
		}

		return id;
	}

	/**
	 * Show a generic dialog.
	 *
	 * @param dialog
	 *            dialog to display
	 * @return result of dialog.open() method
	 */
	@WrapToScript
	public static int openDialog(final Window dialog) {
		final RunnableWithResult<Integer> run = new RunnableWithResult<Integer>() {

			@Override
			public void run() {
				setResult(dialog.open());
			}
		};
		Display.getDefault().syncExec(run);

		return run.getResult();
	}

	/**
	 * Get the workbench shell instance.
	 *
	 * @return shell
	 */
	@WrapToScript
	public static Shell getShell() {
		final RunnableWithResult<Shell> runnable = new RunnableWithResult<Shell>() {
			@Override
			public void run() {
				setResult(Display.getCurrent().getActiveShell());
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Get the active view instance.
	 *
	 * @return active view
	 */
	@WrapToScript
	public static IWorkbenchPart getActiveView() {
		final RunnableWithResult<IWorkbenchPart> runnable = new RunnableWithResult<IWorkbenchPart>() {
			@Override
			public void run() {
				setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Get the active editor instance.
	 *
	 * @return active editor
	 */
	@WrapToScript
	public static IEditorPart getActiveEditor() {
		final RunnableWithResult<IEditorPart> runnable = new RunnableWithResult<IEditorPart>() {
			@Override
			public void run() {
				setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}

	/**
	 * Write text data to the clipboard.
	 *
	 * @param data
	 *            data to write to the clipboard
	 */
	@WrapToScript
	public static void setClipboard(final String data) {
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final Clipboard clipboard = new Clipboard(Display.getDefault());
				clipboard.setContents(new Object[] { data }, new Transfer[] { TextTransfer.getInstance() });
			}
		};

		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Get text data from the clipboard.
	 *
	 * @return clipboard text
	 */
	@WrapToScript
	public static Object getClipboard() {
		final RunnableWithResult<Object> runnable = new RunnableWithResult<Object>() {
			@Override
			public void run() {
				final Clipboard clipboard = new Clipboard(Display.getDefault());
				setResult(clipboard.getContents(TextTransfer.getInstance()));
			}
		};

		Display.getDefault().syncExec(runnable);

		return runnable.getResult();
	}
}
