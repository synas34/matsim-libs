package Analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.events.handler.BasicEventHandler;

import java.util.ArrayList;
import java.util.List;
public class AgentSpecificEventHandler implements BasicEventHandler {
	private final Id<Person> targetAgentId;
	private final List<Event> agentEvents = new ArrayList<>();

	public AgentSpecificEventHandler(Id<Person> targetAgentId) {
		this.targetAgentId = targetAgentId;
	}

	@Override
	public void handleEvent(Event event) {
		// You might have to cast event to a specific event type if you're accessing methods not present in the base Event class
		if (event.getAttributes().get("person") != null && Id.createPersonId(event.getAttributes().get("person")).equals(targetAgentId)) {
			agentEvents.add(event);
		}
	}

	public List<Event> getAgentEvents() {
		return agentEvents;
	}

	// Other necessary overrides like reset()
}
