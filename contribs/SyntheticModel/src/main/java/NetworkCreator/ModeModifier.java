package NetworkCreator;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.run.NetworkCleaner;

import java.util.HashSet;
import java.util.Set;

public class ModeModifier {

	public void modifyLinkModes(String inputNetworkPath, String outputPath) {
		// Load the existing network
		Network network = NetworkUtils.readNetwork(inputNetworkPath);

		// Iterate over all links and add "ride" to the allowed modes
		for (Link link : network.getLinks().values()) {
			Set<String> currentAllowedModes = link.getAllowedModes();
			// Create a new HashSet to avoid UnsupportedOperationException
			Set<String> newAllowedModes = new HashSet<>(currentAllowedModes);
			if (newAllowedModes.contains("car")) {
				newAllowedModes.add("ride");
				link.setAllowedModes(newAllowedModes);
			}
		}

		// Write the modified network back to the output path
		new NetworkWriter(network).write(outputPath);
	}

	public static void main(String[] args) {
		// Hardcoded paths for demonstration
		String inputNetworkPath = "examples/scenarios/Odakyu3/network.xml";
		String outputPath = "examples/scenarios/Odakyu3/network2.xml";

		new ModeModifier().modifyLinkModes(inputNetworkPath, outputPath);
	}
}
