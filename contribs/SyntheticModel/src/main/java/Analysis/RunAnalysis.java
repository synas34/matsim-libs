package Analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class RunAnalysis {

	public static void main(String[] args) {
		String pathToEventsFile = "examples/scenarios/Odakyu1/output/output_events.xml.gz";

		ModeTrackerExample tracker = new ModeTrackerExample(pathToEventsFile);
		tracker.analyze();

		// Now retrieve mode counts and create bar chart
		Map<String, Integer> modeCounts = tracker.getModeCounts();
		createBarChart(modeCounts);
	}

	private static void createBarChart(Map<String, Integer> data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (Map.Entry<String, Integer> entry : data.entrySet()) {
			dataset.addValue(entry.getValue(), "Modes Count", entry.getKey());
		}

		JFreeChart barChart = ChartFactory.createBarChart(
			"Modes Count",
			"Modes",
			"Count",
			dataset,
			PlotOrientation.VERTICAL,
			true, true, false
		);

		CategoryPlot plot = barChart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		// Define colors for each mode
		Color[] colors = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.ORANGE, Color.PINK, Color.CYAN}; // Add more colors if needed
		int colorIndex = 0;

		int columnCount = plot.getDataset().getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			renderer.setSeriesPaint(i, colors[colorIndex % colors.length]);
			colorIndex++;
		}

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ChartPanel(barChart));
		frame.pack();
		frame.setVisible(true);
	}

}
