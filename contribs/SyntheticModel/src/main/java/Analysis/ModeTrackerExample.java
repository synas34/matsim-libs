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

	private String pathToEventsFile;
	private Map<String, Integer> modeCounts;

	public ModeTrackerExample(String pathToEventsFile) {
		this.pathToEventsFile = pathToEventsFile;
	}

	public void analyze() {
		// Create event handlers
		MainModeAnalyzer analyzer = new MainModeAnalyzer();
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(analyzer);  // Register MainModeAnalyzer as an event handler

		// Process the events
		new MatsimEventsReader(eventsManager).readFile(pathToEventsFile);

		// Now retrieve agents with main PT mode AFTER processing events
		Set<Id<Person>> agentsWithMainPTMode = analyzer.getAgentsUsingPtMode();

		// Register ModeRecordingEventHandler
		ModeRecordingEventHandler eventHandler = new ModeRecordingEventHandler(agentsWithMainPTMode);
		eventsManager.addHandler(eventHandler);  // NOTE: We could also re-use the same eventsManager but you'd need to reset() it first

		// Register AgentSpecificEventHandler
		Id<Person> targetAgentId = Id.createPersonId("12293"); // Target agent's ID
		AgentSpecificEventHandler agentEventHandler = new AgentSpecificEventHandler(targetAgentId);
		eventsManager.addHandler(agentEventHandler);

		// Re-process the events to record modes (if you didn't reset the previous EventsManager)
		new MatsimEventsReader(eventsManager).readFile(pathToEventsFile);

		// Compute mode counts
		this.modeCounts = computeModeCounts(eventHandler.getAgentModes());
	}

	public Map<String, Integer> getModeCounts() {
		return modeCounts;
	}

	private static Map<String, Integer> computeModeCounts(Map<Id<Person>, Set<String>> modes) {
		Map<String, Integer> modeCounts = new HashMap<>();
		for (Set<String> modeSet : modes.values()) {
			for (String mode : modeSet) {
				modeCounts.put(mode, modeCounts.getOrDefault(mode, 0) + 1);
			}
		}
		return modeCounts;
	}
}
