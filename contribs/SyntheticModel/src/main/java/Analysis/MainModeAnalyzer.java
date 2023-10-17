package Analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.HashSet;
import java.util.Set;

public class MainModeAnalyzer implements BasicEventHandler {
	private Set<Id<Person>> agentsUsingPTMode = new HashSet<>();

	@Override
	public void handleEvent(Event event) {
		if (event instanceof PersonEntersVehicleEvent) {
			PersonEntersVehicleEvent pevEvent = (PersonEntersVehicleEvent) event;
			if (isPtVehicle(pevEvent.getVehicleId())) {
				agentsUsingPTMode.add(pevEvent.getPersonId());
				System.out.println("Agent " + pevEvent.getPersonId() + " used PT.");
			}
		}
	}

	private boolean isPtVehicle(Id<Vehicle> vehicleId) {
		return vehicleId.toString().contains("tr_");
	}

	public Set<Id<Person>> getAgentsUsingPtMode() {
		return agentsUsingPTMode;
	}

	@Override
	public void reset(int iteration) {
		agentsUsingPTMode.clear();
	}
}
