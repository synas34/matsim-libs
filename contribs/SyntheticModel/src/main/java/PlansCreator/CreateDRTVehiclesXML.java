package PlansCreator;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTrees;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateDRTVehiclesXML {

	private static final double SQUARE_SIZE = 1000.0;

	public static void main(String[] args) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		double operationStartTime = 0.;
		double operationEndTime = 32 * 3600.;
		int seats = 4;
		int size = 1;
		int count = 0;
		String outputDirectory = "examples/scenarios/UrbanLine/Extension/zoneSuburban/";
		String drtsFile = outputDirectory + "drts" + count + "S" + seats + ".xml";

		// Set up your square densities and decay rates here
		List<Integer> densities = List.of(0, 0, 140, 100, 60);
		List<Double> decayRates = List.of(0.7, 0.5, 0.5, 0.9, 0.3);

		RandomCoordinatesGenerator generator = new RandomCoordinatesGenerator(densities, decayRates);
		List<Coord> coordinates = generator.generateCoords();

		List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
		new MatsimNetworkReader(scenario.getNetwork()).readFile("examples/scenarios/UrbanLine/Extension/zoneSuburban/network_pt.xml");
		Network network = scenario.getNetwork();

		// Filter out PT links and retain only car-accessible links
		List<Link> carAccessibleLinks = network.getLinks().values().stream()
			.filter(link -> link.getAllowedModes().contains("car") && !link.getAllowedModes().contains(TransportMode.pt))
			.collect(Collectors.toList());

		QuadTree<Link> linkQuadTree = buildLinkQuadTree(carAccessibleLinks);

		for (Coord coord : coordinates) {
			// Ensure that the nearest link allows 'car' mode and is not a PT link
			Link startLink = linkQuadTree.getClosest(coord.getX(), coord.getY());
			addVehicles(vehicles, count, startLink, size, seats, operationStartTime, operationEndTime);
			count += size;
		}

		new FleetWriter(vehicles.stream()).write(drtsFile);
	}
	private static QuadTree<Link> buildLinkQuadTree(List<Link> links) {
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (Link link : links) {
			if (link.getCoord().getX() < minx) { minx = link.getCoord().getX(); }
			if (link.getCoord().getY() < miny) { miny = link.getCoord().getY(); }
			if (link.getCoord().getX() > maxx) { maxx = link.getCoord().getX(); }
			if (link.getCoord().getY() > maxy) { maxy = link.getCoord().getY(); }
		}

		QuadTree<Link> quadTree = new QuadTree<>(minx, miny, maxx, maxy);
		for (Link link : links) {
			quadTree.put(link.getCoord().getX(), link.getCoord().getY(), link);
		}
		return quadTree;
	}
	public static void addVehicles(List<DvrpVehicleSpecification> vehicles, int count, Link link, int size, int seats, double operationStartTime, double operationEndTime) {
		for (int i = 0; i < size; i++) {
			vehicles.add(ImmutableDvrpVehicleSpecification.newBuilder()
				.id(Id.create("drt" + count, DvrpVehicle.class))
				.startLinkId(link.getId())
				.capacity(seats)
				.serviceBeginTime(operationStartTime)
				.serviceEndTime(operationEndTime)
				.build());
			count++;
		}
	}

	public static class Coordinate {
		public final double x;
		public final double y;

		public Coordinate(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	// The rest of your RandomCoordinatesGenerator class and Coordinate class here
}
