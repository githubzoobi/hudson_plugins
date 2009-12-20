package com.ikokoon.serenity.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.ikokoon.serenity.model.IModel;
import com.ikokoon.toolkit.Matrix;
import com.jinsight.jetchart.BarSerie;
import com.jinsight.jetchart.Graph;
import com.jinsight.jetchart.Java2Adapter;
import com.jinsight.jetchart.Legend;

public class JetChartApplet extends JApplet {

	private static final int WIDTH = 750;
	private static final int HEIGHT = 210;
	private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);

	private Graph graph = new Graph();
	@SuppressWarnings("unused")
	private Color blue = new Color(35, 102, 165);
	@SuppressWarnings("unused")
	private Color red = new Color(239, 41, 41);
	@SuppressWarnings("unused")
	private Color yellow = new Color(253, 183, 57);

	private Color color = new Color(35, 102, 165);
	private int width = 40;

	/** This is a dummy model for testing. */
	private IModel model = ModelFactory.getModel();

	/**
	 * Sets the model, presumably from JavaScript in the page.
	 * 
	 * @param matrix
	 *            the model in string form
	 */
	public void setModel(final String uri) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					int counter = 0;
					model = null;
					while (counter++ < 50 && model == null) {
						Thread.sleep(2);
						model = ModelFactory.getModel(getDocumentBase(), uri);
					}
					repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public synchronized void init() {
		setPreferredSize(dimension);
		try {
			graph = new Graph();
			graph.set3DEnabled(true);
			graph.set3DSeriesInLineEnabled(true);
			graph.setPreferredSize(dimension);
			graph.setBorder(new LineBorder(Color.black, 1));

			Java2Adapter ja = new Java2Adapter(graph);
			ja.setAntiAliasEnabled(true);

			setChartProperties(graph);

			getContentPane().add(graph, BorderLayout.CENTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void paint(final Graphics g) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				paint(model);
			}
		});
	}

	private void paint(IModel model) {
		graph.removeAllSeries();
		// Set the labels for the columns
		graph.setLabels(model.getLegend().toArray(new String[model.getLegend().size()]));
		// Create the value arrays from the metric arrays
		List<ArrayList<Double>> metrics = model.getMetrics();
		double[][] values = new double[metrics.size()][];
		for (int i = 0; i < metrics.size(); i++) {
			ArrayList<Double> series = metrics.get(i);
			values[i] = new double[series.size()];
			for (int j = 0; j < series.size(); j++) {
				values[i][j] = series.get(j);
			}
		}
		values = Matrix.inverse(values);
		for (int i = 0; i < values.length; i++) {
			BarSerie barSerie = new BarSerie(values[i]);
			barSerie.setColor(color);
			barSerie.setWidth(width);
			graph.addSerie(barSerie);
		}
		graph.setTitle(new String[] { model.getName() });
		graph.repaint();
	}

	@SuppressWarnings("deprecation")
	private void setChartProperties(Graph graph) {
		// Enables tooltip
		graph.getToolTip().setEnabled(true);
		// Sets labels title
		graph.getBottomTitle().setText("Metrics");
		// Sets values title
		graph.getLeftTitle().setText("Values");
		// Applies gradient colours to graphic context
		graph.setGradientColors(Color.white, new Color(104, 150, 200));
		// Enables chart and legend dragging. A double-click over chart alternates
		// between moving chart and scale adjustment.
		graph.setDraggingEnabled(false);
		// Enables grid and sets grid colour
		graph.getGraphSet(0).getGrid().setEnabled(true);
		// Sets legend orientation and position. Legend can be dynamically
		// dragged, if the Graph draggable property is set to true.
		graph.getLegend().setOrientation(Legend.HORIZONTAL);
		// graph.getLegend().setPosition(Legend.RIGHT);
		// graph.getLegend().setPosition(Legend.LEFT);
		// Sets format for values.
		graph.setValueFormat("###,###0.00");
		// Sets depth for vertical graph.
		graph.setVDepth(10);
		// Sets depth for horizontal graph.
		graph.setHDepth(10);
		// Disables in-line painting of 3D series, drawing bar series side by side.
		graph.set3DSeriesInLineEnabled(true);
	}

	public void update(Graphics g) {
		paint(g);
	}

}