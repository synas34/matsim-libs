/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.data;

import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;

/**
 * @author michalm
 */
public interface Vehicle extends Identifiable<Vehicle> {

	/**
	 * @return the link at which vehicle starts operating (i.e. depot)
	 */
	Link getStartLink();

	void setStartLink(Link link);

	/**
	 * @return the amount of people/goods that can be served/transported at the same time (see:
	 *         {@link Request#getQuantity()})
	 */
	double getCapacity();

	/**
	 * @return (desired) time when the vehicle should start operating (inclusive); can be different from
	 *         {@link Schedule#getBeginTime()}
	 */
	double getServiceBeginTime();

	/**
	 * @return (desired) time by which the vehicle should stop operating (exclusive); can be different from
	 *         {@link Schedule#getEndTime()}
	 */
	double getServiceEndTime();

	/**
	 * See {@link Vehicle#getServiceEndTime()}
	 */
	void setT1(double t1);

	/**
	 * Design comment(s):
	 * <ul>
	 * <li>The Schedule is meant to be changed only by the optimizer. Note, however, that the present design does not
	 * prevent other classes to change it, so be careful to not do that. kai, feb'17
	 * </ul>
	 */
	Schedule getSchedule();

	/**
	 * In the only existing implementation, this re-creates the Schedule object by calling a new constructor.
	 */
	void resetSchedule();
}
