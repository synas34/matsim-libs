package NetworkCreator;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;

import java.util.Arrays;
import java.util.List;

public class NetworkReverser {

	private static final List<String> NAMES_TO_REMOVE = Arrays.asList(
		"Hachiōji", "Katakura", "Hachiōji-Minamino", "Aihara", "Hashimoto",
		 "Nagatsuta", "Nakayama", "Kamoi",
		"Shin-Yokohama", "Kikuna", "Higashi-Kanagawa", "Kashiwadai",
		"Sagamino",
		"Sagami-Ōtsuka",
		"Yamato",
		"Seya",
		"Mitsukyō",
		"Kibōgaoka",
		"Futamata-gawa",
		"Tsurugamine",
		"Nishiya",
		"Hoshikawa",
		"Yokohama"
	);

	public void cleanNetwork(String inputPath, String outputPath) {
		Network network = NetworkUtils.readNetwork(inputPath);

		// Remove nodes based on id
		for (Node node : network.getNodes().values()) {
			for (String name : NAMES_TO_REMOVE) {
				if (node.getId().toString().contains(name)) {
					network.removeNode(node.getId());
					break;
				}
			}
		}

		// Remove links based on id
		for (Link link : network.getLinks().values()) {
			for (String name : NAMES_TO_REMOVE) {
				if (link.getId().toString().contains(name)) {
					network.removeLink(link.getId());
					break;
				}
			}
		}

		new NetworkWriter(network).write(outputPath);
	}

	public static void main(String[] args) {
		String inputPath = "examples/scenarios/Odakyu1/test/network.xml";
		String outputPath = "examples/scenarios/Odakyu1/test/network.xml";

		new NetworkReverser().cleanNetwork(inputPath, outputPath);
	}
}
