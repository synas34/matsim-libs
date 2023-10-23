package NetworkCreator;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;

import java.util.Set;

public class ModeModifier {

	public void modifyLinkModes(String inputNetworkPath, String outputPath) {
		// Load the existing network
		Network network = NetworkUtils.readNetwork(inputNetworkPath);

		// Iterate over all links and modify the modes attribute for those with freespeed less than 20 and without pt
		for (Link link : network.getLinks().values()) {
			double freespeed = link.getFreespeed();
			Set<String> allowedModes = link.getAllowedModes();

			if (allowedModes.contains("publicTransport")) {
				link.setAllowedModes(Set.of("pt"));
			}
		}

		// Write the modified network back to the output path
		new NetworkWriter(network).write(outputPath);
	}

	public static void main(String[] args) {
		// Hardcoded paths for demonstration
		String inputNetworkPath = "examples/scenarios/Odakyu1/network.xml";
		String outputPath = "examples/scenarios/Odakyu1/test/network.xml";

		new ModeModifier().modifyLinkModes(inputNetworkPath, outputPath);
	}
}
