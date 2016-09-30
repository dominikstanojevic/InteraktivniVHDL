package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik on 29.9.2016..
 */
public class Graph {
	private double startTime;
	private VHDLComponent.Input signal;
	private XYSeries series;

	private JFreeChart chart;

	private static final Map<LogicValue, Integer> values = new HashMap<>();

	static {
		values.put(LogicValue.ZERO, 0);
		values.put(LogicValue.UNINITIALIZED, 1);
		values.put(LogicValue.ONE, 2);
	}

	public Graph(VHDLComponent.Input signal, long startTime) {
		this.startTime = (double) startTime / 1000;

		this.signal = signal;
		series = new XYSeries(signal.toString());


		XYSeriesCollection dataset = new XYSeriesCollection(series);
		chart = createChart(dataset);
		updateSignal(this.startTime / 1000);
	}

	private JFreeChart createChart(XYDataset dataset) {
		final JFreeChart result = ChartFactory.createXYLineChart(null, "Seconds", "Value",
				dataset);
		final XYPlot plot = result.getXYPlot();
		NumberAxis axis = new NumberAxis("Seconds");
		plot.setDomainAxis(axis);
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60.0);  // 1 minute

		axis.setNumberFormatOverride(new DecimalFormat("0"));


		SymbolAxis symbolAxis = new SymbolAxis("Value", new String[] {
				"0", "U", "1"});
		plot.setRangeAxis(symbolAxis);
		return result;
	}


	public void updateSignal(double time) {
		double diff = time - startTime;
		updateSignalWithDiffTime(diff);
	}

	public void updateSignalWithDiffTime(double time) {
		series.add(time, values.get(signal.getValue()));
	}

	public JFreeChart getChart() {
		return chart;
	}

	public Signal getSignal() {
		return signal.getSignal();
	}

	public void clearData() {
		series.clear();
	}
}
