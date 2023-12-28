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

public class Dec28TripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public Dec28TripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
                              TimeInterpretation timeInterpretation) {
		super(tripRouter, facilities, timeInterpretation, createPreroutedModes());
	}

	private static Collection<String> createPreroutedModes() {
		// Create a collection with your prerouted modes
		return Arrays.asList(TransportMode.car, TransportMode.walk, TransportMode.pt, TransportMode.bike);
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
		String carAvailability = (String) person.getAttributes().getAttribute("car_avail");
		return "always".equals(carAvailability);
	}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}


	private double calculateUtilityForWorkActivity(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -0.7 - 0.15 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;						}
		}
		return utility;
	}
	private double calculateStandardUtility(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		// Mode Choice Constants Estimated from Odakyu Trips
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}
	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 4){
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 4 & totalTravelDistance <= 10)  {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;
				case TransportMode.walk: utility = -10000000 - 7.48 * totalTravelTime;   			break;
				case TransportMode.bike: utility = -10000000 - 5.28 * totalTravelTime;			break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;			}
		}
		return utility;
	}
}
