/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package playground.southafrica.kai.gauteng;

import org.matsim.api.core.v01.Id;

/**
 * @author nagel
 *
 */
public interface UtilityOfDistanceI {
	double getMarginalUtilityOfDistance( Id personId ) ;
	// yyyy just "person" is probably not enough.  "vehicle"?  kai, nov'13

	double getMarginalUtilityOfDistanceMAX();
}
