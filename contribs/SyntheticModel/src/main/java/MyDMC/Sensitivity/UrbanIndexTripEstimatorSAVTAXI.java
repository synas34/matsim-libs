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

public class UrbanIndexTripEstimatorSAVTAXI extends AbstractTripRouterEstimator {
	@Inject
	public UrbanIndexTripEstimatorSAVTAXI(TripRouter tripRouter, ActivityFacilities facilities,
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

		if (totalTravelDistance <= 3) {
			walkFactor =  -9.83 * totalTravelTime;
			bikeFactor = -1.88 - 6.35 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.88 - 6.35 * totalTravelTime;
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
			case TransportMode.car: utility = carFactor -2.74 - 5.51 * totalTravelTime - 0.0011 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.59 - 6.91	 * totalTravelTime;		break;
			case TransportMode.pt: utility = -2.19 - 0.38 * totalRidingTime - 4.13 * totalTransferTime - 0.0015 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.46 - 4.50 * totalTravelTime - 0.00013 * (totalTravelDistance * 70); break; }

			return utility;
	}

	private double UrbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -10.43 * totalTravelTime;
			bikeFactor = -1.77 - 6.85 * totalTravelTime ;
		} else if (totalTravelDistance > 3 && totalTravelDistance <= 7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.77 - 6.85 * totalTravelTime;
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
			case TransportMode.car: utility = -3.90 - 3.56 * totalTravelTime - 0.0045 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 4.05 - 7.32 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.55 - 0.19 * totalRidingTime - 3.62 * totalTransferTime - 0.0018 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.33 - 5.06 * totalTravelTime - 0.00010 * (totalTravelDistance * 70); break;    }


		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.12 * totalTravelTime;
			bikeFactor = -1.83 - 5.70 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
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
			case TransportMode.drt: utility = -3.22 - 6.88 * totalTravelTime - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -9.03 * totalTravelTime;
			bikeFactor = -1.79 - 5.66 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7) {
			walkFactor = dummyvalue + dummyvalue * totalTravelTime;
			bikeFactor = -1.79 - 5.66 * totalTravelTime;
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
			case TransportMode.car: utility = -1.67 - 5.93 * totalTravelTime - 0.00046 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.22 - 6.77 * totalTravelTime;break;
			case TransportMode.pt: utility = -2.79 - 0.93 * totalRidingTime - 2.74 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.63 - 4.98 * totalTravelTime - 0.00013 * (totalTravelDistance * 70); break;    }


		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -6.80 * totalTravelTime;
			bikeFactor = -1.22 - 5.74 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
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
			case TransportMode.drt: utility = -2.79 - 6.20 * totalTravelTime - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}
	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -10.53 * totalTravelTime;
			bikeFactor = -3.90 - 6.26 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
			walkFactor = dummyvalue * totalTravelTime;
			bikeFactor = -3.90 - 6.26 * totalTravelTime;
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
			case TransportMode.car: utility = -6.41 - 2.74 * totalTravelTime - 0.0013 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 6.31 - 3.04 * totalTravelTime;break;
			case TransportMode.pt: utility = -4.08 + 0.30 * totalRidingTime - 2.15 * totalTransferTime - 0.0010 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -5.74 - 7.82 * totalTravelTime - 0.0000001 * (totalTravelDistance * 70); break;    }

		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
		double utility = 0;
		double dummyvalue = -10000000;
		double walkFactor = 0;
		double bikeFactor = 0;
		double rideFactor = 0;
		double carFactor = 0;

		if (totalTravelDistance <= 3) {
			walkFactor = -7.48 * totalTravelTime;
			bikeFactor = -1.47 - 5.28 * totalTravelTime;
		} else if (totalTravelDistance > 3 && totalTravelDistance <=  7){
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
			case TransportMode.drt: utility = -3.35 - 7.65 * totalTravelTime - 0.0010 * (totalTravelDistance * 70); break;    }

		return utility;
	}


	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance, double totalRidingTime, double totalTransferTime, double totalRidingDistance, double totalTransferDistance,boolean carAlwaysAvailable) {
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
			case TransportMode.car: utility = -2.38 - 5.60 * totalTravelTime - 0.0020 * (totalTravelDistance * 7); break;
			case TransportMode.ride: utility = rideFactor - 3.49 - 7.50 * totalTravelTime;break;
			case TransportMode.pt: utility =-2.18 - 0.17 * totalRidingTime - 4.08 * totalTransferTime - 0.0011 * (totalRidingDistance * 16) ;break;
			case TransportMode.drt: utility = -6.29 - 5.81 * totalTravelTime - 0.00013 * (totalTravelDistance * 70); break;    }

		return utility;
	}
}
