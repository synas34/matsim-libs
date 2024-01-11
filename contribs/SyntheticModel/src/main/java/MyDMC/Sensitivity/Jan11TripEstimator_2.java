package MyDMC.Sensitivity;

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

public class Jan11TripEstimator_2 extends AbstractTripRouterEstimator {
	@Inject
	public Jan11TripEstimator_2(TripRouter tripRouter, ActivityFacilities facilities,
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
		if (UrbanContext.equals("Urban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = UrbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = SuburbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("CBD")) {
			utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
		}
		if (UrbanContext.equals("Rural")) {
			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, carAlwaysAvailable);
		}

		return utility;
	}


	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("car_avail");
		return "always".equals(carAvailability);			}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor =  -5.56 * totalTravelTime;
			bikeFactor = -1.36 - 4.61 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.38 - 4.78 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -3.40 - 2.65 * totalTravelTime - 0.0010 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -5.23 - 4.30 * totalTravelTime;	break;
			case TransportMode.pt: utility = -0.507 - 0.76 * totalRidingTime - 5.56 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-5.23 - 4.30 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7 ; break;    }

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
			case TransportMode.pt: utility = -1.20 - 0.026 * totalRidingTime - 7.52 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-4.14 - 6.64 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7 ; break;    }


		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -5.22 * totalTravelTime;
			bikeFactor = -1.67 - 3.76 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.67 - 3.76 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor     ;break;
			case TransportMode.bike: utility = bikeFactor 	  ;break;
			case TransportMode.car: utility = -1.13	 - 5.25 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -3.66 - 5.65 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.57 - 0.81 * totalRidingTime - 5.22 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-3.66 - 5.65 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7; break;    }


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
			case TransportMode.pt: utility = -1.02 - 0.95 * totalRidingTime - 6.80 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-2.79 - 6.20 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7; break;    }

		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.31 * totalTravelTime;
			bikeFactor = -2.53 - 6.45 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -2.53 - 6.45 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -5.29 - 3.38 * totalTravelTime - 0.0025 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -5.92 - 4.16 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.50 + 0.51 * totalRidingTime - 7.31 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-5.92 - 4.16 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7; break;    }

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
			case TransportMode.pt: utility = -1.50 - 0.51 * totalRidingTime - 7.31 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-3.35 - 7.65 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7; break;    }

		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -6.96 * totalTravelTime;
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
			case TransportMode.car: utility = -1.70 - 6.37 * totalTravelTime - 0.0034 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = -3.24 - 7.50 * totalTravelTime;break;
			case TransportMode.pt: utility = -0.73 - 0.16 * totalRidingTime - 6.96 * totalTransferTime - 0.0010 * (totalTravelDistance * 16) ;break;
			case TransportMode.drt: utility = (-3.24 - 7.50 * totalTravelTime - 0.0010 * (totalTravelDistance * 70)) * 0.7; break;    }

		return utility;
	}
}
