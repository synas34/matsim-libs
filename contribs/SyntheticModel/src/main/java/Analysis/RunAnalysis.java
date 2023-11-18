package Analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import javax.swing.*;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class RunAnalysis {

	public static void main(String[] args) {
		String pathToEventsFile = "examples/scenarios/UrbanLine/Extension/base/output/output_events.xml.gz";
		String outputPath = "examples/scenarios/Odakyu1/Analyses/tt.csv";  // Modify this to your desired output path

		// Set up events manager and handler
		EventsManager eventsManager = EventsUtils.createEventsManager();
		TravelTimeEventHandler travelTimeHandler = new TravelTimeEventHandler();
		eventsManager.addHandler(travelTimeHandler);

		// Read events from the file
		MatsimEventsReader eventsReader = new MatsimEventsReader(eventsManager);
		eventsReader.readFile(pathToEventsFile);

		// Retrieve travel times
		Map<Id<Person>, Double> travelTimes = travelTimeHandler.getTravelTimes();

		// Write to CSV
		writeTravelTimesToCSV(travelTimes, outputPath);
	}

	private static void writeTravelTimesToCSV(Map<Id<Person>, Double> travelTimes, String outputPath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
			// Write header
			writer.write("PersonID,TravelTime\n");

			// Write data
			for (Map.Entry<Id<Person>, Double> entry : travelTimes.entrySet()) {
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Remaining methods for the bar chart...

}
