/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.run;

import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

public class RunTaxiScenario {
	public static void run(String configFile, boolean otfvis) {
		Config config = ConfigUtils.loadConfig(configFile, new TaxiConfigGroup(), new DvrpConfigGroup(),
				new OTFVisConfigGroup());
		config.controler().setLastIteration(3);
		TaxiControlerCreator.createControler(config, otfvis).run();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("RunTaxiScenario needs one argument: path to the configuration file");
		}
		RunTaxiScenario.run(args[0], false);
	}
}
