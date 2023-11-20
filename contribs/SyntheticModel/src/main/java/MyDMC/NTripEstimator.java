package MyDMC;

import com.google.inject.Inject;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.components.estimators.AbstractTripRouterEstimator;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.contribs.discrete_mode_choice.model.trip_based.candidates.TripCandidate;
import org.matsim.core.router.TripRouter;
import org.matsim.core.utils.timing.TimeInterpretation;
import org.matsim.facilities.ActivityFacilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NTripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public NTripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
						  TimeInterpretation timeInterpretation) {
		super(tripRouter, facilities, timeInterpretation, createPreroutedModes());
	}

	private static Collection<String> createPreroutedModes() {
		// Create a collection with your prerouted modes
		return Arrays.asList(TransportMode.car, TransportMode.walk, TransportMode.pt, TransportMode.bike, TransportMode.drt);
	}

	@Override
	protected double estimateTrip(Person person, String mode, DiscreteModeChoiceTrip trip,
								  List<TripCandidate> previousTrips, List<? extends PlanElement> routedTrip) {
		// I) Calculate total travel time
		double totalTravelTime = 0.0;
		double totalTravelDistance = 0.0;


		for (PlanElement element : routedTrip) {
			if (element instanceof Leg) {
				Leg leg = (Leg) element;
				totalTravelTime = totalTravelTime + (leg.getTravelTime().seconds() / 3600.0);
				totalTravelDistance += leg.getRoute().getDistance() * 1e-3;
			}
		}

		// II) Compute mode-specific utility
		double utility = 0;
		// Check if the person always has a car available
		boolean carAlwaysAvailable = isCarAlwaysAvailable(person);
		boolean nextActivityIsWork = isNextActivityWork(trip);
		if (!isDRTZone(trip)) {
			// Compute mode-specific utility based on car availability
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = calculateUtilityForWorkActivity(mode, totalTravelTime, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = calculateStandardUtility(mode, totalTravelTime, carAlwaysAvailable);
			}
		} else {
			// Compute mode-specific utility based on car availability
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = calculateUtilityForWorkActivityDRT(mode, totalTravelTime, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = calculateStandardUtilityDRT(mode, totalTravelTime, carAlwaysAvailable);
			}
		}
		return utility;
	}

	private boolean isCarAlwaysAvailable (Person person){
		// Implementation depends on how car availability is represented in your data model
		// Example: Check a custom attribute in the Person object
		String carAvailability = (String) person.getAttributes().getAttribute("car_avail");
		return "always".equals(carAvailability);
	}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}
	private boolean isDRTZone (DiscreteModeChoiceTrip trip){
		Double xCoordinate = (Double) trip.getOriginActivity().getCoord().getX();
		Double xCoordinate1 = (Double) trip.getDestinationActivity().getCoord().getX();
		return  xCoordinate >= 1000 && xCoordinate1 >= 1000 ;
	}

	private double calculateUtilityForWorkActivity(String mode, double totalTravelTime, boolean carAlwaysAvailable) {
		double utility = 0;
		if (carAlwaysAvailable) {
			// Mode Choice Constants Estimated from Odakyu Trips
			switch (mode) {
				case TransportMode.car: utility = -0.96 - 0.3 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.drt: utility = -0.7 - 6.04 * totalTravelTime;break;
			}
		} else {
			switch (mode) {
//			case TransportMode.car:
//				utility = -0.5 - 0.025 * totalTravelDistance;
//				break;
//			case TransportMode.pt:
//				utility = -0.15 - 0.15 * totalTravelTime;
//				break;
//			case TransportMode.walk:
//				utility = -1.0 * totalTravelTime;
//				break;
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime; break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime; break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime; break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.drt: utility = -0.7 - 6.04 * totalTravelTime; break;
			}
		}
		return utility;
	}
	private double calculateStandardUtility(String mode, double totalTravelTime, boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (carAlwaysAvailable) {
			switch (mode) {
				case TransportMode.car:
					utility = -0.96 - 0.3 * totalTravelTime;
					break;
				case TransportMode.pt:
					utility = -1.7 - 5.15 * totalTravelTime;
					break;
				case TransportMode.walk:
					utility = -7.48 * totalTravelTime;
					break;
				case TransportMode.bike:
					utility = -1.47 - 5.28 * totalTravelTime;
					break;
				case TransportMode.drt:
					utility = -0.7 - 6.04 * totalTravelTime;
					break;
			}
		} else {
			switch (mode) {
				case TransportMode.car:
					utility = -0.96 - 5.3 * totalTravelTime;
					break;
				case TransportMode.pt:
					utility = -1.7 - 5.15 * totalTravelTime;
					break;
				case TransportMode.walk:
					utility = -7.48 * totalTravelTime;
					break;
				case TransportMode.bike:
					utility = -1.47 - 5.28 * totalTravelTime;
					break;
				case TransportMode.drt:
					utility = -0.7 - 6.04 * totalTravelTime;
					break;
			}
		}

		return utility;
	}
	private double calculateUtilityForWorkActivityDRT(String mode, double totalTravelTime, boolean carAlwaysAvailable) {
		double utility = 0;
		if (carAlwaysAvailable) {
			switch (mode) {
				case TransportMode.car: utility = -0.96 - 0.3 * totalTravelTime; break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime; break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime; break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime; break;
				case TransportMode.drt: utility = -0.7 - 6.04 * totalTravelTime; break;          }
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime; break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime; break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime; break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.drt: utility = -0.7 - 6.04 * totalTravelTime; break;   }
		}
		return utility;
	}
	private double calculateStandardUtilityDRT(String mode, double totalTravelTime, boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (carAlwaysAvailable) {
			switch (mode) {
				case TransportMode.car:
					utility = -0.96 - 0.3 * totalTravelTime;
					break;
				case TransportMode.pt:
					utility = -1.7 - 5.15 * totalTravelTime;
					break;
				case TransportMode.walk:
					utility = -7.48 * totalTravelTime;
					break;
				case TransportMode.bike:
					utility = -1.47 - 5.28 * totalTravelTime;
					break;
				case TransportMode.drt:
					utility = -0.7 - 6.04 * totalTravelTime;
					break;
			}
		} else {
			switch (mode) {
				case TransportMode.car:
					utility = -0.96 - 5.3 * totalTravelTime;
					break;
				case TransportMode.pt:
					utility = -1.7 - 5.15 * totalTravelTime;
					break;
				case TransportMode.walk:
					utility = -7.48 * totalTravelTime;
					break;
				case TransportMode.bike:
					utility = -1.47 - 5.28 * totalTravelTime;
					break;
				case TransportMode.drt:
					utility = -0.7 - 6.04 * totalTravelTime ;
					break;
			}
		}

		return utility;
	}




}

