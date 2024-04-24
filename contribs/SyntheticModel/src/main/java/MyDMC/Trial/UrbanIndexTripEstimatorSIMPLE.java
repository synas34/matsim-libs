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

public class UrbanIndexTripEstimatorSIMPLE extends AbstractTripRouterEstimator {
	@Inject
	public UrbanIndexTripEstimatorSIMPLE(TripRouter tripRouter, ActivityFacilities facilities,
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
		String DestUrbanContext = (String) person.getAttributes().getAttribute("DestUrbanContext");
		// Compute mode-specific utility based on car availability
		if (UrbanContext.equals("Urban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			} else {
				// Standard utility calculations
				utility = UrbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			} else {
				// Standard utility calculations
				utility = SuburbanOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			}
		}
		if (UrbanContext.equals("CBD")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			} else {
				// Standard utility calculations
				utility = CBDOtherUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
			}
		}
		if (UrbanContext.equals("Rural")) {
			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
		}

//		utility = calcModeUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip);

//		if (totalTravelDistance <= 10) {
//			if (nextActivityIsWork) {
//				// Utility calculations when the next activity is work
//				utility = calcSHORTUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
//			} else {
//				// Standard utility calculations
//				utility = calcLONGUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable, trip, DestUrbanContext);
//			}
//		}

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
			case TransportMode.drt: utility = -6.46 - 4.50 * totalTravelTime - 0.00013 * ((totalTravelDistance * 300) + 500); break; }

			return utility;
	}

	private double calcSHORTUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor =  -10.54 * totalTravelTime;
			bikeFactor = -1.91 - 7.40 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.91 - 7.40 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}


		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = carFactor -2.17 - 11.11 * totalTravelTime + 0.011 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.44 - 10.27	 * totalTravelTime;		break;
			case TransportMode.pt: utility = -2.37 - 0.815 * totalRidingTime - 5.81 * totalTransferTime - 0.0011 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.46 - 3.46 * totalTravelTime - 0.00062 * ((totalTravelDistance * 430)+ 200); break; }

		return utility;
	}

	private double calcLONGUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		switch (mode) {
			case TransportMode.walk: utility = dummyvalue    ;break;
			case TransportMode.bike: utility = dummyvalue 	;break;
			case TransportMode.car: utility = carFactor -0 - 2.014 * totalTravelTime - 0.0004 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 1.69 - 3.027	 * totalTravelTime;		break;
			case TransportMode.pt: utility = 2.056 - 0.417 * totalRidingTime - 3.37 * totalTransferTime - 0.0004 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -1.66 - 4.96 * totalTravelTime - 0.0004 * ((totalTravelDistance * 300) + 500); break; }

		return utility;
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;
		double drtFactor = 0;
		double railFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.77 * totalTravelTime;
			bikeFactor = -1.18 - 5.52 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.18 - 5.52 * totalTravelTime;
		} else {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		if (!carAlwaysAvailable) {
			rideFactor = dummyvalue + dummyvalue * totalTravelTime;
			carFactor = dummyvalue + dummyvalue * totalTravelTime;
		}
		if (destUrbanContext == "Urban" | destUrbanContext == "CBD") {
			drtFactor = 1.32;
			railFactor = 0.99;

		}
		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	;break;
			case TransportMode.car: utility = carFactor - 3.63 - 2.05 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.42 - 4.60 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.111 - 0.46 * totalRidingTime - 3.50 * totalTransferTime - 0.0013 * (totalRidingDistance * 16) + railFactor;break;
			case TransportMode.drt: utility = -5.14 - 5.41 * totalTravelTime - 0.0004 * ((totalTravelDistance * 300) + 500) + drtFactor;break;    }


		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -11.21 * totalTravelTime;
			bikeFactor = -1.84 - 7.57 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.84 - 7.57 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor - 3.88 - 5.51 * totalTravelTime - 0.00213 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.06 - 8.03 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.96 - 0.145 * totalRidingTime - 3.69 * totalTransferTime - 0.00213 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.55 - 5.14 * totalTravelTime - 0.00213 * ((totalTravelDistance * 430)+ 200);break;    }

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -8.86 * totalTravelTime;
			bikeFactor = -0.95 - 6.17 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -0.95 - 6.17 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor - 1.86 - 5.11 * totalTravelTime - 0.0011 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.264 - 5.45 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.79 - 1.13 * totalRidingTime - 3.13 * totalTransferTime - 0.0011 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -14.43 + 5.05 * totalTravelTime - 0.0011 * ((totalTravelDistance * 430)+ 200);break;    }


		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.16 * totalTravelTime;
			bikeFactor = -1.88 - 5.88 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.88 - 5.88 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor - 2.17 - 5.74 * totalTravelTime - 0.00064 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.16 - 6.51 * totalTravelTime;break;
			case TransportMode.pt: utility = -1.22 - 0.81 * totalRidingTime - 2.26 * totalTransferTime - 0.00064 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.45 - 5.06 * totalTravelTime - 0.00064 * ((totalTravelDistance * 430)+ 200);break;    }

		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.63 * totalTravelTime;
			bikeFactor = -2.35 - 10.36 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -2.35 - 10.36 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor - 4.39 - 5.49 * totalTravelTime - 0.00017 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.34 - 3.79 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.20 + 0.221 * totalRidingTime - 4.00 * totalTransferTime - 0.00017 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -4.97 - 2.05 * totalTravelTime - -0.00017 * ((totalTravelDistance * 430)+ 200);break;    }

		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -11.06 * totalTravelTime;
			bikeFactor = -4.10 - 6.09 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -4.10 - 6.09 * totalTravelTime;
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
			case TransportMode.car: utility = -6.30 - 3.17 * totalTravelTime + 0.00063 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 6.908 - 2.65 * totalTravelTime;break;
			case TransportMode.pt: utility =-4.14 - 0.29 * totalRidingTime - 2.39 * totalTransferTime + 0.00063 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -5.93 - 8.86 * totalTravelTime + 0.00063 * ((totalTravelDistance * 430)+ 200);break;    }

		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance, boolean carAlwaysAvailable, DiscreteModeChoiceTrip trip, String destUrbanContext) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -10.44 * totalTravelTime;
			bikeFactor = -5.34 + 1.25 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -5.34 + 1.25 * totalTravelTime ;
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
			case TransportMode.car: utility = carFactor - 2.09 - 3.00 * totalTravelTime + 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.49 - 7.50 * totalTravelTime;break;
			case TransportMode.pt: utility =-3.35 + 1.09 * totalRidingTime - 3.16 * totalTransferTime + 0.0013 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.29 - 5.81 * totalTravelTime + 0.00013 * ((totalTravelDistance * 430)+ 200);break;    }

		return utility;
	}
}
