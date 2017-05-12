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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Methods providing access to UI components. Allows to show dialogs, execute code in the UI thread, access views and editors.
 */
public class UIModule extends AbstractScriptModule {

	/** Module identifier. */
	public static final String MODULE_ID = "/System/UI";

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
	 * Displays an info dialog. Uses the engine output stream in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript(alias = "showMessageDialog")
	public void showInfoDialog(final String message, @ScriptParameter(defaultValue = "Info") final String title) {
		if (isHeadless())
			getEnvironment().print("INFO: " + message, true);
		else
			Display.getDefault().syncExec(() -> MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, message));
	}

	/**
	 * Displays a question dialog. Contains yes/no buttons. Uses the engine I/O streams in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 * @return <code>true</code> when 'yes' was pressed, <code>false</code> otherwise
	 */
	@WrapToScript
	public boolean showQuestionDialog(final String message, @ScriptParameter(defaultValue = "Question") final String title) {
		if (isHeadless()) {
			try {
				getEnvironment().print(message + " [Y/n])", false);
				final int character = getScriptEngine().getInputStream().read();
				if (Character.toLowerCase(character) == 'n')
					return false;

				return true;
			} catch (final IOException e) {
				// could not read from input
				return false;
			}

		} else {
			final RunnableWithResult<Boolean> runnable = new RunnableWithResult<Boolean>() {

				@Override
				public void run() {
					setResult(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, message));
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}
	}

	/**
	 * Displays an input dialog. Uses the engine I/O streams in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param initialValue
	 *            default value used to populate input box
	 * @param title
	 *            dialog title
	 * @return user input or <code>null</code> in case the user aborted/closed the dialog
	 */
	@WrapToScript
	public String showInputDialog(final String message, @ScriptParameter(defaultValue = "") final String initialValue,
			@ScriptParameter(defaultValue = "Information request") final String title) {
		if (isHeadless()) {
			try {
				getEnvironment().print(message, (initialValue == null));
				if (initialValue != null)
					getEnvironment().print("[" + initialValue + "]", true);

				final StringBuilder result = new StringBuilder();
				while (true) {
					final int character = getScriptEngine().getInputStream().read();
					if (character == -1)
						// EOF reached
						return null;

					if (Character.toLowerCase(character) == '\n')
						return result.toString();

					result.append((char) character);
				}

			} catch (final IOException e) {
				// could not read from input
				return null;
			}

		} else {
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
	}

	/**
	 * Displays a confirmation dialog. Uses the engine I/O streams in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 * @return <code>true</code> when accepted
	 */
	@WrapToScript
	public boolean showConfirmDialog(final String message, @ScriptParameter(defaultValue = "Confirmation") final String title) {

		if (isHeadless())
			return showQuestionDialog(message, title);

		else {
			final RunnableWithResult<Boolean> runnable = new RunnableWithResult<Boolean>() {

				@Override
				public void run() {
					setResult(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), title, message));
				}
			};
			Display.getDefault().syncExec(runnable);

			return runnable.getResult();
		}
	}

	/**
	 * Displays a warning dialog. Uses the engine output stream in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript
	public void showWarningDialog(final String message, @ScriptParameter(defaultValue = "Warning") final String title) {
		if (isHeadless())
			getEnvironment().print("WARNING: " + message, true);
		else
			Display.getDefault().syncExec(() -> MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, message));
	}

	/**
	 * Displays an error dialog. Uses the engine output stream in headless mode.
	 *
	 * @param message
	 *            dialog message
	 * @param title
	 *            dialog title
	 */
	@WrapToScript
	public void showErrorDialog(final String message, @ScriptParameter(defaultValue = "Error") final String title) {
		if (isHeadless())
			getEnvironment().print("ERROR: " + message, true);
		else
			Display.getDefault().syncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell(), title, message));
	}

	/**
	 * Close the application. On unsaved editors user will be asked to save before closing.
	 */
	@WrapToScript
	public static void exitApplication() {
		Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().close());
	}

	/**
	 * Opens a view by given Name or id. When <i>name</i> does not match any known view id we try to match it with a view title. When found the view is opened.
	 * If the view is already visible it will be given focus.
	 *
	 * @param name
	 *            name or id of view to open
	 * @return view instance or <code>null</code>
	 * @throws Throwable
	 *             when view cannot be created
	 */
	public static IViewPart showView(final String name) throws Throwable {
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
	 * @param name
	 *            either the id of the view extension to use or the visible name of the view (tab title)
	 * @param secondaryId
	 *            the secondary id to use, or <code>null</code> for no secondary id
	 * @param mode
	 *            the activation mode. Must be {@link #VIEW_ACTIVATE}, {@link #VIEW_VISIBLE} or {@link #VIEW_CREATE}, Default is {@link #VIEW_ACTIVATE}
	 * @return a view
	 * @throws Throwable
	 *             when the view cannot be opened
	 * @throws IllegalArgumentException
	 *             when the supplied mode is not valid
	 * @since 3.0
	 */
	@WrapToScript(alias = "openView")
	public static IViewPart showView(final String name, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String secondaryId,
			@ScriptParameter(defaultValue = "" + IWorkbenchPage.VIEW_ACTIVATE) final int mode) throws Throwable {

		// find view ID
		final String viewID = getIDForName(name);

		if (viewID != null) {
			final RunnableWithResult<IViewPart> runnable = new RunnableWithResult<IViewPart>() {

				@Override
				public void runWithTry() throws Throwable {
					try {
						setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID, secondaryId, mode));
					} catch (final NullPointerException e) {
						if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0)
							setResult(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().showView(viewID, secondaryId, mode));
					}
				}
			};

			Display.getDefault().syncExec(runnable);
			return runnable.getResultFromTry();
		}

		// maybe this view is already open, search for view titles

		for (final IViewReference part : PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].getViewReferences()) {
			if (part.getTitle().equals(name))
				return part.getView(false);
		}

		return null;
	}

	/**
	 * Opens a file in an editor.
	 *
	 * @param location
	 *            file location to open, either {@link IFile} or workspace file location
	 * @return editor instance or <code>null</code>
	 * @throws Throwable
	 *             when we cannot open the editor
	 */
	@WrapToScript(alias = "openEditor")
	public IEditorPart showEditor(final Object location) throws Throwable {
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
	 * @throws Throwable
	 *             when we cannot open the editor
	 */
	public static IEditorPart showEditor(final IFile file) throws Throwable {
		IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		if (descriptor == null)
			descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);

		if (descriptor != null) {
			final IEditorDescriptor editorDescriptor = descriptor;
			final RunnableWithResult<IEditorPart> runnable = new RunnableWithResult<IEditorPart>() {

				@Override
				public void runWithTry() throws Throwable {
					try {

						setResult(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file),
								editorDescriptor.getId()));
					} catch (final NullPointerException e) {
						if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0)
							setResult(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().openEditor(new FileEditorInput(file),
									editorDescriptor.getId()));
					}
				}
			};

			Display.getDefault().syncExec(runnable);

			return runnable.getResultFromTry();
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
				// this is a valid view ID
				return name;

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
		final Runnable runnable = () -> {
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { data }, new Transfer[] { TextTransfer.getInstance() });
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

	/**
	 * Clear the script console.
	 */
	@WrapToScript
	public void clearConsole() {
		final ScriptConsole console = getConsole();
		if (console != null)
			console.clearConsole();
	}

	/**
	 * Get the script console for the current engine.
	 *
	 * @return script console or <code>null</code>
	 */
	@WrapToScript
	public ScriptConsole getConsole() {
		final IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (final IConsole console : consoles) {
			if (console instanceof ScriptConsole) {
				if (getScriptEngine().equals(((ScriptConsole) console).getScriptEngine()))
					return (ScriptConsole) console;
			}
		}

		return null;
	}

	/**
	 * Maximize a dedicated view. If the view is not opened yet, it will be opened by this call. A second call will restore the original size of the view.
	 *
	 * @param name
	 *            visible name or id of view to maximize
	 * @throws Throwable
	 *             when view cannot be opened
	 */
	@WrapToScript
	public static void maximizeView(final String name) throws Throwable {
		final IViewPart view = showView(name);
		if (view != null)
			ActionFactory.MAXIMIZE.create(view.getViewSite().getWorkbenchWindow()).run();
	}

	/**
	 * Minimize a dedicated view. If the view is not opened yet, it will be opened by this call. A second call will restore view on a floating dock.
	 *
	 * @param name
	 *            name or id of view to minimize
	 * @throws Throwable
	 *             when view cannot be opened
	 */
	@WrapToScript
	public static void minimizeView(final String name) throws Throwable {
		final IViewPart view = showView(name);
		if (view != null)
			ActionFactory.MINIMIZE.create(view.getViewSite().getWorkbenchWindow()).run();
	}

	/**
	 * Close a dedicated view.
	 *
	 * @param name
	 *            visible name or id of view to close
	 * @param secondaryID
	 *            secondary ID of view to close
	 */
	@WrapToScript
	public static void closeView(final String name, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String secondaryID) {
		// find view ID
		final String viewID = getIDForName(name);

		final Runnable runnable = () -> {
			final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			for (final IViewReference part : activePage.getViewReferences()) {
				if (part.getId().equals(viewID)) {
					if ((secondaryID == null) || (secondaryID.equals(part.getSecondaryId()))) {
						activePage.hideView(part);
						return;
					}
				}
			}
		};

		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Shut down the application.
	 */
	@WrapToScript
	public static void shutdown() {
		Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().close());
	}

	/**
	 * Verify if we are running in headless mode.
	 *
	 * @return <code>true</code> if we are running without UI
	 */
	@WrapToScript
	public static boolean isHeadless() {
		return !PlatformUI.isWorkbenchRunning();
	}

	/**
	 * Constructs a new color given the desired red, green and blue values expressed as ints in the range 0 to 255 (where 0 is black and 255 is full
	 * brightness).
	 * <p>
	 * You must dispose the color when it is no longer required.
	 * </p>
	 *
	 * @param red
	 *            the amount of red in the color
	 * @param green
	 *            the amount of green in the color
	 * @param blue
	 *            the amount of blue in the color
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the red, green or blue argument is not between 0 and 255</li>
	 *                </ul>
	 * @return color instance
	 */
	@WrapToScript
	public Color createColor(int red, int green, int blue) {
		return new Color(Display.getDefault(), red, green, blue);
	}
}
