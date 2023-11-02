package Analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;


public class TravelTimeEventHandler implements PersonArrivalEventHandler, PersonDepartureEventHandler {

	private final Map<Id<Person>, Double> departureTimes = new HashMap<>();
	private final Map<Id<Person>, Double> travelTimes = new HashMap<>();

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		departureTimes.put(event.getPersonId(), event.getTime());
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		double departureTime = departureTimes.remove(event.getPersonId());
		double travelTime = event.getTime() - departureTime;
		travelTimes.put(event.getPersonId(), travelTime);
	}

	public Map<Id<Person>, Double> getTravelTimes() {
		return travelTimes;
	}

	// ... (other methods, if any)
}
