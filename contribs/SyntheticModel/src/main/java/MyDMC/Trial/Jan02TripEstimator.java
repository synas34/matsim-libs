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

public class Jan02TripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public Jan02TripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
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
		String UrbanContext = (String) person.getAttributes().getAttribute("UrbanContext");
		// Compute mode-specific utility based on car availability
		if (UrbanContext.equals("Urban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = UrbanOtherUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = SuburbanOtherUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("CBD")) {
			utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
		}
		if (UrbanContext.equals("Rural")) {
			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
		}
		return utility;
	}

	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("car_avail");
		return "always".equals(carAvailability);			}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor =  -5.68 * totalTravelTime;
			bikeFactor = -1.38 - 4.78 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.38 - 4.78 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -3.56 - 1.36 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -5.19 - 4.92 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.40 - 1.03 * totalTravelTime - 0.0013 * (totalTravelDistance * 16) ;break;     }

		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.56 * totalTravelTime;
			bikeFactor = -1.15 - 6.67 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.15 - 6.67 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor ;break;
			case TransportMode.car: utility = -3.07 - 4.64 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -4.09 - 7.21 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.13 - 0.93 * totalTravelTime - 0.0013 * (totalTravelDistance * 16) ;break;		}

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -5.26 * totalTravelTime;
			bikeFactor = -1.67 - 3.80 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.67 - 3.80 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor     ;break;
			case TransportMode.bike: utility = bikeFactor 	  ;break;
			case TransportMode.car: utility = -1.20 - 4.35 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -3.61 - 6.24 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.44 - 0.96 * totalTravelTime - 0.0013 * (totalTravelDistance * 16) ;break;		}

		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.35 * totalTravelTime;
			bikeFactor = -2.53 - 5.71 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -2.53 - 5.71 * totalTravelTime;
		} else {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -1.15 - 4.77 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -2.76 - 6.47 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.98 - 1.21 * totalTravelTime - 0.0013 * (totalTravelDistance * 16) ;break;		}
		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.35 * totalTravelTime;
			bikeFactor = -2.53 - 6.53 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -2.53 - 6.53 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -5.37 - 2.80 * totalTravelTime - 0.0025 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -5.90 - 4.41 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.46 + 0.30 * totalTravelTime - 0.00011 * (totalTravelDistance * 16) ;break;		}
		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.48 * totalTravelTime;
			bikeFactor = -1.47 - 5.28 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.47 - 5.28 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;		}
		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.00 * totalTravelTime;
			bikeFactor = -1.24 - 5.83 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -1.24 - 5.83 * totalTravelTime ;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -1.75 - 5.54 * totalTravelTime - 0.0034 * (totalTravelDistance * 10); break;
			case TransportMode.ride: utility = -3.21 - 7.81 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.65 - 0.23 * totalTravelTime - 0.0008 * (totalTravelDistance * 16) ;break;}
		return utility;
	}
}
