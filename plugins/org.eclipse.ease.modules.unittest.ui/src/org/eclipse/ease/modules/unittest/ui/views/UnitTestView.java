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
package org.eclipse.ease.modules.unittest.ui.views;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.modules.unittest.ITestListener;
import org.eclipse.ease.modules.unittest.components.Test;
import org.eclipse.ease.modules.unittest.components.TestComposite;
import org.eclipse.ease.modules.unittest.components.TestFile;
import org.eclipse.ease.modules.unittest.components.TestResult;
import org.eclipse.ease.modules.unittest.components.TestStatus;
import org.eclipse.ease.modules.unittest.components.TestSuite;
import org.eclipse.ease.modules.unittest.components.TestSuiteModel;
import org.eclipse.ease.modules.unittest.ui.Activator;
import org.eclipse.ease.modules.unittest.ui.sourceprovider.TestSuiteSource;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.ease.ui.tools.DecoratedLabelProvider;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.ExpandAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;

public class UnitTestView extends ViewPart implements ITestListener, IConsoleListener {
	public static final String VIEW_ID = "org.eclipse.ease.views.unittest";

	public static final String TEST_STATUS_PROPERTY = "test status";

	private static class Statistics {
		private final Map<Object, Integer> mCounters = new HashMap<Object, Integer>();

		public synchronized void updateCounter(final Object identifier, final int value) {
			if (mCounters.containsKey(identifier))
				mCounters.put(identifier, mCounters.get(identifier) + value);
			else
				mCounters.put(identifier, value);
		}

		public void reset() {
			mCounters.clear();
		}

		public synchronized int getCounter(final Object identifier) {
			if (mCounters.containsKey(identifier))
				return mCounters.get(identifier);

			return 0;
		}
	}

	private static final String XML_CURRENT_SUITE = "currentSuite";

	private static final Object STATISTICS_TESTFILES_FINISHED = "testFiles";

	private static final Object STATISTICS_TEST_ERROR = "test errors";

	private static final Object STATISTICS_TEST_FAILURE = "test failures";

	private static final Object STATISTICS_TEST_VALID = "valid tests";

	private static final Object STATISTICS_TESTFILE_COUNT = "testFile count";

	private static final Object STATISTICS_TEST_FINISHED = "test count";

	private Table ftable;
	private ProgressBar fProgressBar;
	private TreeViewer fFileTreeViewer;
	private TableViewer fTestTableViewer;
	private SashForm sashForm;

	private int[] fSashWeights = new int[] { 70, 30 };
	private TableColumn tblclmnType;
	private TableViewerColumn tableViewerColumn1;
	private TableColumn tblclmnMessage;
	private TableViewerColumn tableViewerColumn2;
	private Composite composite1;

	private IMemento mMemento;

	private CollapseAllHandler mCollapseAllHandler;

	private ExpandAllHandler mExpandAllHandler;

	private Label lblTimeLeftText;

	private LocalResourceManager fResourceManager;

	private final UpdateUI fUIUpdater = new UpdateUI();
	private final Statistics fStatistics = new Statistics();

	private Label lblErrorCount;
	private Label lblFailureCount;

	private SuiteRuntimeInformation fRuntimeInformation = null;
	private Label lblTimeLeft;
	private ScriptConsole fConsole = null;

	public UnitTestView() {
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		mMemento = memento;
		super.init(site, memento);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(8, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		fResourceManager = new LocalResourceManager(JFaceResources.getResources(), composite);

		final Label lblErrorIcon = new Label(composite, SWT.NONE);
		final GridData gdLblErrorIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblErrorIcon.horizontalIndent = 50;
		lblErrorIcon.setLayoutData(gdLblErrorIcon);
		lblErrorIcon.setImage(fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_ERROR)));

		final Label lblErrors = new Label(composite, SWT.NONE);
		lblErrors.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblErrors.setAlignment(SWT.CENTER);
		lblErrors.setText("Errors:");

		lblErrorCount = new Label(composite, SWT.NONE);
		final GridData gd_lblErrorCount = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblErrorCount.horizontalIndent = 20;
		lblErrorCount.setLayoutData(gd_lblErrorCount);

		final Label lblFailureIcon = new Label(composite, SWT.NONE);
		final GridData gdLblFailureIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblFailureIcon.horizontalIndent = 50;
		lblFailureIcon.setLayoutData(gdLblFailureIcon);
		lblFailureIcon.setImage(fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_FAILURE)));

		final Label lblFailures = new Label(composite, SWT.NONE);
		lblFailures.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFailures.setAlignment(SWT.CENTER);
		lblFailures.setText("Failures:");

		lblFailureCount = new Label(composite, SWT.NONE);
		final GridData gdLblFailureCount = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gdLblFailureCount.horizontalIndent = 20;
		lblFailureCount.setLayoutData(gdLblFailureCount);

		lblTimeLeftText = new Label(composite, SWT.NONE);
		final GridData gdLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLabel.horizontalIndent = 50;
		lblTimeLeftText.setLayoutData(gdLabel);
		lblTimeLeftText.setText("Time left: ");
		lblTimeLeftText.setVisible(false);

		lblTimeLeft = new Label(composite, SWT.NONE);
		final GridData gdLblTimeLeft = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLblTimeLeft.horizontalIndent = 20;
		lblTimeLeft.setLayoutData(gdLblTimeLeft);
		lblTimeLeft.setVisible(false);

		fProgressBar = new ProgressBar(parent, SWT.NONE);
		fProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashForm.setOrientation(SWT.VERTICAL);

		fFileTreeViewer = new TreeViewer(sashForm, SWT.BORDER | SWT.MULTI);
		fFileTreeViewer.setContentProvider(new TestSuiteContentProvider());

		fFileTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public final int category(final Object element) {
				if (element instanceof TestFile)
					return 1;

				return 0;
			}
		});

		// use a decorated label provider
		final LabelProvider provider = new TestSuiteLabelProvider(fResourceManager);
		fFileTreeViewer.setLabelProvider(new DecoratedLabelProvider(provider));

		fFileTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				try {
					final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (element instanceof TestFile) {
						final Object file = ((TestFile) element).getFile();
						if (file instanceof IFile)
							UIModule.showEditor((IFile) ((TestFile) element).getFile());

					} else if (element instanceof TestSuite)
						UIModule.showEditor(((TestSuite) element).getModel().getFile());

				} catch (final PartInitException e) {
					// TODO handle this exception (but for now, at least know it happened)
					throw new RuntimeException(e);

				}
			}
		});
		fFileTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final ITreeSelection selection = (ITreeSelection) event.getSelection();
				final Object element = selection.getFirstElement();

				if (element instanceof TestComposite) {
					// test set selected
					fTestTableViewer.setInput(element);

					if (sashForm.getWeights()[1] == 0)
						sashForm.setWeights(fSashWeights);

					fTestTableViewer.refresh();

				} else {
					// test container selected, or no selection at all
					fTestTableViewer.setInput(null);

					if (sashForm.getWeights()[1] != 0)
						fSashWeights = sashForm.getWeights();

					sashForm.setWeights(new int[] { 100, 0 });
				}
			}
		});

		composite1 = new Composite(sashForm, SWT.NONE);
		final TableColumnLayout layout = new TableColumnLayout();
		composite1.setLayout(layout);

		fTestTableViewer = new TableViewer(composite1, SWT.BORDER | SWT.FULL_SELECTION);
		ftable = fTestTableViewer.getTable();
		fTestTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (element instanceof Test) {

					List<IScriptDebugFrame> trace;
					final List<TestResult> messages = ((Test) element).getMessages();
					if ((messages != null) && (!messages.isEmpty()))
						trace = messages.get(0).getStackTrace();
					else
						trace = ((Test) element).getTestLocation();

					if (trace != null) {
						// open trace location
						for (final IScriptDebugFrame traceElement : trace) {
							final Object file = traceElement.getScript().getFile();
							if (file instanceof IFile) {
								if (((IFile) file).exists()) {
									try {
										final int line = Math.max(traceElement.getLineNumber(), 1);
										final ITextEditor textEditor = (ITextEditor) UIModule.showEditor((IFile) file);
										final IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
										try {
											textEditor.selectAndReveal(document.getLineOffset(line - 1), document.getLineLength(line - 1));
										} catch (final BadLocationException e) {
											// TODO implement
											throw new RuntimeException(e);
										}
									} catch (final PartInitException e) {
										// TODO handle this exception (but for now, at least know it happened)
										throw new RuntimeException(e);

									}

									break;
								}
							}
						}

					} else {
						// we do not have a trace, open test set
						final Object input = fTestTableViewer.getInput();
						if (input instanceof TestFile) {

							try {
								final Object file = ((TestFile) input).getFile();
								if (file instanceof IFile)
									UIModule.showEditor((IFile) ((TestFile) input).getFile());

							} catch (final PartInitException e) {
								// TODO handle this exception (but for now, at least know it happened)
								throw new RuntimeException(e);

							}
						}
					}
				}
			}
		});
		ftable.setHeaderVisible(true);
		ftable.setLinesVisible(true);

		fTestTableViewer.setContentProvider(new TestFileContentProvider());
		// ColumnViewerToolTipSupport.enableFor(tableViewer,
		// ToolTip.NO_RECREATE);

		tableViewerColumn1 = new TableViewerColumn(fTestTableViewer, SWT.NONE);
		tblclmnType = tableViewerColumn1.getColumn();
		tblclmnType.setWidth(100);
		tblclmnType.setText("Test");
		layout.setColumnData(tblclmnType, new ColumnWeightData(30, 50, true));
		tableViewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Test)
					return ((Test) element).getTitle();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {

				TestStatus status = null;
				if (element instanceof Test)
					status = ((Test) element).getStatus();
				else if (element instanceof TestResult)
					status = ((TestResult) element).getStatus();

				if (status != null) {
					switch (status) {
					case PASS:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_PASS));
					case ERROR:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_ERROR));
					case FAILURE:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_FAILURE));
					case RUNNING:
						return fResourceManager.createImage(Activator.getImageDescriptor(Activator.ICON_RUNNING));
					default:
						return super.getImage(element);
					}
				}

				return super.getImage(element);
			}

			@Override
			public String getToolTipText(final Object element) {
				if (element instanceof Test) {
					if ((((Test) element).getDescription() != null) && (!((Test) element).getDescription().isEmpty()))
						return ((Test) element).getDescription();
				}

				return super.getToolTipText(element);
			}
		});

		tableViewerColumn2 = new TableViewerColumn(fTestTableViewer, SWT.NONE);
		tblclmnMessage = tableViewerColumn2.getColumn();
		tblclmnMessage.setWidth(100);
		tblclmnMessage.setText("Message");
		layout.setColumnData(tblclmnMessage, new ColumnWeightData(70, 50, true));
		tableViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Test) {
					final TestResult message = ((Test) element).getSeverestMessage();
					if (message != null)
						return message.getDescription();

					return ((Test) element).getDescription();
				}

				if (element instanceof TestResult)
					return ((TestResult) element).getDescription();

				return super.getText(element);
			}
		});
		ColumnViewerToolTipSupport.enableFor(fTestTableViewer, ToolTip.NO_RECREATE);

		sashForm.setWeights(new int[] { 1, 1 });

		// add context menu support
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(fFileTreeViewer.getTree());
		fFileTreeViewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuManager, fFileTreeViewer);

		final MenuManager menuManager2 = new MenuManager();
		final Menu menu2 = menuManager2.createContextMenu(fFileTreeViewer.getTree());
		fTestTableViewer.getTable().setMenu(menu2);
		getSite().registerContextMenu(menuManager2, fTestTableViewer);

		// add collapseAll/expandAll handlers
		final IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		mCollapseAllHandler = new CollapseAllHandler(fFileTreeViewer);
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, mCollapseAllHandler);
		mExpandAllHandler = new ExpandAllHandler(fFileTreeViewer);
		handlerService.activateHandler(ExpandAllHandler.COMMAND_ID, mExpandAllHandler);

		// RemarksContributionFactory.addContextMenu("com.infineon.views.javascript.unittest.testfile.remarks");
		menuManager.setRemoveAllWhenShown(true);

		// load last suite
		if (mMemento != null) {
			final IMemento currentSuiteNode = mMemento.getChild(XML_CURRENT_SUITE);
			if (currentSuiteNode != null) {
				final Path path = new Path(currentSuiteNode.getTextData());
				final IFile suiteFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				try {
					loadSuite(new TestSuite(new TestSuiteModel(suiteFile)));
				} catch (final Exception e) {
					// loading failed, ignore
				}
			}
		}

		// register for console events
		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(UnitTestView.this);

		final MultiSelectionProvider selectionProvider = new MultiSelectionProvider();
		selectionProvider.addSelectionProvider(fFileTreeViewer);
		selectionProvider.addSelectionProvider(fTestTableViewer);

		getSite().setSelectionProvider(selectionProvider);
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	public void collapseTests(final boolean collapse) {
		if (collapse)
			fFileTreeViewer.collapseAll();
		else
			fFileTreeViewer.expandAll();
	}

	public TreeViewer getTreeViewer() {
		return fFileTreeViewer;
	}

	private class UpdateUI extends UIJob {

		private final List<Object> fElements = new ArrayList<Object>();

		public UpdateUI() {
			super("Update Script Unit View");
		}

		public void addElement(final Object element) {
			synchronized (fElements) {
				if (fElements.isEmpty()) {
					// we might have added the same element again, so we cannot check for size() == 1 afterwards
					fElements.add(element);
					schedule(300);
				} else
					fElements.add(element);
			}
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			// create a local copy of elements so we can continue tests without waiting for the UI updater
			ArrayList<Object> localElements;
			synchronized (fElements) {
				localElements = new ArrayList<Object>(fElements);
				fElements.clear();
			}

			// update tree elements
			for (final Object element : localElements) {
				if (element instanceof TestComposite) {
					// update tree element and all its parents
					// Object node = element;
					// FIXME stalls UI thread in an endless loop
					// do {
					fFileTreeViewer.update(element, new String[] { TEST_STATUS_PROPERTY });
					// node = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(element);
					// } while (node != null);
				}
			}

			// update table
			if (fTestTableViewer.getInput() != null) {
				// update only if tableviewer is visible at all
				for (final Object element : localElements) {
					if (element instanceof Test) {
						final TestComposite testComposite = ((Test) element).getParent();

						if (fTestTableViewer.getInput().equals(testComposite)) {
							fTestTableViewer.refresh();
							// TODO scroll to last element

							// one refresh is enough for the whole table, so bail out
							break;
						}
					}
				}
			}

			// update statistics
			if (!fProgressBar.isDisposed()) {

				lblErrorCount.setText(Integer.toString(fStatistics.getCounter(STATISTICS_TEST_ERROR)));
				lblFailureCount.setText(Integer.toString(fStatistics.getCounter(STATISTICS_TEST_FAILURE)));
				lblFailureCount.getParent().layout();

				fProgressBar.setSelection(fStatistics.getCounter(STATISTICS_TESTFILES_FINISHED));
				lblTimeLeft.setText(getEstimatedTime());

				if ((fStatistics.getCounter(STATISTICS_TEST_ERROR) > 0) || (fStatistics.getCounter(STATISTICS_TEST_FAILURE) > 0))
					fProgressBar.setForeground(fResourceManager.createColor(new RGB(0xaa, 0, 0)));

				else if (fStatistics.getCounter(STATISTICS_TESTFILES_FINISHED) == fStatistics.getCounter(STATISTICS_TESTFILE_COUNT)) {
					if ((fStatistics.getCounter(STATISTICS_TEST_ERROR) == 0) && (fStatistics.getCounter(STATISTICS_TEST_FAILURE) == 0))
						fProgressBar.setForeground(fResourceManager.createColor(new RGB(0, 0xaa, 0)));
				}
			}

			synchronized (fElements) {
				if (!fElements.isEmpty())
					schedule(1000);
			}

			return Status.OK_STATUS;
		}

		private String getEstimatedTime() {
			if (getCurrentSuite().getStatus() != TestStatus.RUNNING) {
				lblTimeLeft.setVisible(false);
				lblTimeLeftText.setVisible(false);
				return "";
			}

			if (fRuntimeInformation != null) {
				final long time = fRuntimeInformation.getEstimatedTestTime();

				if (time < 0)
					return "calculating...";

				if (time < 60000)
					return new SimpleDateFormat("ss 'seconds'").format(time);

				if (time < 3600000)
					return new SimpleDateFormat("mm:ss").format(time);

				return new SimpleDateFormat("hh:mm:ss").format(time);
			}

			return "unknown";
		}
	}

	@Override
	public void notify(final Object testObject, final TestStatus status) {
		if ((testObject instanceof TestSuite) && (status == TestStatus.RUNNING)) {

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						loadSuite((TestSuite) testObject);
					} catch (final Exception e) {
						// TODO handle this exception (but for now, at least know it happened)
						throw new RuntimeException(e);
					}

					fStatistics.reset();
					fStatistics.updateCounter(STATISTICS_TESTFILE_COUNT, ((TestSuite) testObject).getActiveTestCount());

					// initialize progress bar
					fProgressBar.setMaximum(fStatistics.getCounter(STATISTICS_TESTFILE_COUNT));
					fProgressBar.setSelection(0);
					fProgressBar.setForeground(null);

					// create console
					if (fConsole == null)
						fConsole = ScriptConsole.create(testObject.toString(), null);

					// clear & attach to suite
					fConsole.clearConsole();
					fConsole.activate();
					((TestSuite) testObject).setOutputStream(fConsole.getOutputStream());
					((TestSuite) testObject).setErrorStream(fConsole.getErrorStream());

					// update estimated runtime
					lblTimeLeft.setVisible(true);
					lblTimeLeftText.setVisible(true);
				}
			});

		} else {
			// update statistics
			if ((status != TestStatus.RUNNING) && (status != TestStatus.NOT_RUN)) {
				// test finished

				if (testObject instanceof Test) {
					// do not track when this is a temporary tests in test count
					if (!((Test) testObject).isTransient())
						fStatistics.updateCounter(STATISTICS_TEST_FINISHED, 1);

					// do not track when this is a temporary test that passed
					if ((!((Test) testObject).isTransient()) || (!((Test) testObject).getMessages().isEmpty())) {

						switch (status) {
						case FAILURE:
							fStatistics.updateCounter(STATISTICS_TEST_FAILURE, 1);
							break;
						case ERROR:
							fStatistics.updateCounter(STATISTICS_TEST_ERROR, 1);
							break;
						case PASS:
							// do not track when global test file scope is valid
							fStatistics.updateCounter(STATISTICS_TEST_VALID, 1);
							break;
						default:
							// nothing to do
							break;
						}
					}

				} else if (testObject instanceof TestFile) {
					fStatistics.updateCounter(STATISTICS_TESTFILES_FINISHED, 1);
				}
			}
		}

		fUIUpdater.addElement(testObject);
	}

	public StructuredViewer getTableViewer() {
		return fTestTableViewer;
	}

	@Override
	public void saveState(final IMemento memento) {
		final TestSuite suite = getCurrentSuite();

		if (suite != null) {
			final IFile file = suite.getModel().getFile();

			if ((file != null) && (file.exists())) {
				// we finally detected the current test suite
				memento.createChild(XML_CURRENT_SUITE).putTextData(file.getFullPath().toString());
			}
		}

		super.saveState(memento);
	}

	public TestSuite getCurrentSuite() {
		final Object input = fFileTreeViewer.getInput();
		if (input instanceof Object[]) {
			if (((Object[]) input).length > 0) {
				final Object suite = ((Object[]) input)[0];
				if (suite instanceof TestSuite)
					return (TestSuite) suite;
			}
		}

		return null;
	}

	/**
	 * Loads a suite file and populates the treeview. Needs to be called from UIThread.
	 *
	 * @param suite
	 *            testSuite
	 * @return
	 * @throws IOException
	 *             cannot read from suite file
	 * @throws CoreException
	 *             invalid data within suite file
	 */
	public void loadSuite(final TestSuite suite) throws IOException, CoreException {
		// save current suite
		final TestSuite currentSuite = getCurrentSuite();
		if (!suite.equals(currentSuite)) {
			if (currentSuite != null) {
				Activator.getDefault().addRecentFile(currentSuite.getModel().getFile());

				// save timing information
				if (fRuntimeInformation != null)
					fRuntimeInformation.save();
			}

			fFileTreeViewer.setInput(new Object[] { suite });
			fRuntimeInformation = new SuiteRuntimeInformation(suite);

			// update source provider
			final TestSuiteSource instance = TestSuiteSource.getActiveInstance();
			if (instance != null)
				instance.setActiveSuite(suite);

			suite.addTestListener(this);

			// refresh console
			if (fConsole != null) {
				fConsole.terminate();
				fConsole = null;
			}
		}

		fFileTreeViewer.refresh();
		fFileTreeViewer.expandAll();
	}

	@Override
	public void dispose() {
		// unregister from console events
		ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);

		// dispose handlers
		mCollapseAllHandler.dispose();
		mExpandAllHandler.dispose();

		// save current suite
		final TestSuite currentSuite = getCurrentSuite();
		if (currentSuite != null)
			Activator.getDefault().addRecentFile(currentSuite.getModel().getFile());

		// save timing information
		if (fRuntimeInformation != null)
			fRuntimeInformation.save();

		super.dispose();
	}

	@Override
	public void consolesAdded(final IConsole[] consoles) {
		// nothing to do
	}

	@Override
	public void consolesRemoved(final IConsole[] consoles) {
		for (final IConsole console : consoles) {
			if (console.equals(fConsole))
				fConsole = null;
		}
	}
}
