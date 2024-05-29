package TransitCreator.Cleaners;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;


public class MATSimTransitNetworkProcessor {

	public void updatePtLinkSpeed(String networkFilePath, String outputPath) throws Exception {
		// Load the network
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFilePath);

		Network network = scenario.getNetwork();
		double newSpeed = 20.0 ; // 72 km/h to  20 m/s

		// Iterate through all links in the network
		for (Link link : network.getLinks().values()) {
			// Check if 'pt' is in the allowed modes
			if (link.getAllowedModes().contains("pt")) {
				// Set the new speed
				link.setFreespeed(newSpeed);
			}
		}

		// Write the modified network back to the file
		new NetworkWriter(network).write(outputPath);
	}

	public void updateCarLinkSpeed(String networkFilePath, String outputPath) throws Exception {
		// Load the network
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFilePath);

		Network network = scenario.getNetwork();

		// Iterate through all links in the network
		for (Link link : network.getLinks().values()) {
			// Check if 'car' is in the allowed modes
			if (link.getAllowedModes().contains("car")) {
				double currentSpeed = link.getFreespeed();
				double newSpeed = determineNewSpeed(currentSpeed);

				// Set the new speed
				link.setFreespeed(newSpeed);
			}
		}

		// Write the modified network back to the file
		new NetworkWriter(network).write(outputPath);
	}

	private double determineNewSpeed(double currentSpeed) {
		if (currentSpeed == 8.333333333333334 ) return 5.333333333333334;
//		if (currentSpeed == 11.11111111111111) return 8.333333333333334;
//		if (currentSpeed == 12.5) return 11.11111111111111;
//		if (currentSpeed == 16.666666666666668) return 12.5;
//		if (currentSpeed == 22.22222222222222) return 13.88888888888889;
//		if (currentSpeed == 25) return 20.0;


		return currentSpeed; // Default case, no change
	}

	public static void main(String[] args) throws Exception {
		String inputFilePath = "examples/scenarios/Odakyu3/network3.xml"; // Replace with your input file path
		String outputFilePath = "examples/scenarios/Odakyu3/network3.xml"; // Replace with your output file path

		new MATSimTransitNetworkProcessor().updatePtLinkSpeed(inputFilePath, outputFilePath);


	}
}
