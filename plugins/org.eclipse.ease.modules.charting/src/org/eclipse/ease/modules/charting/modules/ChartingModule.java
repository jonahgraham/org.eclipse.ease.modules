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

import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.charting.charts.Chart;
import org.eclipse.ease.modules.charting.views.ChartView;
import org.eclipse.ease.modules.platform.ResourcesModule;
import org.eclipse.ease.modules.platform.UIModule;
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
	 * Shows a View in this page with the Empty Chart where figureId will be the name of the View and the Chart. Figure is actually View. If the Name is not
	 * given the name "Figure id" will be given where id is the current number of the Figure Iterator. Figure Iterator start at 1 and with every new Figure is
	 * incremented by one. If Figure with figureId already exist then that Figure will be show and set as active Figure.
	 * 
	 * @param figureId
	 *            Name of this Figure, Default is <code>null</code> if no figureId is given, in that case the name "figure id" will be set as explained above
	 * @return Chart as composite part of this view to set different properties of this view
	 * @throws PartInitException
	 *             if the view could not be initialized
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
	public Trace series(@ScriptParameter(defaultValue = ScriptParameter.NULL) String seriesName, @ScriptParameter(defaultValue = "") String format)
			throws PartInitException {
		return getChart().series(seriesName, format);
	}

	/**
	 * Create new chart if possible and return it, if workbench is not working exception will be thrown
	 */
	private Chart getChart() throws PartInitException {
		if (fChart == null)
			figure(null);
		return fChart;
	}

	/**
	 * Add (x,y) point to the last Series that is set with method series(seriesName,format). If there is no active Figure and Series then both will be created
	 * and activated.
	 *
	 * @param x
	 *            x coordinate of this point
	 * @param y
	 *            y coordinate of this point
	 * @return series as Trace type to set different properties for this series
	 */
	@WrapToScript
	public Trace plotPoint(double x, double y) throws PartInitException {
		return getChart().plot(x, y);
	}

	/**
	 * Plot array of points (x[],y[]) on the last Series that is set with method series(seriesName,format). If there is no active Figure and Series then both
	 * will be created and activated.
	 *
	 * @param x
	 *            array of x coordinates
	 * @param y
	 *            array of y coordinates
	 * @return series as Trace type to set different properties for this series
	 */
	@WrapToScript
	public Trace plot(double[] x, double[] y) throws PartInitException {
		return getChart().plot(x, y);
	}

	/**
	 * Set Graph Title. If there is no active Figure then Figure will be created and activated.
	 *
	 * @param plotTitle
	 *            Title to be set
	 * @return Graph as XYGraph type to set different properties for this Graph
	 */
	@WrapToScript(alias = "title")
	public XYGraph setPlotTitle(String plotTitle) throws PartInitException {
		return getChart().setPlotTitle(plotTitle);
	}

	/**
	 * Set X Axis Name.If there is no active Figure then Figure will be created and activated.
	 *
	 * @param xLabel
	 *            x Label to be set
	 * @return x Axis as Axis type to set different properties for this Axis
	 */
	@WrapToScript(alias = "xlabel")
	public Axis setXLabel(String xLabel) throws PartInitException {
		return getChart().setXLabel(xLabel);
	}

	/**
	 * Set Y Axis Name.If there is no active Figure then Figure will be created and activated.
	 *
	 * @param yLabel
	 *            y Label to be set
	 * @return y Axis as Axis type to set different properties for this Axis
	 */
	@WrapToScript(alias = "ylabel")
	public Axis setYLabel(String yLabel) throws PartInitException {
		return getChart().setYLabel(yLabel);
	}

	/**
	 * Set lower and upper limit of the X Axis and the Y Axis, right call of this function will be setAxisRange([xmin,xmax],[ymin,ymax]) where all parameters
	 * are double numbers.If there is no active Figure then Figure will be created and activated.
	 *
	 * @param xrange
	 *            Range from x Axis to be set, format is [xmin, xmax]
	 * @param yrange
	 *            Range from Y Axis to be set, format is [ymin, ymax]
	 */
	@WrapToScript(alias = "axis")
	public void setAxisRange(double[] xrange, double[] yrange) throws Exception {
		getChart().setAxisRange(xrange, yrange);
	}

	/**
	 * Show Grid if true.If there is no active Figure then Figure will be created and activated.
	 *
	 * @param showGrid
	 *            true - show Grid, false - disable grid
	 */
	@WrapToScript
	public void showGrid(boolean showGrid) throws PartInitException {
		getChart().showGrid(showGrid);
	}

	/**
	 * If this parameter is true after new point is added auto scale will be performed, auto scale is performed also with double click.If there is no active
	 * Figure then Figure will be created and activated.
	 * 
	 * @param performAutoScale
	 *            if true perform scale will be performed
	 */
	@WrapToScript
	public void setAutoScale(boolean performAutoScale) throws PartInitException {
		getChart().setAutoScale(performAutoScale);
	}

	/**
	 * Clear all series from Graph.If there is no active Figure then Figure will be created and activated.
	 */
	@WrapToScript
	public void clear() throws PartInitException {
		getChart().clear();
	}

	/**
	 * Export this graph as png file.If there is no active Figure then Figure will be created and activated.
	 * 
	 * @param imageName
	 *            Name of this image to be saved, if this parameter is not set or is empty ("") File Dialog will be opened
	 * @param overwrite
	 *            Overwrite flag, if true file will be overwritten without question, default is false
	 * @throws Exception
	 */
	@WrapToScript
	public void exportGraph(@ScriptParameter(defaultValue = ScriptParameter.NULL) String imageName, @ScriptParameter(defaultValue = "false") boolean overwrite)
			throws Exception {
		Object file = null;
		if (!((imageName == null) || imageName.trim().isEmpty()))
			file = getEnvironment().getModule(ResourcesModule.class).getFile(imageName, false);
		getChart().export(file, overwrite);
	}

	/**
	 * Remove Series.If there is no active Figure then Figure will be created and activated.
	 *
	 * @param seriesName
	 *            Name of this Series
	 */
	@WrapToScript
	public void removeSeries(String seriesName) throws PartInitException {
		getChart().removeSeries(seriesName);
	}

}
