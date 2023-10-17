package Analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModeRecordingEventHandler implements PersonDepartureEventHandler {

	private final Set<Id<Person>> agentIdsToTrack;
	private final Map<Id<Person>, Set<String>> agentModes = new HashMap<>();

	public ModeRecordingEventHandler(Set<Id<Person>> agentIdsToTrack) {
		this.agentIdsToTrack = agentIdsToTrack;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (agentIdsToTrack.contains(event.getPersonId())) {
			agentModes.computeIfAbsent(event.getPersonId(), k -> new HashSet<>()).add(event.getLegMode());
			if (event.getLegMode().equals("drt") || event.getLegMode().equals("bike")) {
				agentModes.get(event.getPersonId()).remove("walk");
			}
			System.out.println("Recorded Mode: " + event.getLegMode() + " for Agent ID: " + event.getPersonId());
		}
	}

	public Map<Id<Person>, Set<String>> getAgentModes() {
		return agentModes;
	}

	@Override
	public void reset(int iteration) {
		agentModes.clear();
	}
}
