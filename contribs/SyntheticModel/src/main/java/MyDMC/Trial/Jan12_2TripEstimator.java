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

public class Jan12_2TripEstimator extends AbstractTripRouterEstimator {
	@Inject
	public Jan12_2TripEstimator(TripRouter tripRouter, ActivityFacilities facilities,
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
		double totalRidingTime = 0.0;
		double totalTransferTime = 0.0;

		for (PlanElement element : routedTrip) {
			if (element instanceof Leg) {
				Leg leg = (Leg) element;
				totalTravelTime = totalTravelTime + (leg.getTravelTime().seconds() / 3600.0);
				totalTravelDistance += leg.getRoute().getDistance() * 1e-3;

				if (leg.getMode().equals("pt")) {
					totalRidingTime += (leg.getTravelTime().seconds() / 3600.0);
				} else {
					totalTransferTime += (leg.getTravelTime().seconds() / 3600.0);
				}
			}
		}

		// II) Compute mode-specific utility
		double utility = 0;
		// Check if the person always has a car available
		boolean carAlwaysAvailable = isCarAlwaysAvailable(person);
		boolean nextActivityIsWork = isNextActivityWork(trip);
		String UrbanContext = (String) person.getAttributes().getAttribute("UrbanContext");
		// Compute mode-specific utility based on car availability
//		if (UrbanContext.equals("Urban")) {
//			if (nextActivityIsWork) {
//				// Utility calculations when the next activity is work
//				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//			} else {
//				// Standard utility calculations
//				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//			}
//		}
//		if (UrbanContext.equals("Suburban")) {
//			if (nextActivityIsWork) {
//				// Utility calculations when the next activity is work
//				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//			} else {
//				// Standard utility calculations
//				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//			}
//		}
//		if (UrbanContext.equals("CBD")) {
//			utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//		}
//		if (UrbanContext.equals("Rural")) {
//			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
//		}

		utility = calcModeUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);

		return utility;
	}


	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("car_avail");
		return "always".equals(carAvailability);			}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}

	private double calcModeUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor =  -6.50 * totalTravelTime;
			bikeFactor = -1.28 - 5.66 * totalTravelTime ;
		} else if (totalTravelDistance > 4 && totalTravelDistance <= 8) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.28 - 5.66 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
		}
		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -1.70 - 5.29 * totalTravelTime - 0.0030 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.23 - 7.19	 * totalTravelTime ;		break;
			case TransportMode.pt: utility = -0.61 - 0.36 * totalRidingTime - 6.50 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;     }

		return utility;
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor =  -6.50 * totalTravelTime;
			bikeFactor = -1.28 - 5.66 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.28 - 5.66 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -1.70 - 5.29 * totalTravelTime - 0.0030 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -3.23 - 7.19	 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.61 - 0.36 * totalRidingTime - 6.50 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;     }

		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.52 * totalTravelTime;
			bikeFactor = -1.15 - 6.61 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.15 - 6.61 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor ;break;
			case TransportMode.car: utility = -2.96 - 5.83 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -4.14 - 6.64 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.20 - 0.026 * totalRidingTime - 7.52 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;     }

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -5.40 * totalTravelTime;
			bikeFactor = -1.15 - 4.88 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.15 - 4.88 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor     ;break;
			case TransportMode.bike: utility = bikeFactor 	  ;break;
			case TransportMode.car: utility = -0.84	 - 5.65 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -2.62 - 6.33 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.85 - 0.50 * totalRidingTime - 5.40 * totalTransferTime - 0.0006 * (totalTravelDistance * 16) ;break;     }

		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -6.80 * totalTravelTime;
			bikeFactor = -1.22 - 5.74 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.22 - 5.74 * totalTravelTime;
		} else {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -1.10 - 5.69 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -2.79 - 6.20 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.02 - 0.95 * totalRidingTime - 6.80 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;     }
		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -6.73 * totalTravelTime;
			bikeFactor = -2.47 - 5.80 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -2.47 - 5.80 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -5.01 - 2.83 * totalTravelTime - 0.0025 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -5.72 - 3.58 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.22 + 1.64 * totalRidingTime - 6.73 * totalTransferTime - 0.0008 * (totalTravelDistance * 16) ;break;     }
		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
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
			case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.50 - 0.51 * totalRidingTime - 7.31 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;     }
		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -6.09 * totalTravelTime;
			bikeFactor = -1.09 - 5.48 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -1.09 - 5.48 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -1.51 - 6.00 * totalTravelTime - 0.0025 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -3.07 - 7.09 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.76 + 0.16 * totalRidingTime - 6.09 * totalTransferTime - 0.0008 * (totalTravelDistance * 16) ;break;     }
		return utility;
	}
}
