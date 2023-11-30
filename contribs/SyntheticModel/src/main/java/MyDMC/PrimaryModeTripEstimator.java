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

public class PrimaryModeTripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public PrimaryModeTripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
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
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = calculateUtilityForWorkActivity(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = calculateStandardUtility(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
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

	private double calculateUtilityForWorkActivity(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
			// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -2.04 - 3.73 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.21 - 0.96 * totalTravelTime - 0.0013 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -5.97 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.68 - 4.31 * totalTravelTime;break;
				case TransportMode.ride: utility = -4.15 - 5.9 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.car: utility = -2.04 - 3.73 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.21 - 0.96 * totalTravelTime - 0.0013 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -5.97 * totalTravelTime - 100000000  ;break;
				case TransportMode.bike: utility = -1.68 - 4.31 * totalTravelTime;break;
				case TransportMode.ride: utility = -4.15 - 5.9 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -2.04 - 3.73 * totalTravelTime - 0.004 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.21 - 0.96 * totalTravelTime - 0.0013 * (totalTravelDistance * 15) ;break;
				case TransportMode.walk: utility = -5.97 * totalTravelTime - 100000000    ;break;
				case TransportMode.bike: utility = -1.68 - 4.31 * totalTravelTime - 100000000  ;break;
				case TransportMode.ride: utility = -4.15 - 5.9 * totalTravelTime;break;			}
		}
		return utility;
	}
	private double calculateStandardUtility(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.85 - 6.27 * totalTravelTime - 0.0027 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.87 - 0.37 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -7.84 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.35 - 6.67 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.car: utility = -1.85 - 6.27 * totalTravelTime - 0.0027 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.87 - 0.37 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -7.84 * totalTravelTime - 100000000   ;break;
				case TransportMode.bike: utility = -1.35 - 6.67 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.85 - 6.27 * totalTravelTime - 0.0027 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.87 - 0.37 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -7.84 * totalTravelTime  - 100000000   ;break;
				case TransportMode.bike: utility = -1.35 - 6.67 * totalTravelTime - 100000000 			;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}
}

