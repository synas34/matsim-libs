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

		// Iterate over all links and increase the freespeed to 25.5555555555 for those with pt mode
		for (Link link : network.getLinks().values()) {
			Set<String> allowedModes = link.getAllowedModes();
			if (allowedModes.contains("pt")) {
				link.setFreespeed(25.5555555555);
				link.setCapacity(2000);
			}
		}

		// Write the modified network back to the output path
		new NetworkWriter(network).write(outputPath);
	}

	public static void main(String[] args) {
		// Hardcoded paths for demonstration
		String inputNetworkPath = "examples/scenarios/UrbanLine/Lastditch/FMLM4/network_pt.xml";
		String outputPath = "examples/scenarios/UrbanLine/Lastditch/FMLM4/network_pt.xml";

		new ModeModifier().modifyLinkModes(inputNetworkPath, outputPath);
	}
}
