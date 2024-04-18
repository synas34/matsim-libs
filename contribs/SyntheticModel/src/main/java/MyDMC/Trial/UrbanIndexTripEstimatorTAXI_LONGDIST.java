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

public class UrbanIndexTripEstimatorTAXI_LONGDIST extends AbstractTripRouterEstimator {
	@Inject
	public UrbanIndexTripEstimatorTAXI_LONGDIST(TripRouter tripRouter, ActivityFacilities facilities,
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
		double totalRidingDistance = 0.0;
		double totalTransferTime = 0.0;
		double totalTransferDistance = 0.0;

		for (PlanElement element : routedTrip) {
			if (element instanceof Leg) {
				Leg leg = (Leg) element;
				totalTravelTime = totalTravelTime + (leg.getTravelTime().seconds() / 3600.0);
				totalTravelDistance += leg.getRoute().getDistance() * 1e-3;

				if (leg.getMode().equals("pt")) {
					totalRidingTime += (leg.getTravelTime().seconds() / 3600.0);
					totalRidingDistance += leg.getRoute().getDistance() * 1e-3;
				} else {
					totalTransferTime += (leg.getTravelTime().seconds() / 3600.0);
					totalTransferDistance += leg.getRoute().getDistance() * 1e-3;
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
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			} else {
				// Standard utility calculations
				utility = UrbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			} else {
				// Standard utility calculations
				utility = SuburbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			}
		}
		if (UrbanContext.equals("CBD")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			} else {
				// Standard utility calculations
				utility = CBDOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
			}
		}
		if (UrbanContext.equals("Rural")) {
			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);
		}

//		utility = calcModeUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);

		return utility;
	}


	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("carAvail");
		return "always".equals(carAvailability);			}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}
	private double calcModeUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor =  -9.83 * totalTravelTime;
			bikeFactor = -1.88 - 6.35 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.88 - 6.35 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}


		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = carFactor -2.74 - 5.51 * totalTravelTime - 0.0011 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.59 - 6.91	 * totalTravelTime;		break;
			case TransportMode.pt: utility = -2.19 - 0.38 * totalRidingTime - 4.13 * totalTransferTime - 0.0015 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.46 - 4.50 * totalTravelTime - 0.00013 * ((totalTravelDistance * 430)); break; }

			return utility;
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.86 * totalTravelTime;
			bikeFactor = -1.18 - 5.66 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.18 - 5.66 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}


		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = carFactor - 3.77 - 1.25 * totalTravelTime - 0.0048 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.42 - 4.60 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.07 - 0.74 * totalRidingTime - 3.64 * totalTransferTime - 0.0013 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -5.14 - 5.41 * totalTravelTime - 0.00010 * ((totalTravelDistance * 430));break;    }


		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -11.12 * totalTravelTime;
			bikeFactor = -1.84 - 7.44 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.84 - 7.44 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor ;break;
			case TransportMode.car: utility = carFactor - 3.80 - 4.78 * totalTravelTime - 0.0042 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.06 - 8.03 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.93 - 0.15 * totalRidingTime - 3.67 * totalTransferTime - 0.0020 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.55 - 5.14 * totalTravelTime - 0.00016 * (totalTravelDistance * 430);break;    }

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.01 * totalTravelTime;
			bikeFactor = -0.95 - 6.27 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -0.95 - 6.27 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor ;break;
			case TransportMode.car: utility = carFactor - 1.74 - 5.02 * totalTravelTime - 0.0025 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.264 - 5.45 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.83 - 1.32 * totalRidingTime - 3.20 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -14.43 + 5.05 * totalTravelTime - 0.00028 * ((totalTravelDistance * 430));break;    }


		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.00 * totalTravelTime;
			bikeFactor = -1.87 - 5.71 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.87 - 5.71 * totalTravelTime;
		} else {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}
		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}
		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = carFactor - 1.63 - 5.94 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.16 - 6.51 * totalTravelTime;break;
			case TransportMode.pt: utility =-3.14 - 0.81 * totalRidingTime - 2.38 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.45 - 5.06 * totalTravelTime - 0.00012 * (totalTravelDistance * 430);break;    }

		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.68 * totalTravelTime;
			bikeFactor = -2.35 - 10.41 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -2.35 - 10.41 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = carFactor - 3.69 - 5.49 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.34 - 3.79 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.12 + 0.31 * totalRidingTime - 3.74 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -4.97 - 2.05 * totalTravelTime - -0.00028 * ((totalTravelDistance * 430));break;    }

		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -11.197 * totalTravelTime;
			bikeFactor = -4.13 - 6.146 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -4.13 - 6.146 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = -7.18 - 2.175 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 6.908 - 2.65 * totalTravelTime;break;
			case TransportMode.pt: utility =-4.45 - 0.362 * totalRidingTime - 2.11 * totalTransferTime + 0.0013 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -5.93 - 8.86 * totalTravelTime - 0.0010 * (totalTravelDistance * 430);break;    }

		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.61 * totalTravelTime;
			bikeFactor = -1.74 - 6.18 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -1.74 - 6.18 * totalTravelTime ;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = carFactor - 2.38 - 5.60 * totalTravelTime - 0.0020 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.49 - 7.50 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.18 - 0.17 * totalRidingTime - 4.08 * totalTransferTime - 0.0011 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.29 - 5.81 * totalTravelTime - 0.00013 * ((totalTravelDistance * 430));break;    }

		return utility;
	}
}
