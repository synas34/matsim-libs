/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package ch.sbb.matsim.routing.pt.raptor;

import java.util.List;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.utils.misc.OptionalTime;

import ch.sbb.matsim.routing.pt.raptor.RaptorStopFinder.Direction;

/**
 * A default implementation of {@link RaptorIntermodalAccessEgress} returning a new RIntermodalAccessEgress,
 * which contains a list of legs (same as in the input), the associated travel time as well as the disutility.
 *
 * @author pmanser / SBB
 */
public class DefaultRaptorIntermodalAccessEgress implements RaptorIntermodalAccessEgress {

	@Override
	public RIntermodalAccessEgress calcIntermodalAccessEgress(final List<? extends PlanElement> legs, RaptorParameters params, Person person, Direction direction) {

		double disutility = 0.0;
		double tTime = 0.0;
		for (PlanElement pe : legs) {
			if (pe instanceof Leg) {
				String mode = ((Leg) pe).getMode();
				OptionalTime travelTime = ((Leg) pe).getTravelTime();
				if (travelTime.isDefined()) {
					tTime += travelTime.seconds();
					disutility += travelTime.seconds() * -params.getMarginalUtilityOfTravelTime_utl_s(mode);
				}
			}
			else if (pe instanceof Activity) {
				if (((Activity) pe).getMaximumDuration().isDefined()) {
					tTime += ((Activity) pe).getMaximumDuration().seconds();
				}
			}
		}
		return new RIntermodalAccessEgress(legs, disutility, tTime, direction);


		//		double disutility = 0.0;
//		double tTime = 0.0;
//		boolean hasPtLeg = false; // Flag to track if there is at least one leg of mode "pt"
//
//		// Declare and initialize variables for utility calculations
//		double walkFactor = 0;
//		double bikeFactor = 0;
//		double rideFactor = 0;
//		double carFactor = 0;
//		double dummyValue = -10000000;
//		double totalTravelDistance = 0.0;
//
//		for (PlanElement pe : legs) {
//			if (pe instanceof Leg) {
//				String mode = ((Leg) pe).getMode();
//				OptionalTime travelTime = ((Leg) pe).getTravelTime();
//				tTime += travelTime.seconds();
//				totalTravelDistance += ((Leg) pe).getRoute().getDistance() * 1e-3;
//
//				// Check if the leg is of mode "pt"
//				if (mode.equals("pt")) {
//					hasPtLeg = true; // Set flag to true if there is at least one leg of mode "pt"
//				}
//				double modeDisutility = params.getMarginalUtilityOfTravelTime_utl_s(mode);
//
//				if (travelTime.isDefined()) {
//
//					// Apply utility calculations only when there is a leg of mode "pt"
//					if (hasPtLeg) {
//						// Apply disutility calculation based on mode
//						switch (mode) {
//							case TransportMode.walk:
//								disutility =  -9.83 * tTime;
//								break;
//							case TransportMode.bike, TransportMode.transit_walk:
//								disutility = -3.88 - 800.35 * tTime;
//								break;
//							case TransportMode.car:
//								disutility = - 2.74 - 5.51 * tTime - 0.0011 * (totalTravelDistance * 7);
//								break;
//							case TransportMode.ride:
//								disutility = - 3.59 - 6.91 * tTime;
//								break;
//							case TransportMode.pt:
//								disutility =  -2.19 - 0.38 * tTime - 0.0015 * (totalTravelDistance * 16);
//								break;
//							case TransportMode.drt:
//								disutility = -6.46 - 4.50 * tTime - 0.00013 * (totalTravelDistance * 430);
//								break;
//						}
//						// Multiply disutility by 5
//						disutility *= 1.3;
//					}
//				}
//			} else if (pe instanceof Activity) {
//				if (((Activity) pe).getMaximumDuration().isDefined()) {
//					tTime += ((Activity) pe).getMaximumDuration().seconds();
//				}
//			}
//		}
//		return new RIntermodalAccessEgress(legs, disutility, tTime, direction);
	}






	private boolean isCarAlwaysAvailable (Person person){
		String carAvailability = (String) person.getAttributes().getAttribute("carAvail");
		return "always".equals(carAvailability);
	}
}

