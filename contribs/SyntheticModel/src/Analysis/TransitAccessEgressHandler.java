package Analysis;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.events.handler.BasicEventHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransitAccessEgressHandler implements  PersonLeavesVehicleEventHandler, PersonDepartureEventHandler {


	// Use a map to count occurrences of each egress mode
	private final Map<String, Integer> egressModeCounts = new HashMap<>();

	public Map<String, Double> getEgressModeRatios() {
		int total = egressModeCounts.values().stream().mapToInt(Integer::intValue).sum();

		Map<String, Double> ratios = new HashMap<>();
		for (Map.Entry<String, Integer> entry : egressModeCounts.entrySet()) {
			ratios.put(entry.getKey(), entry.getValue() / (double) total);
		}
		return ratios;
	}
	private final Set<String> egressModes = new HashSet<>();
	private final Map<String, Boolean> recentTransitExit = new HashMap<>();  // Map to store if a person recently left a transit vehicle

	@Override
	public void reset(int iteration) {
		egressModes.clear();
		recentTransitExit.clear();
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		// Logic to determine egress mode...
		// For this example, let's assume you have a method called determineEgressMode that gets the mode
//		String egressMode = determineEgressMode(event);
//		egressModeCounts.put(egressMode, egressModeCounts.getOrDefault(egressMode, 0) + 1);
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		// When someone departs (starts a new leg), we check if they recently exited a transit mode
		if(recentTransitExit.getOrDefault(event.getPersonId().toString(), false)) {
			egressModes.add(event.getLegMode());  // Capture the mode as egress mode
			recentTransitExit.put(event.getPersonId().toString(), false);  // Reset the flag for this person
		}
	}

	public Set<String> getEgressModes() {
		return egressModes;
	}


}
