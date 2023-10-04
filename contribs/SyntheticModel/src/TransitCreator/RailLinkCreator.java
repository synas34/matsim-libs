package TransitCreator;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import java.util.HashSet;
import java.util.Set;

public class RailLinkCreator {

	public static void main(String[] args) {
		RailLinkCreator creator = new RailLinkCreator();
		Network network = NetworkUtils.readNetwork("C:\\Users\\snasi\\IdeaProjects\\matsim-libs\\examples\\scenarios\\UrbanLine\\network22.xml");

		Coord coordStart = new Coord(100, 500);
		double[] distances = {300, 333, 333, 400, 400, 450,  1000, 1000, 1500, 2000};
		int numOfLinks = 10;

		if (distances.length != numOfLinks) {
			System.out.println("The number of distances provided does not match the number of links specified.");
			return;
		}

		for (int i = 0; i < numOfLinks; i++) {
			Coord coordEnd = new Coord(coordStart.getX() + distances[i], coordStart.getY());
			creator.createRailLink(network, coordStart, coordEnd);
			coordStart = coordEnd;
		}

		new NetworkWriter(network).write("C:\\Users\\snasi\\IdeaProjects\\matsim-libs\\examples\\scenarios\\UrbanLine\\pathToSave.xml");
	}

	public void createRailLink(Network network, Coord coordStart, Coord coordEnd) {
		// Create the original link
		Node fromNode = network.getFactory().createNode(Id.createNodeId("from_" + coordStart.getX()), coordStart);
		Node toNode = network.getFactory().createNode(Id.createNodeId("to_" + coordEnd.getX()), coordEnd);
		Link link = network.getFactory().createLink(Id.createLinkId("link-id_" + coordStart.getX() + "_" + coordEnd.getX()), fromNode, toNode);

		Set<String> allowedModes = new HashSet<>();
		allowedModes.add("train");
		link.setAllowedModes(allowedModes);

		network.addNode(fromNode);
		network.addNode(toNode);
		network.addLink(link);

		// Create the reverse link
		Link reverseLink = network.getFactory().createLink(Id.createLinkId("link-id_" + coordStart.getX() + "_" + coordEnd.getX() + "_r"), toNode, fromNode);
		reverseLink.setAllowedModes(allowedModes);
		network.addLink(reverseLink);

		// Create Station Link
		//Place this later

	}

}
