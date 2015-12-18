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

package org.eclipse.ease.modules.charting.modules;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.charting.charts.Chart;
import org.eclipse.ease.modules.charting.views.ChartView;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class ChartingModule extends AbstractScriptModule {
	public static final String MODULE_NAME = "Charting";
	private Chart fChart = null;
	private static int fFigureIterator = 1;

	/**
	 * Opens a new view with an empty figure. <i>figureId</i> is used as a view and chart title. If a view with the same <i>figureId</i> already exists, it will
	 * be activated. The last activated figure will be used for all further commands of this modules.
	 *
	 * @param figureId
	 *            name of the figure to be created
	 * @return new or activated chart
	 * @throws PartInitException
	 *             when the view could not be initialized
	 */
	@WrapToScript
	public Chart figure(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String figureId) throws PartInitException {
		fChart = null;
		String secondaryId = (figureId == null) ? "Figure " + Integer.toString(fFigureIterator++) : figureId;
		ChartView view = (ChartView) UIModule.showView(ChartView.VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		view.setViewName(secondaryId);
		fChart = view.getChart();
		fChart.setPlotTitle(secondaryId);
		return fChart;
	}

	/**
	 * Series will be created with the name seriesName. If the name is not given then Series will have the name "Series id" where id is the current number of
	 * the Series Iterator. Series Iterator start at 1 and with every new Series is incremented by one. If Series is already created with seriesName then that
	 * series will be set as currently active Series and on that Series methods will be performed. If there is no active Figure then Figure will be created and
	 * activated.
	 *
	 * @param seriesName
	 *            Name of this series. Default is <code>null</code> if no seriesName is given, in that case the name is "Series id" as explained above
	 * @param format
	 *            default is "", if number is written inside format like "f#25" point size will be set(in this case 25), please write format correctly, if for
	 *            example 2 colors will be written like "rg" then the last one will be taken, in this case g or green, so please set line style, point size,
	 *            color and Marker Type only once. Used matlab syntax to define plot format:
	 *            <table cellspacing="0" class="body" cellpadding="4" border="2">
	 *
	 *            <tr valign="top">
	 *            <th valign="top">Specifier</th>
	 *            <th valign="top">LineStyle</th>
	 *            </tr>
	 *
	 *            <tr valign="top">
	 *            <td>'<tt>-</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Solid line (default)
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>--</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Dashed line
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>:</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Dotted line
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>-.</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Dash-dot line
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>#</tt>'</td>
	 *            <td>
	 *            <p>
	 *            No line
	 *            </p>
	 *            </td>
	 *            </tr>
	 *
	 *            </table>
	 *            <table cellspacing="0" class="body" cellpadding="4" border="2">
	 *
	 *            <tr valign="top">
	 *            <th>
	 *            <p>
	 *            Specifier
	 *            </p>
	 *            </th>
	 *            <th>
	 *            <p>
	 *            Color
	 *            </p>
	 *            </th>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>r</tt></td>
	 *            <td>
	 *            <p>
	 *            Red
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>g</tt></td>
	 *            <td>
	 *            <p>
	 *            Green
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>b</tt></td>
	 *            <td>
	 *            <p>
	 *            Blue
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>c</tt></td>
	 *            <td>
	 *            <p>
	 *            Cyan
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>m</tt></td>
	 *            <td>
	 *            <p>
	 *            Magenta
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>y</tt></td>
	 *            <td>
	 *            <p>
	 *            Yellow
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>k</tt></td>
	 *            <td>
	 *            <p>
	 *            Black
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td><tt>w</tt></td>
	 *            <td>
	 *            <p>
	 *            White
	 *            </p>
	 *            </td>
	 *            </tr>
	 *
	 *            </table>
	 *            <table cellspacing="0" class="body" cellpadding="4" border="2">
	 *            <tr valign="top">
	 *            <th>
	 *            <p>
	 *            Specifier
	 *            </p>
	 *            </th>
	 *            <th>
	 *            <p>
	 *            Marker Type
	 *            </p>
	 *            </th>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>+</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Plus sign
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>o</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Circle
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>p</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Point
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>x</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Cross
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>s</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Square
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>f</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Filled Square
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>d</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Diamond
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            <tr valign="top">
	 *            <td>'<tt>v</tt>'</td>
	 *            <td>
	 *            <p>
	 *            Downward-pointing triangle
	 *            </p>
	 *            </td>
	 *            </tr>
	 *            </table>
	 *
	 * @return series as Trace type to set different properties for this series
	 * @throws PartInitException
	 *             if the series could not be initialized
	 */
	@WrapToScript
	public Trace series(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String seriesName, @ScriptParameter(defaultValue = "") final String format)
			throws PartInitException {
		return getChart().series(seriesName, format);
	}

	/**
	 * Create new chart if possible and return it, if workbench is not working exception will be thrown.
	 */
	private Chart getChart() throws PartInitException {
		if (fChart == null)
			figure(null);

		return fChart;
	}

	/**
	 * Add (x,y) point to the last Series that is set with method series(seriesName,format). If there is no active figure and series then both will be created
	 * and activated.
	 *
	 * @param x
	 *            x coordinate of this point
	 * @param y
	 *            y coordinate of this point
	 * @return series as Trace type to set different properties for this series
	 */
	@WrapToScript
	public Trace plotPoint(final double x, final double y) throws PartInitException {
		return getChart().plot(x, y);
	}

	/**
	 * Plot array of points (x[],y[]) on the last series that is set with method series(seriesName, format). If there is no active Figure and Series then both
	 * will be created and activated.
	 *
	 * @param x
	 *            array of x coordinates
	 * @param y
	 *            array of y coordinates
	 * @return series as Trace type to set different properties for this series
	 */
	@WrapToScript
	public Trace plot(final double[] x, final double[] y) throws PartInitException {
		return getChart().plot(x, y);
	}

	/**
	 * Sets the title of the active chart.
	 *
	 * @param chartTitle
	 *            title to be set
	 * @return {@link XYGraph} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "title")
	public XYGraph setPlotTitle(final String chartTitle) throws PartInitException {
		if (fChart != null)
			return fChart.setPlotTitle(chartTitle);

		return null;
	}

	/**
	 * Set X axis name.
	 *
	 * @param label
	 *            label to be used for X axis
	 * @return {@link Axis} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "xlabel")
	public Axis setXLabel(final String label) throws PartInitException {
		if (fChart != null)
			return fChart.setXLabel(label);

		return null;
	}

	/**
	 * Set Y axis name.
	 *
	 * @param label
	 *            label to be used for Y axis
	 * @return {@link Axis} object or <code>null</code> if there is no active graph
	 */
	@WrapToScript(alias = "ylabel")
	public Axis setYLabel(final String label) throws PartInitException {
		if (fChart != null)
			return fChart.setYLabel(label);

		return null;
	}

	/**
	 * Set lower and upper limit of the X axis and the Y axis.
	 *
	 * @param xRange
	 *            Range from x Axis to be set, format is [xmin, xmax]
	 * @param yRange
	 *            Range from Y Axis to be set, format is [ymin, ymax]
	 */
	@WrapToScript(alias = "axis")
	public void setAxisRange(final double[] xRange, final double[] yRange) throws Exception {
		if (fChart != null)
			fChart.setAxisRange(xRange, yRange);
	}

	/**
	 * Activates or deactivates drawing of a background grid.
	 *
	 * @param showGrid
	 *            if <code>true</code> a grid will be displayed
	 */
	@WrapToScript
	public void showGrid(final boolean showGrid) throws PartInitException {
		if (fChart != null)
			fChart.showGrid(showGrid);
	}

	/**
	 * Activates or deactivates auto scaling whenever the cart is updated. An auto scale may also be triggered by double clicking right into the chart area.
	 *
	 * @param performAutoScale
	 *            if <code>true</code> auto scaling will be enabled
	 */
	@WrapToScript
	public void setAutoScale(final boolean performAutoScale) throws PartInitException {
		if (fChart != null)
			fChart.setAutoScale(performAutoScale);
	}

	/**
	 * Clears the active chart.
	 */
	@WrapToScript
	public void clear() throws PartInitException {
		if (fChart != null)
			fChart.clear();
	}

	/**
	 * Export the current figure as png file. When no <i>location</i> is provided, a popup dialog will ask the user for the target location.
	 *
	 * @param location
	 *            where to store image to. Accepts strings, {@link IFile} and {@link File} instances
	 * @param overwrite
	 *            Overwrite flag, if <code>true</code> file will be overwritten without question
	 * @throws Exception
	 */
	@WrapToScript
	public void exportGraph(@ScriptParameter(defaultValue = ScriptParameter.NULL) final Object location,
			@ScriptParameter(defaultValue = "false") final boolean overwrite) throws Exception {

		if (fChart != null) {
			Object resolvedLocation = (location != null) ? ResourceTools.resolveFile(location, getScriptEngine().getExecutedFile(), false) : null;
			getChart().export(resolvedLocation, overwrite);
		}
	}

	/**
	 * Deletes a given series. If the series does not exists this method does nothing.
	 *
	 * @param seriesName
	 *            name of series to be deleted
	 */
	@WrapToScript
	public void removeSeries(final String seriesName) throws PartInitException {
		getChart().removeSeries(seriesName);
	}
}
