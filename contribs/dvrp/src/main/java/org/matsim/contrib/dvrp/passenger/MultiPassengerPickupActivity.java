/* *********************************************************************** *
 *                                                                         *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.dvrp.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.*;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class MultiPassengerPickupActivity extends AbstractDynActivity implements PassengerPickupActivity {
	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<? extends PassengerRequest> requests;
	private final double pickupDuration;

	private double maxRequestT0;

	private int passengersAboard;
	private double endTime;

	public MultiPassengerPickupActivity(PassengerEngine passengerEngine, DynAgent driver, StayTask pickupTask,
			Set<? extends PassengerRequest> requests, double pickupDuration, String activityType) {
		super(activityType);

		if (requests.size() > pickupTask.getSchedule().getVehicle().getCapacity()) {
			throw new IllegalStateException("Number of requests exceeds number of seats");
		}

		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.requests = requests;
		this.pickupDuration = pickupDuration;

		double now = pickupTask.getBeginTime();

		for (PassengerRequest request : requests) {
			if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
				passengersAboard++;
			}

			if (request.getEarliestStartTime() > maxRequestT0) {
				maxRequestT0 = request.getEarliestStartTime();
			}
		}

		if (passengersAboard == requests.size()) {
			endTime = now + pickupDuration;
		} else {
			setEndTimeIfWaitingForPassengers(now);
		}
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public void doSimStep(double now) {
		if (passengersAboard < requests.size()) {
			setEndTimeIfWaitingForPassengers(now);// TODO use DynActivityEngine.END_ACTIVITY_LATER instead?
		}
	}

	private void setEndTimeIfWaitingForPassengers(double now) {
		endTime = Math.max(now, maxRequestT0) + pickupDuration;

		if (endTime == now) {// happens only if pickupDuration == 0
			endTime += 1; // to prevent the driver departing now (before picking up the passenger)
		}
	}

	@Override
	public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
		PassengerRequest request = getRequestForPassenger(passenger);

		if (request == null) {
			throw new IllegalArgumentException("I am waiting for different passengers!");
		}

		if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
			passengersAboard++;
		} else {
			throw new IllegalStateException("The passenger is not on the link or not available for departure!");
		}

		if (passengersAboard == requests.size()) {
			endTime = now + pickupDuration;
		}
	}

	private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
		for (PassengerRequest request : requests) {
			if (passenger == request.getPassenger()) {
				return request;
			}
		}

		return null;
	}
}
