package MyDMC.Trial;

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

public class Dec4TripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public Dec4TripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
							 TimeInterpretation timeInterpretation) {
		super(tripRouter, facilities, timeInterpretation, createPreroutedModes());
	}

	private static Collection<String> createPreroutedModes() {
		// Create a collection with your prerouted modes
		return Arrays.asList(TransportMode.car, TransportMode.walk, TransportMode.pt, TransportMode.bike, TransportMode.ride);
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
			// Compute mode-specific utility based on car availability
//		if (person.getAttributes().getAttribute("InScope") == "yes" & person.getAttributes().getAttribute("EndScope") == "yes") {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = calculateUtilityForWorkActivity(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = calculateStandardUtility(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			}
//		} else {
//			utility = OutOfScopeActivity(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
//		}
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

	private double calculateUtilityForWorkActivity(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
			// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.bike: utility = -1.90 - 4.77 * totalTravelTime;break;
				case TransportMode.car: utility = -2.04 - 5.17 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.15 - 7.87 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.73 - 1.54 * totalTravelTime  ;break;
				case TransportMode.walk: utility = -7.11 * totalTravelTime    ;break;		}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.bike: utility = -1.90 - 4.77 * totalTravelTime;break;
				case TransportMode.car: utility = -2.04 - 5.17 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.15 - 7.87 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.73 - 1.54 * totalTravelTime  ;break;
				case TransportMode.walk: utility = -7.11 * totalTravelTime - 100000000    ;break;		}
		} else {
			switch (mode) {
				case TransportMode.bike: utility = -1.90 - 4.77 * totalTravelTime - 100000000 ;break;
				case TransportMode.car: utility = -2.04 - 5.17 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.15 - 7.87 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.73 - 1.54 * totalTravelTime  ;break;
				case TransportMode.walk: utility = -7.11 * totalTravelTime - 100000000   ;break;		}
		}
		return utility;
	}
	private double calculateStandardUtility(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.bike: utility = -1.50 - 8.19 * totalTravelTime;break;
				case TransportMode.car: utility = -1.94 - 8.99 * totalTravelTime - 0.0023 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.47 - 10.21 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.28 - 0.637 * totalTravelTime - 0.0011 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -9.48 * totalTravelTime ;break;		}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.bike: utility = -1.50 - 8.19 * totalTravelTime ;break;
				case TransportMode.car: utility = -1.94 - 8.99 * totalTravelTime - 0.0023 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.47 - 10.21 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.28 - 0.637 * totalTravelTime - 0.0011 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -9.48 * totalTravelTime - 100000000   ;break;		}
		} else {
			switch (mode) {
				case TransportMode.bike: utility = -1.50 - 8.19 * totalTravelTime - 100000000 ;break;
				case TransportMode.car: utility = -1.94 - 8.99 * totalTravelTime - 0.0023 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.47 - 10.21 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.28 - 0.637 * totalTravelTime - 0.0011 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -9.48 * totalTravelTime - 100000000   ;break;		}
		}
		return utility;
	}

	private double OutOfScopeActivity(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.bike: utility = -0.62 - 3.82 * totalTravelTime;break;
				case TransportMode.car: utility = -1.00 - 2.09 * totalTravelTime - 0.0044 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.77 - 3.80 * totalTravelTime;break;
				case TransportMode.pt: utility = 1.32 - 0.51 * totalTravelTime  - 0.0014 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -4.56 * totalTravelTime    ;break;		}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.bike: utility = -0.62 - 3.82 * totalTravelTime;break;
				case TransportMode.car: utility = -1.00 - 2.09 * totalTravelTime - 0.0044 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.77 - 3.80 * totalTravelTime;break;
				case TransportMode.pt: utility = 1.32 - 0.51 * totalTravelTime  - 0.0014 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -4.56 * totalTravelTime  - 100000000    ;break;		}
		} else {
			switch (mode) {
				case TransportMode.bike: utility = -0.62 - 3.82 * totalTravelTime - 100000000 ;break;
				case TransportMode.car: utility = -1.00 - 2.09 * totalTravelTime - 0.0044 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.77 - 3.80 * totalTravelTime;break;
				case TransportMode.pt: utility = 1.32 - 0.51 * totalTravelTime  - 0.0014 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -4.56 * totalTravelTime  - 100000000    ;break;		}
		}
		return utility;
	}

}

