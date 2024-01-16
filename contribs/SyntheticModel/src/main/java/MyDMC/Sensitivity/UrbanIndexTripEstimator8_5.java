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

public class UrbanIndexTripEstimator8_5 extends AbstractTripRouterEstimator {
	@Inject
	public UrbanIndexTripEstimator8_5(TripRouter tripRouter, ActivityFacilities facilities,
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
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("CBD")) {
			utility = CBDWorkUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
		}
		if (UrbanContext.equals("Rural")) {
			utility = RuralUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);
		}

//		utility = calcModeUtil(mode, totalTravelTime, totalTravelDistance, totalRidingTime, totalTransferTime, totalRidingDistance, totalTransferDistance, carAlwaysAvailable);

		return utility;
	}


	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("carAvail");
		return "always".equals(carAvailability);			}
	private boolean isNextActivityWork (DiscreteModeChoiceTrip trip){
		return "w".equals(trip.getDestinationActivity().getType());
	}
	private double calcModeUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor =  -9.57 * totalTravelTime;
			bikeFactor = -1.83 - 6.19 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.83 - 6.19 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor -2.41 - 5.50 * totalTravelTime - 0.0016 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.58 - 6.95	 * totalTravelTime;		break;
			case TransportMode.pt: utility = -2.05 - 0.32 * totalRidingTime - 4.30 * totalTransferTime - 0.0015 * (totalRidingDistance * 16) ;break;     }

		return utility;
	}
	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor =  -10.1 * totalTravelTime;
			bikeFactor = -1.69 - 6.75 * totalTravelTime ;
		} else if (totalTravelDistance > 4 && totalTravelDistance <= 10) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.69 - 6.75 * totalTravelTime;
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
			case TransportMode.car: utility = -3.97 - 3.57 * totalTravelTime - 0.0037 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.18 - 6.72 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.55 - 0.28 * totalRidingTime - 3.55 * totalTransferTime - 0.0015 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -4.18 - 6.72 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }


		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -9.12 * totalTravelTime;
			bikeFactor = -1.83 - 5.70 * totalTravelTime;
		} else if (totalTravelDistance > 4 && totalTravelDistance <=  8){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.83 - 5.70 * totalTravelTime;
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
			case TransportMode.car: utility = -1.71 - 5.71 * totalTravelTime - 0.0015 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.22 - 6.88 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.85 - 0.89 * totalRidingTime - 2.74 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -3.22 - 6.88 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -9.12 * totalTravelTime;
			bikeFactor = -1.83 - 5.70 * totalTravelTime;
		} else if (totalTravelDistance > 4 && totalTravelDistance <= 10) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.83 - 5.70 * totalTravelTime;
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
			case TransportMode.car: utility = -1.71 - 5.71 * totalTravelTime - 0.0015 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.22 - 6.88 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.85 - 0.89 * totalRidingTime - 2.74 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -3.22 - 6.88 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }


		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -6.80 * totalTravelTime;
			bikeFactor = -1.22 - 5.74 * totalTravelTime;
		} else if (totalTravelDistance > 4 && totalTravelDistance <=  8){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.22 - 5.74 * totalTravelTime;
		} else {
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = dummyvalue + dummyvalue * totalTravelTime;		}

		switch (mode) {
			case TransportMode.walk: utility = walkFactor    ;break;
			case TransportMode.bike: utility = bikeFactor 	 ;break;
			case TransportMode.car: utility = -1.10 - 5.69 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 2.79 - 6.20 * totalTravelTime;break;
			case TransportMode.pt: utility =-1.02 - 0.95 * totalRidingTime - 6.80 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -2.79 - 6.20 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -11.2 * totalTravelTime;
			bikeFactor = -3.26 - 7.50 * totalTravelTime;
		} else if (totalTravelDistance > 4 && totalTravelDistance <=  8){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -3.26 - 7.50 * totalTravelTime;
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
			case TransportMode.car: utility = -6.74 - 2.03 * totalTravelTime - 0.0014 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 5.89 - 4.40 * totalTravelTime;break;
			case TransportMode.pt: utility =-3.67 + 0.51 * totalRidingTime - 2.60 * totalTransferTime - 0.0006 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -5.89 - 4.40 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -7.48 * totalTravelTime;
			bikeFactor = -1.47 - 5.28 * totalTravelTime;
		} else if (totalTravelDistance > 4 && totalTravelDistance <=  8){
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.47 - 5.28 * totalTravelTime;
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
			case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.35 - 7.65 * totalTravelTime;break;
			case TransportMode.pt: utility =-1.50 - 0.51 * totalRidingTime - 7.31 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -3.35 - 7.65 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 4) {
			walkFactor = -9.57 * totalTravelTime;
			bikeFactor = -1.73 - 6.17 * totalTravelTime ;
		} else if (totalTravelDistance > 4 && totalTravelDistance <=  8){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -1.73 - 6.17 * totalTravelTime ;
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
			case TransportMode.car: utility = -2.36 - 5.61 * totalTravelTime - 0.0020 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.48 - 7.52 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.14 - 0.183 * totalRidingTime - 4.146 * totalTransferTime - 0.0011 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -3.48 - 7.52 * (totalTravelTime * 0.85) - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}
}
