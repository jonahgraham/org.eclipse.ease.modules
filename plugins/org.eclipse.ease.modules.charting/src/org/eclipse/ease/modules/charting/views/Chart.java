/*******************************************************************************
 * Copyright (c) 2015 Domjan Sansovic and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Domjan Sansovic - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.charting.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

public class Chart extends Composite {

	private final XYGraph fXYGraph;
	private final List<Trace> fTraces = new ArrayList<Trace>();
	private final List<CircularBufferDataProvider> fTraceDataProviders = new ArrayList<CircularBufferDataProvider>();
	private boolean fPerformAutoScale = true;
	private int fIndex = -1;
	private int fSeriesCounter = 1;

	public Chart(final Composite parent, final int style) {
		super(parent, style);
		final GridLayout layout = new GridLayout();
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayout(layout);
		final Canvas myCanvas = new Canvas(this, style);
		myCanvas.setLayoutData(gd);
		final LightweightSystem lws = new LightweightSystem(myCanvas);
		fXYGraph = new XYGraph();
		fXYGraph.setTitle("Chart");
		fXYGraph.primaryXAxis.setShowMajorGrid(true);
		fXYGraph.primaryYAxis.setShowMajorGrid(true);
		fXYGraph.setZoomType(ZoomType.DYNAMIC_ZOOM);
		fXYGraph.getPlotArea().addMouseListener(new MouseListener() {

			@Override
			public void mousePressed(final MouseEvent me) {
			}

			@Override
			public void mouseReleased(final MouseEvent me) {
			}

			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				fXYGraph.performAutoScale();
			}
		});
		lws.setContents(fXYGraph);
		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseScrolled(final org.eclipse.swt.events.MouseEvent e) {
				final IFigure figureUnderMouse = fXYGraph.findFigureAt(e.x, e.y, new TreeSearch() {

					@Override
					public boolean prune(final IFigure figure) {
						return false;
					}

					@Override
					public boolean accept(final IFigure figure) {
						return (figure instanceof Axis) || (figure instanceof PlotArea);
					}
				});
				if (figureUnderMouse instanceof Axis) {
					final Axis axis = ((Axis) figureUnderMouse);
					final double valuePosition = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
					axis.zoomInOut(valuePosition, (e.count * 0.1) / 3);
				} else if (figureUnderMouse instanceof PlotArea) {
					final PlotArea plotArea = (PlotArea) figureUnderMouse;
					plotArea.zoomInOut(true, true, e.x, e.y, (e.count * 0.1) / 3);
				}
			}
		});
	}

	private void getTraceIndex(final String traceName) {
		boolean findTrace = false;
		fIndex = 0;
		for (final Trace trace : fTraces) {
			if (trace.getName().equals(traceName)) {
				findTrace = true;
				break;
			}
			fIndex++;
		}
		if (!findTrace) {
			final CircularBufferDataProvider newTraceDataProvider = new CircularBufferDataProvider(false);
			newTraceDataProvider.setBufferSize(1000);
			fTraceDataProviders.add(newTraceDataProvider);
			final Trace currentTrace = new Trace(traceName, fXYGraph.primaryXAxis, fXYGraph.primaryYAxis, newTraceDataProvider);
			currentTrace.setTraceType(TraceType.SOLID_LINE);
			currentTrace.setPointStyle(PointStyle.XCROSS);
			currentTrace.setPointSize(5);
			fTraces.add(currentTrace);
			fXYGraph.addTrace(currentTrace);
		}
	}

	public Trace plot(final double x, final double y) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (fIndex == (-1))
					getTraceIndex("Series " + Integer.toString(fSeriesCounter++));
				plotPoint(x, y);
			}
		});
		return fTraces.get(fIndex);
	}

	private void plotPoint(final double x, final double y) {
		fTraceDataProviders.get(fIndex).addSample(new Sample(x, y));
		if (fPerformAutoScale)
			fXYGraph.performAutoScale();
	}

	private void setStyle(final Trace trace, final String format) {
		boolean doubleLine = false;

		for (final char ch : format.toCharArray()) {
			switch (ch) {
			case 'r':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
				break;
			case 'g':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_GREEN));
				break;
			case 'b':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));
				break;
			case 'c':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_CYAN));
				break;
			case 'm':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_PURPLE));
				break;
			case 'y':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_YELLOW));
				break;
			case 'k':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLACK));
				break;
			case 'w':
				trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_WHITE));
				break;
			case 'o':
				trace.setPointStyle(PointStyle.CIRCLE);
				break;
			case 'x':
				trace.setPointStyle(PointStyle.XCROSS);
				break;
			case '+':
				trace.setPointStyle(PointStyle.CROSS);
				break;
			case 's':
				trace.setPointStyle(PointStyle.SQUARE);
				break;
			case 'f':
				trace.setPointStyle(PointStyle.FILLED_SQUARE);
				break;
			case 'd':
				trace.setPointStyle(PointStyle.DIAMOND);
				break;
			case 'v':
				trace.setPointStyle(PointStyle.TRIANGLE);
				break;
			case 'p':
				trace.setPointStyle(PointStyle.POINT);
				break;
			case '-':
				if (doubleLine)
					trace.setTraceType(TraceType.DASH_LINE);
				else
					trace.setTraceType(TraceType.SOLID_LINE);
				doubleLine = true;
				break;
			case ':':
				trace.setTraceType(TraceType.DOT_LINE);
				break;
			case '.':
				trace.setTraceType(TraceType.DASHDOT_LINE);
				break;
			case '#':
				trace.setTraceType(TraceType.POINT);
				break;
			}
			final Pattern regex = Pattern.compile("(\\d+)");
			final Matcher regexMatcher = regex.matcher(format);
			if (regexMatcher.find()) {
				trace.setPointSize(Integer.parseInt(regexMatcher.group(1)));
			}
		}
	}

	public XYGraph setPlotTitle(final String title) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.setTitle(title);
			}
		});
		return fXYGraph;
	}

	public Axis setXLabel(final String title) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.primaryXAxis.setTitle(title);
			}
		});
		return fXYGraph.primaryXAxis;
	}

	public Axis setYLabel(final String title) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.primaryYAxis.setTitle(title);
			}
		});
		return fXYGraph.primaryYAxis;
	}

	public void setAxisRange(final double[] xrange, final double[] yrange) {
		if ((xrange.length != 2) || (yrange.length != 2))
			throw new IndexOutOfBoundsException();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.primaryXAxis.setRange(xrange[0], xrange[1]);
				fXYGraph.primaryYAxis.setRange(yrange[0], yrange[1]);
			}
		});
	}

	public void showGrid(final boolean showGrid) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.primaryXAxis.setShowMajorGrid(showGrid);
				fXYGraph.primaryYAxis.setShowMajorGrid(showGrid);
			}
		});
	}

	public void setAutoScale(final boolean performAutoScale) {
		fPerformAutoScale = performAutoScale;
	}

	public void zoom(final String zoomType) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fXYGraph.setZoomType(ZoomType.valueOf(zoomType));
			}
		});
	}

	public void export(final Object object, final boolean overwrite) throws Throwable {
		final RunnableWithResult<Object> runnable = new RunnableWithResult<Object>() {
			@Override
			public void runWithTry() throws Throwable {
				final ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { fXYGraph.getImage().getImageData() };
				boolean done = true;
				if (object != null) {
					// If the file already exists; asks for confirmation
					final MessageBox mb = new MessageBox(Display.getDefault().getShells()[0], SWT.ICON_WARNING | SWT.YES | SWT.NO);
					// We really should read this string from a
					// resource bundle
					mb.setText("Warning");
					// If they click Yes, we're done and we drop out. If
					// they click No File will not be saved
					if (object instanceof IFile) {
						final IFile file = (IFile) object;
						final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						loader.save(outStream, SWT.IMAGE_PNG);
						final ByteArrayInputStream stream = new ByteArrayInputStream(outStream.toByteArray());
						if (file.exists()) {
							if (!overwrite) {
								mb.setMessage(file.getName() + " already exists. Do you want to replace it?");
								done = (mb.open() == SWT.YES);
							}
							if (done)
								file.setContents(stream, 0, null);
						} else
							file.create(stream, 0, null);
						file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
					} else if (object instanceof File) {
						final File file = (File) object;
						if (file.exists()) {
							if (!overwrite) {
								mb.setMessage(file.getName() + " already exists. Do you want to replace it?");
								done = (mb.open() == SWT.YES);
							}
						}
						if (done)
							loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
					}

				}
				if ((object == null) || (!done)) {
					final FileDialog dialog = new FileDialog(Display.getDefault().getShells()[0], SWT.SAVE);
					dialog.setFilterNames(new String[] { "PNG Files", "All Files (*.*)" });
					dialog.setFilterExtensions(new String[] { "*.png", "*.*" }); // Windows
					final String path = dialog.open();
					if ((path != null) && (!path.equals(""))) {
						loader.save(path, SWT.IMAGE_PNG);
					}
				}
			}
		};
		Display.getDefault().syncExec(runnable);

		// simply fetch result to eventually trigger a thrown exception
		runnable.getResultFromTry();
	}

	public void setCursor(final String cursorName, final String traceName) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Trace currentTrace = null;
				for (final Trace trace : fTraces) {
					if (trace.getName().equals(traceName)) {
						currentTrace = trace;
						break;
					}
				}
				if (currentTrace == null)
					return;
				final Annotation annotation = new Annotation(cursorName, currentTrace);
				fXYGraph.addAnnotation(annotation);
			}
		});
	}

	public void removeCursor(final String cursorName) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Annotation currentAnnotation = null;
				for (final Annotation annotation : fXYGraph.getPlotArea().getAnnotationList()) {
					if (annotation.getName().equals(cursorName)) {
						currentAnnotation = annotation;
						break;
					}
				}
				if (currentAnnotation == null)
					return;
				fXYGraph.removeAnnotation(currentAnnotation);
			}
		});
	}

	public void removeSeries(final String traceName) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (final Trace trace : fTraces) {
					if (trace.getName().equals(traceName)) {
						fXYGraph.removeTrace(trace);
						fTraceDataProviders.remove((trace.getDataProvider()));
						fTraces.remove(trace);
						return;
					}
				}
			}
		});
	}

	public void clear() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (final Trace trace : fTraces) {
					fXYGraph.removeTrace(trace);
				}
				fTraceDataProviders.clear();
				fTraces.clear();
				fSeriesCounter = 1;
				fIndex = -1;
			}
		});
	}

	public Trace plot(final double[] x, final double[] y) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (fIndex == (-1))
					getTraceIndex("Series " + Integer.toString(fSeriesCounter++));
				plotArray(x, y);
			}
		});
		return fTraces.get(fIndex);
	}

	private void plotArray(final double[] x, final double[] y) {
		fTraceDataProviders.get(fIndex).setCurrentXDataArray(x);
		fTraceDataProviders.get(fIndex).setCurrentYDataArray(y);
		if (fPerformAutoScale)
			fXYGraph.performAutoScale();
	}

	public Trace series(final String seriesName, final String format) {
		final RunnableWithResult<Trace> runnable = new RunnableWithResult<Trace>() {

			@Override
			public void run() {
				final String traceName = (seriesName == null) ? "Series " + Integer.toString(fSeriesCounter++) : seriesName;
				getTraceIndex(traceName);
				setResult(fTraces.get(fIndex));
				setStyle(getResult(), format);
			}
		};

		Display.getDefault().syncExec(runnable);
		return runnable.getResult();
	}
}
