package Analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.api.core.v01.events.Event;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModeTrackerExample {
	public static void main(String[] args) {
		String pathToEventsFile = "examples/scenarios/UrbanLine/Lastditch/FMLM/output/output_events.xml.gz"; // Replace with your path

		// Create event handlers
		MainModeAnalyzer analyzer = new MainModeAnalyzer();
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(analyzer);  // Register MainModeAnalyzer as an event handler

		// Process the events
		new MatsimEventsReader(eventsManager).readFile(pathToEventsFile);

		// Now retrieve agents with main PT mode AFTER processing events
		Set<Id<Person>> agentsWithMainPTMode = analyzer.getAgentsUsingPtMode();
		System.out.println("Agent: " + agentsWithMainPTMode);

		// Register ModeRecordingEventHandler
		ModeRecordingEventHandler eventHandler = new ModeRecordingEventHandler(agentsWithMainPTMode);
		eventsManager.addHandler(eventHandler);  // NOTE: We could also re-use the same eventsManager but you'd need to reset() it first

		// Register AgentSpecificEventHandler
		Id<Person> targetAgentId = Id.createPersonId("12293"); // Target agent's ID
		AgentSpecificEventHandler agentEventHandler = new AgentSpecificEventHandler(targetAgentId);
		eventsManager.addHandler(agentEventHandler);

		// Re-process the events to record modes (if you didn't reset the previous EventsManager)
		new MatsimEventsReader(eventsManager).readFile(pathToEventsFile);

		// Now you can retrieve and print results
		List<Event> eventsForAgent = agentEventHandler.getAgentEvents();
		Map<Id<Person>, Set<String>> modes = eventHandler.getAgentModes();

		// Compute mode counts and print
		Map<String, Integer> modeCounts = getModeCounts(modes);
		for (Map.Entry<Id<Person>, Set<String>> entry : modes.entrySet()) {
			System.out.println("Agent: " + entry.getKey() + ", Mode: " + entry.getValue());
		}
		for (Event event : eventsForAgent) {     // print specific agents events
			System.out.println(event.toString());
		}
		for (Map.Entry<String, Integer> entry : modeCounts.entrySet()) {
			System.out.println("Mode: " + entry.getKey() + ", Count: " + entry.getValue());
		}

	}

	private static Map<String, Integer> getModeCounts(Map<Id<Person>, Set<String>> modes) {
		Map<String, Integer> modeCounts = new HashMap<>();
		for (Set<String> modeSet : modes.values()) {
			for (String mode : modeSet) {
				modeCounts.put(mode, modeCounts.getOrDefault(mode, 0) + 1);
			}
		}
		return modeCounts;
	}


}

