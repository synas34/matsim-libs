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
				utility = UrbanWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			}
		}
		if (UrbanContext.equals("Suburban")) {
			if (nextActivityIsWork) {
				// Utility calculations when the next activity is work
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
			} else {
				// Standard utility calculations
				utility = SuburbanWorkUtil(mode, totalTravelTime, totalTravelDistance, carAlwaysAvailable);
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
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -6.88 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.17 - 6.02 * totalTravelTime;break;
				case TransportMode.car: utility = -3.15 - 3.48 * totalTravelTime - 0.0055 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.16 - 6.71 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.92 - 0.26 * totalTravelTime - 0.0010 * (totalTravelDistance * 16) ;break;     }
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.17 - 6.02 * totalTravelTime;break;
				case TransportMode.car: utility = -3.15 - 3.48 * totalTravelTime - 0.0055 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.16 - 6.71 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.92 - 0.26 * totalTravelTime - 0.0010 * (totalTravelDistance * 16) ;break;     }
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -3.15 - 3.48 * totalTravelTime - 0.0055 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -4.16 - 6.71 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.92 - 0.26 * totalTravelTime - 0.0010 * (totalTravelDistance * 16) ;break;     }
		}
		return utility;
	}

	private double UrbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -10.64 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.66 - 8.58 * totalTravelTime;break;
				case TransportMode.car: utility = -2.24 - 8.78 * totalTravelTime + 0.0037 * (totalTravelDistance * 7); break;
				case TransportMode.ride: utility = -4.04 - 7.60 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.88 - 2.46 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;		}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -2.24 - 8.78 * totalTravelTime + 0.0037 * (totalTravelDistance * 7); break;
				case TransportMode.ride: utility = -4.04 - 7.60 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.88 - 2.46 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;		}
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -2.24 - 8.78 * totalTravelTime + 0.0037 * (totalTravelDistance * 7); break;
				case TransportMode.ride: utility = -4.04 - 7.60 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.88 - 2.46 * totalTravelTime - 0.0011 * (totalTravelDistance * 16) ;break;		}
		}
		return utility;
	}

	private double SuburbanWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -6.21 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.30 - 5.12 * totalTravelTime;break;
				case TransportMode.car: utility = -1.08 - 4.98 * totalTravelTime - 0.0035 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.78 - 6.78 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.79 - 0.93 * totalTravelTime - 0.00088 * (totalTravelDistance * 16) ;break;		}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.30 - 5.12 * totalTravelTime;break;
				case TransportMode.car: utility = -1.08 - 4.98 * totalTravelTime - 0.0035 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.78 - 6.78 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.79 - 0.93 * totalTravelTime - 0.00088 * (totalTravelDistance * 16) ;break;		}
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -1.08 - 4.98 * totalTravelTime - 0.0035 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -2.78 - 6.78 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.79 - 0.93 * totalTravelTime - 0.00088 * (totalTravelDistance * 16) ;break;		}
		}
		return utility;
	}

	private double SuburbanOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.car: utility = -2.14 - 8.83 * totalTravelTime + 0.0030 * (totalTravelDistance * 7); break;
				case TransportMode.pt: utility = -1.06 - 0.71 * totalTravelTime - 0.0030 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -10.01 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.49 - 9.21 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.73 - 7.81 * totalTravelTime;break;			}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.car: utility = -2.14 - 8.83 * totalTravelTime + 0.0030 * (totalTravelDistance * 7); break;
				case TransportMode.pt: utility = -1.06 - 0.71 * totalTravelTime - 0.0030 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -10000000.01 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.49 - 9.21 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.73 - 7.81 * totalTravelTime;break;			}
		} else {
			switch (mode) {
				case TransportMode.car: utility = -2.14 - 8.83 * totalTravelTime + 0.0030 * (totalTravelDistance * 7); break;
				case TransportMode.pt: utility = -1.06 - 0.71 * totalTravelTime - 0.0030 * (totalTravelDistance * 16) ;break;
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 9.21 * totalTravelTime;break;
				case TransportMode.ride: utility = -3.73 - 7.81 * totalTravelTime;break;			}
		}
		return utility;
	}

	private double CBDWorkUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -7.35 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -2.53 - 6.53 * totalTravelTime;break;
				case TransportMode.car: utility = -5.37 - 2.80 * totalTravelTime - 0.0025 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -5.90 - 4.41 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.46 + 0.30 * totalTravelTime - 0.00011 * (totalTravelDistance * 16) ;break;		}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -2.53 - 6.53 * totalTravelTime;break;
				case TransportMode.car: utility = -5.37 - 2.80 * totalTravelTime - 0.0025 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -5.90 - 4.41 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.46 + 0.30 * totalTravelTime - 0.00011 * (totalTravelDistance * 16) ;break;		}
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -5.37 - 2.80 * totalTravelTime - 0.0025 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -5.90 - 4.41 * totalTravelTime;break;
				case TransportMode.pt: utility = -1.46 + 0.30 * totalTravelTime - 0.00011 * (totalTravelDistance * 16) ;break;		}
		}
		return utility;
	}

	private double CBDOtherUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -7.48 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;		}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.47 - 5.28 * totalTravelTime;break;
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;		}
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -1.96 - 5.3 * totalTravelTime - 0.0013 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.35 - 7.65 * totalTravelTime;break;
				case TransportMode.pt: utility = -2.7 - 1.17 * totalTravelTime - 0.0013 * (totalTravelDistance * 2) ;break;		}
		}
		return utility;
	}

	private double RuralUtil(String mode, double totalTravelTime, double totalTravelDistance,  boolean carAlwaysAvailable) {
		double utility = 0;
		if (totalTravelDistance <= 3){
			switch (mode) {
				case TransportMode.walk: utility = -7.00 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.24 - 5.83 * totalTravelTime;break;
				case TransportMode.car: utility = -1.75 - 5.54 * totalTravelTime - 0.0034 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.21 - 7.81 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.65 - 0.23 * totalTravelTime - 0.0008 * (totalTravelDistance * 16) ;break;}
		} else if (totalTravelDistance >= 3 & totalTravelDistance <= 7)  {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -1.24 - 5.83 * totalTravelTime;break;
				case TransportMode.car: utility = -1.75 - 5.54 * totalTravelTime - 0.0034 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.21 - 7.81 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.65 - 0.23 * totalTravelTime - 0.0008 * (totalTravelDistance * 16) ;break;}
		} else {
			switch (mode) {
				case TransportMode.walk: utility = -10000000 * totalTravelTime    ;break;
				case TransportMode.bike: utility = -10000000 - 10000000 * totalTravelTime;break;
				case TransportMode.car: utility = -1.75 - 5.54 * totalTravelTime - 0.0034 * (totalTravelDistance * 10); break;
				case TransportMode.ride: utility = -3.21 - 7.81 * totalTravelTime;break;
				case TransportMode.pt: utility = -0.65 - 0.23 * totalTravelTime - 0.0008 * (totalTravelDistance * 16) ;break;}
		}
		return utility;
	}
}
