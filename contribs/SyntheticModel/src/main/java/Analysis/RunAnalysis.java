package Analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.matsim.analysis.TripsAndLegsCSVWriter;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.drt.routing.MultiModeDrtMainModeIdentifier;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.AnalysisMainModeIdentifier;
import org.matsim.core.router.RoutingModeMainModeIdentifier;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.matsim.analysis.TripsAndLegsCSVWriter.LEGSHEADER_BASE;
import static org.matsim.analysis.TripsAndLegsCSVWriter.TRIPSHEADER_BASE;


public class RunAnalysis {

	public static void main(String[] args) {
//		String pathToEventsFile = "examples/scenarios/UrbanLine/Extension/base/output/output_events.xml.gz";
//		String outputPath = "examples/scenarios/Odakyu1/Analyses/tt.csv";  // Modify this to your desired output path
//
//		// Set up events manager and handler
//		EventsManager eventsManager = EventsUtils.createEventsManager();
//		TravelTimeEventHandler travelTimeHandler = new TravelTimeEventHandler();
//		eventsManager.addHandler(travelTimeHandler);
//
//		// Read events from the file
//		MatsimEventsReader eventsReader = new MatsimEventsReader(eventsManager);
//		eventsReader.readFile(pathToEventsFile);
//
//		// Retrieve travel times
//		Map<Id<Person>, Double> travelTimes = travelTimeHandler.getTravelTimes();
//
//		// Write to CSV
////		writeTravelTimesToCSV(travelTimes, outputPath);
//

	}


	// Implement this method to retrieve or generate experienced plans
	private static IdMap<Person, Plan> getExperiencedPlans(Scenario scenario, String pathToExperiencedPlansFile) {
		// Create an empty IdMap to store the experienced plans
		IdMap<Person, Plan> experiencedPlans = new IdMap<>(Person.class);

		// Create a population object to load plans into
		Population population = PopulationUtils.createPopulation(scenario.getConfig(), scenario.getNetwork());

		// Set up a MATSim plans reader pointing to the experienced plans file
		PopulationReader plansReader = new PopulationReader(scenario);

		// Read the experienced plans file
		plansReader.readFile(pathToExperiencedPlansFile);

		// Iterate over the persons in the loaded population and add their selected plans to the IdMap
		for (Person person : population.getPersons().values()) {
			Plan selectedPlan = person.getSelectedPlan();
			if (selectedPlan != null) {
				experiencedPlans.put(person.getId(), selectedPlan);
			}
		}
		return experiencedPlans;
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
