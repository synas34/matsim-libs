package Analysis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.Config;

import java.util.Map;
import java.util.Set;

public class RunAnalysis {
	public static void main(String[] args) {
		String eventsFilePath = "examples/scenarios/UrbanLine/SuburbanCorex5/output/output_events.xml.gz";

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		TransitAccessEgressHandler handler = new TransitAccessEgressHandler();
		eventsManager.addHandler(handler);

		new MatsimEventsReader(eventsManager).readFile(eventsFilePath);
		Map<String, Double> egressModeRatios = handler.getEgressModeRatios();

		// Print out the results
		System.out.println("Egress Modes Ratios:");
		for (Map.Entry<String, Double> entry : egressModeRatios.entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
	}

}
