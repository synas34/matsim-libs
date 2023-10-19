package TransitCreator;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class RailScheduleCreator {




	public void generateSchedule(String scenarioPath, String[] times, String[] vehicleRefIds, double[] distances) {

		// Initialise scenario

		// Add Vehicles, rail specification
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Id<VehicleType> typeId = Id.create("rail",  VehicleType.class);
		VehicleType railtype = scenario.getVehicles().getFactory().createVehicleType(typeId);
		railtype.setNetworkMode(TransportMode.pt); // set rail vehicles to pt
		railtype.getCapacity().setSeats(333); // set seating and standing room
		railtype.getCapacity().setStandingRoom(667);
		railtype.setLength(36);

		new MatsimVehicleWriter(scenario.getTransitVehicles()).writeFile(scenarioPath + "/transitVehicles.xml");

		// Add Network Elements
		List<Id<Link>> linkIds = new ArrayList<>();
		List<Id<Link>> linkIds_r = new ArrayList<>();
		Network network = NetworkUtils.readNetwork(scenarioPath + "/network.xml");
		Coord coordStart = new Coord(-6100, 500);
		Coord initialExtraCoord = new Coord(coordStart.getX() - 50, coordStart.getY());
		// Create the starting node to act as a station for turn around
		Node InitialStation = network.getFactory().createNode(Id.createNodeId("station" + initialExtraCoord.getX()), initialExtraCoord);
		network.addNode(InitialStation);  // Add this node to the network
		Node previousNode = network.getFactory().createNode(Id.createNodeId("station" + coordStart.getX()), coordStart);
		network.addNode(previousNode);  // Add this node to the network
		createRailLink(network, InitialStation, previousNode,  linkIds, linkIds_r);

		// Iterate to create the main nodes and links
        for (double distance : distances) {
            // Calculate the end coordinate for the current link
            Coord coordEnd = new Coord(coordStart.getX() + distance, coordStart.getY());
            String nodeId = "station" + coordEnd.getX();
            System.out.println("Creating node with ID: " + nodeId + " and Coordinates: (" + coordEnd.getX() + ", " + coordEnd.getY() + ")");
            Node currentNode = network.getFactory().createNode(Id.createNodeId(nodeId), coordEnd);
            network.addNode(currentNode);
            createRailLink(network, previousNode, currentNode, linkIds, linkIds_r);  // Call RailLink to create forward and reverse link
			previousNode = currentNode;
            coordStart = coordEnd;
        }

		// Create a final extra node to the right of the last coordEnd
		Coord finalExtraCoord = new Coord(coordStart.getX() + 50, coordStart.getY());
		Node finalExtraNode = network.getFactory().createNode(Id.createNodeId("station" + finalExtraCoord.getX()), finalExtraCoord);
		network.addNode(finalExtraNode);
		createRailLink(network, previousNode, finalExtraNode, linkIds, linkIds_r);

		// Write the modified network back to the XML file
		new NetworkWriter(network).write(scenarioPath + "/network_pt.xml");

		Config config = ConfigUtils.loadConfig(scenarioPath + "/config.xml");
		scenario = ScenarioUtils.loadScenario(config);

		// Access the schedule factory to create and modify transit components
		TransitScheduleFactory scheduleFactory = scenario.getTransitSchedule().getFactory();

		// Determine the first and last link IDs from the provided lists
		Collections.reverse(linkIds_r);
		Id<Link> startLinkId = linkIds.get(0);
		Id<Link> endLinkId = linkIds.get(linkIds.size() - 1);
		Id<Link> StartLinkId_r = linkIds_r.get(0);
		Id<Link> EndLinkId_r = linkIds_r.get(linkIds_r.size() - 1);
		System.out.println("Total links: " + linkIds.size());
		System.out.println("Total links: " + linkIds_r.size());
		// Extracting intermediate links (excluding the first and the last)
		List<Id<Link>> intermediateLinkIds = linkIds.subList(1, linkIds.size() - 1);
		List<Id<Link>> intermediateLinkIds_r = linkIds_r.subList(1, linkIds_r.size() - 1);

		// Create a network route from the provided link lists
		NetworkRoute networkRoute = RouteUtils.createLinkNetworkRouteImpl(startLinkId, intermediateLinkIds, endLinkId);
		NetworkRoute networkRoute_r = RouteUtils.createLinkNetworkRouteImpl(StartLinkId_r, intermediateLinkIds_r, EndLinkId_r);
		System.out.println("Total links: " +  scenario.getNetwork().getLinks().get("402623_402624"));


		System.out.println("Total links: " +  scenario.getNetwork().getLinks().get("402623_402624"));


		// Create stop facilities for both forward and reverse routes
		List<TransitStopFacility> stopFacilities = StopfacilityCreator(scenario,scheduleFactory, linkIds);
		System.out.println("Total stop facilities: " + scenario.getTransitSchedule().getFacilities().size());
		List<TransitRouteStop> transitRouteStops = createTransitRouteStopsForFacilities(scenario,scheduleFactory, stopFacilities);

		List<TransitStopFacility> stopFacilities_r = StopfacilityCreator(scenario,scheduleFactory, linkIds_r);
		List<TransitRouteStop> transitRouteStops_r = createTransitRouteStopsForFacilities(scenario, scheduleFactory, stopFacilities_r);

		// Create transit routes using the stop facilities and network routes
		TransitRoute transitRoute = scheduleFactory.createTransitRoute(Id.create("Suburbs", TransitRoute.class), networkRoute, transitRouteStops, "pt");
		TransitRoute transitRoute_r = scheduleFactory.createTransitRoute(Id.create("Centre", TransitRoute.class), networkRoute_r, transitRouteStops_r, "pt");

		int halfIndex = vehicleRefIds.length / 2;

		String[] firstHalfVehicleRefIds = Arrays.copyOfRange(vehicleRefIds,0, halfIndex);
		String[] secondHalfVehicleRefIds = Arrays.copyOfRange(vehicleRefIds, halfIndex, vehicleRefIds.length);

// Populate the departures for the created routes using the first half of vehicleRefIds
		DepartureCreator(scheduleFactory, transitRoute, times, firstHalfVehicleRefIds);

// Modify vehicle references and then populate departures for the reverse route using the second half
		DepartureCreator(scheduleFactory, transitRoute_r, times, secondHalfVehicleRefIds);


		// Group the routes under a transit line and add them to the scenario
		TransitLine transitLine = scheduleFactory.createTransitLine(Id.create("Shuttle", TransitLine.class));
		transitLine.addRoute(transitRoute);
		transitLine.addRoute(transitRoute_r);
		scenario.getTransitSchedule().addTransitLine(transitLine);

		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(scenarioPath + "/transitschedule.xml");

	}

	/**
	 * Load an existing network file and a CSV file with station names and coordinates,
	 * then adds nodes to the network corresponding with each row of the CSV and generates links connecting them.
	 *
	 * @param networkFilePath Path to the existing network XML file.
	 * @param csvFilePath     Path to the CSV file with station names and coordinates.
	 * @throws IOException If there's a problem reading the files.
	 */
	public void loadFromCsvAndGenerateLinks(String networkFilePath, String csvFilePath) throws IOException {
		Network network = NetworkUtils.readNetwork(networkFilePath);

		// Read the CSV file
		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String line;
			Node previousNode = null;

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Splits on commas outside of quotes
				if (parts.length != 4) {
					System.out.println("Unexpected format in line: " + line);
					continue; // Skip processing this line and move to the next
				}
				String stationName = parts[0].replace("\"", "").trim();
				String[] coordinates = parts[1].replace("\"", "").split(",");
				double linkLength = 1.0; // Default value

				if (coordinates.length != 2) {
					System.out.println("Unexpected coordinates format in line: " + line);
					continue; // Skip processing this line and move to the next
				}

				double latitude = Double.parseDouble(coordinates[0].trim());
				double longitude = Double.parseDouble(coordinates[1].trim());

				// Create node for the current station
				Coord coord = new Coord(longitude, latitude);
				Node currentNode = network.getFactory().createNode(Id.createNodeId(stationName), coord);
				network.addNode(currentNode);

				// Create a link from the current node to itself with length 1
				createLinkWithLength(network, currentNode, currentNode, 1.0);

				// If there's a previous node, create links between the two nodes
				if (previousNode != null) {
					if (!parts[3].trim().isEmpty() && !parts[3].trim().equals("-")) {
						linkLength = Double.parseDouble(parts[3].trim()) * 1000; // Convert km to meters
					} else {
						// Handle the case when the distance is "-" or empty
						System.out.println("Invalid or missing link length for stations: " + stationName);
						continue; // Or set a default linkLength value if you prefer
					}
					createLinkWithLength(network, previousNode, currentNode, linkLength); // Original direction link
					createLinkWithLength(network, currentNode, previousNode, linkLength); // Reverse direction link
				}
				previousNode = currentNode;
			}
		}

		// Write the modified network back to the XML file
		new NetworkWriter(network).write(networkFilePath);
	}

	// The function to create a link with specified length
	private void createLinkWithLength(Network network, Node fromNode, Node toNode, double length) {
		Link link = network.getFactory().createLink(Id.createLinkId(fromNode.getId() + "_" + toNode.getId()), fromNode, toNode);
		link.setLength(length);

		// Setting specified attributes for the link
		link.setCapacity(9999);
		link.setFreespeed(20.0 / 3.6); // Convert km/h to m/s
		Set<String> modes = new HashSet<>();
		modes.add("pt");
		link.setAllowedModes(modes);

		network.addLink(link);
	}
	private void createOneRailLink(Network network, Node fromNode, Node toNode) {
		// Create forward link
		Id<Link> linkIdForward = Id.createLinkId(fromNode.getId() + "_" + toNode.getId());
		Link linkForward = network.getFactory().createLink(linkIdForward, fromNode, toNode);
		network.addLink(linkForward);

		// Create reverse link
		Id<Link> linkIdReverse = Id.createLinkId(toNode.getId() + "_" + fromNode.getId());
		Link linkReverse = network.getFactory().createLink(linkIdReverse, toNode, fromNode);
		network.addLink(linkReverse);
	}
	public void createRailLink(Network network, Node fromNode, Node toNode, List<Id<Link>> linkIds, List<Id<Link>> linkIds_r) {
		// Create the original link
		Link link = network.getFactory().createLink(Id.createLinkId("link-id_" + fromNode.getId() + "_" + toNode.getId()), fromNode, toNode);
		Set<String> allowedModes = new HashSet<>();
		allowedModes.add("pt");
		link.setAllowedModes(allowedModes);
		link.setCapacity(1000);
		link.setFreespeed(12);
		link.setNumberOfLanes(2);
		network.addLink(link);
		linkIds.add(link.getId()); // Add the link ID to linkIds list

		// Create the reverse link
		Link reverseLink = network.getFactory().createLink(Id.createLinkId("link-id_" + fromNode.getId() + "_" + toNode.getId() + "_r"), toNode, fromNode);
		reverseLink.setAllowedModes(allowedModes);
		reverseLink.setCapacity(1000);
		reverseLink.setFreespeed(12);
		reverseLink.setNumberOfLanes(2);
		network.addLink(reverseLink);
		linkIds_r.add(reverseLink.getId()); // Add the reverse link ID to linkIds_r list

		System.out.println("Created Link: " + link.getId());
	}

	public static List<TransitStopFacility> StopfacilityCreator(Scenario scenario, TransitScheduleFactory scheduleFactory, List<Id<Link>> linkIds) {
		List<TransitStopFacility> createdStopFacilities = new ArrayList<>();

		for (Id<Link> linkId : linkIds) {
			Link link = scenario.getNetwork().getLinks().get(linkId);
			Coord stopCoord = link.getFromNode().getCoord();
			Id<TransitStopFacility> stopId = Id.create("stop_" + linkId.toString(), TransitStopFacility.class);
			TransitStopFacility stopFacility = scheduleFactory.createTransitStopFacility(stopId, stopCoord, false);
			stopFacility.setLinkId(linkId); // Associate the stop with the link
			// Add the stop facility to the scenario's transit schedule
			scenario.getTransitSchedule().addStopFacility(stopFacility);
			createdStopFacilities.add(stopFacility);
			System.out.println("Adding stop facility for link ID: " + linkId);
			System.out.println("Total stop facilities: " + scenario.getTransitSchedule().getFacilities().size());

		}
		return createdStopFacilities;
	}

	public static List<TransitRouteStop> createTransitRouteStopsForFacilities(Scenario scenario, TransitScheduleFactory scheduleFactory, List<TransitStopFacility> stopFacilities) {
		List<TransitRouteStop> transitRouteStops = new ArrayList<>();

		// Assuming a fixed dwell time of 0 seconds at each stop and the same arrival and departure offsets for simplicity
		for (TransitStopFacility stopFacility : stopFacilities) {
			TransitRouteStop transitRouteStop = scheduleFactory.createTransitRouteStop(stopFacility, 0, 0);
			transitRouteStops.add(transitRouteStop);
		}
		System.out.println("Total stop facilities: " + scenario.getTransitSchedule().getFacilities().size());
		return transitRouteStops;
	}

	private static void DepartureCreator(TransitScheduleFactory scheduleFactory, TransitRoute transitRoute_r, String[] times, String[] vehicleRefIds) {
		for (int i = 0; i < times.length; i++) {
			Id<Departure> departureId = Id.create("d_" + (i + 1), Departure.class);
			double departureTime = Time.parseTime(times[i]);
			Id<Vehicle> vehicleId = Id.create(vehicleRefIds[i + 1], Vehicle.class);
			Departure departure = scheduleFactory.createDeparture(departureId, departureTime);
			departure.setVehicleId(vehicleId);
			transitRoute_r.addDeparture(departure);
		}
	}

	public static class DepTimeGenerator {
		public static String[] generateTimes(int normalInterval, int rushHourMorningInterval, int rushHourEveningInterval) {
			List<String> timesList = new ArrayList<>();
			// Define the time ranges for rush hours (for simplicity)
			int rushHourMorningStart = 6; // 6 AM
			int rushHourMorningEnd = 9;   // 9 AM
			int rushHourEveningStart = 16; // 4 PM
			int rushHourEveningEnd = 19;   // 7 PM
			for (int hour = 5; hour < 24; hour++) {
				int interval = normalInterval;
				if (hour >= rushHourMorningStart && hour < rushHourMorningEnd) {
					interval = rushHourMorningInterval;
				} else if (hour >= rushHourEveningStart && hour < rushHourEveningEnd) {
					interval = rushHourEveningInterval;
				}

				for (int minute = 0; minute < 60; minute += interval) {
					timesList.add(String.format("%02d:%02d:00", hour, minute));
				}
			}

			return timesList.toArray(new String[0]);
		}

		public static void addTransitLineFromCSV(String networkPath, String transitSchedulePath, String csvPath, String transitLineName, String[] times, String[] vehicleRefIds) {
			Config config = ConfigUtils.createConfig();
			Scenario scenario = ScenarioUtils.createScenario(config);

			// Read the existing network and transit schedule
			new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath);
			new TransitScheduleReader(scenario).readFile(transitSchedulePath);

			List<Id<Link>> linkIds = new ArrayList<>();

			// Reading the CSV file to populate the linkIds list
			try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] values = line.split(",");
					linkIds.add(Id.createLinkId(values[0]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			TransitScheduleFactory scheduleFactory = scenario.getTransitSchedule().getFactory();

			// Creating Network Route and Stops
			Id<Link> startLinkId = linkIds.get(0);
			Id<Link> endLinkId = linkIds.get(linkIds.size() - 1);
			List<Id<Link>> intermediateLinkIds = linkIds.subList(1, linkIds.size() - 1);
			NetworkRoute networkRoute = RouteUtils.createLinkNetworkRouteImpl(startLinkId, intermediateLinkIds, endLinkId);

			List<TransitStopFacility> stopFacilities = createStopFacilities(scenario, scheduleFactory, linkIds);
			List<TransitRouteStop> transitRouteStops = createTransitRouteStopsForFacilities(scheduleFactory, stopFacilities);

			// Creating Transit Routes
			TransitRoute transitRoute = scheduleFactory.createTransitRoute(Id.create(transitLineName, TransitRoute.class), networkRoute, transitRouteStops, "pt");
			int halfIndex = vehicleRefIds.length / 2;

			String[] firstHalfVehicleRefIds = Arrays.copyOfRange(vehicleRefIds,0, halfIndex);

			// Populate the departures for the created routes using the first half of vehicleRefIds
			DepartureCreator(scheduleFactory, transitRoute, times, firstHalfVehicleRefIds);

			// Creating Transit Line and adding to the schedule
			TransitLine transitLine = scheduleFactory.createTransitLine(Id.create(transitLineName, TransitLine.class));
			transitLine.addRoute(transitRoute);
			scenario.getTransitSchedule().addTransitLine(transitLine);

			// Write the updated transit schedule back to the same file
			new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(transitSchedulePath);
		}



		private static List<TransitStopFacility> createStopFacilities(Scenario scenario, TransitScheduleFactory scheduleFactory, List<Id<Link>> linkIds) {
			List<TransitStopFacility> stopFacilities = new ArrayList<>();
			for (Id<Link> linkId : linkIds) {
				Link link = scenario.getNetwork().getLinks().get(linkId);
				System.out.println("Extractedlink: " + link);
				TransitStopFacility stopFacility = scheduleFactory.createTransitStopFacility(Id.create(linkId, TransitStopFacility.class), link.getCoord(), false);
				stopFacility.setLinkId(linkId);
				// Check if the stop
				if (!scenario.getTransitSchedule().getFacilities().containsKey(stopFacility.getId())) {
				scenario.getTransitSchedule().addStopFacility(stopFacility);
				stopFacilities.add(stopFacility);
				} else {
				// If it already exists, just add it to the list of stop facilities
				stopFacilities.add(scenario.getTransitSchedule().getFacilities().get(stopFacility.getId()));
				}
			}
    			return stopFacilities;
		}

	private static List<TransitRouteStop> createTransitRouteStopsForFacilities(TransitScheduleFactory scheduleFactory, List<TransitStopFacility> stopFacilities) {
		List<TransitRouteStop> transitRouteStops = new ArrayList<>();
		for (TransitStopFacility stopFacility : stopFacilities) {
			TransitRouteStop routeStop = scheduleFactory.createTransitRouteStop(stopFacility, 0, 0); // Assuming 0 dwell time for simplicity
			transitRouteStops.add(routeStop);
		}
		return transitRouteStops;
	}





		public static void main(String[] args) throws IOException {
			// Initialise scenario
			String networkPath = "examples/scenarios/Odakyu1/rrte2.xml";
			String csvPath = "examples/scenarios/Odakyu1/test/Odakyutest.csv";
			String transitSchedulePath = "examples/scenarios/Odakyu1/transitschedule.xml";

			// Provided departure times
			String[] times = {
				// 5 AM to 6 AM: Early morning, every 15 minutes
				"05:00:00", "05:16:00", "05:27:00", "05:41:00", "05:45:00", "05:55:00",

				// 6 AM to 7 AM: Morning rush, every few minutes
				"06:00:00", "06:01:00", "06:02:00", "06:04:00", "06:07:00", "06:09:00", "06:11:00", "06:13:00", "06:15:00",
				"06:20:00", "06:21:00", "06:23:00", "06:25:00", "06:30:00", "06:31:00", "06:32:00", "06:37:00", "06:38:00",
				"06:40:00", "06:43:00", "06:47:00", "06:48:00", "06:50:00", "06:53:00", "06:55:00", "06:59:00",

				// 7 AM to 7:30 AM: Continue morning rush
				"07:00:00", "07:01:00", "07:02:00", "07:04:00", "07:05:00", "07:10:00", "07:12:00", "07:13:00", "07:15:00",
				"07:19:00", "07:20:00", "07:21:00", "07:23:00", "07:25:00", "07:28:00", "07:30:00",

				// 7:30 AM to 9 AM: Peak rush, every few minutes
				"07:31:00", "07:32:00", "07:34:00", "07:37:00", "07:38:00", "07:39:00", "07:42:00", "07:46:00", "07:49:00",
				"07:50:00", "07:53:00", "07:55:00", "08:00:00", "08:01:00", "08:03:00", "08:04:00", "08:08:00", "08:12:00",
				"08:16:00", "08:20:00", "08:21:00", "08:25:00", "08:28:00", "08:30:00", "08:32:00", "08:35:00", "08:40:00",
				"08:41:00", "08:45:00", "08:50:00", "08:52:00", "08:55:00",

				// 9 AM: Start off-peak
				"09:00:00"
			};



			String[] vehicleRefIds = new String[400];
			for (int i = 0; i < 400; i++) {
				vehicleRefIds[i] = "tr_" + i;  // rotates between tr_1, tr_2, .... tr_25
			}
			RailScheduleCreator creator = new RailScheduleCreator();
			//creator.loadFromCsvAndGenerateLinks(networkPath,csvPath);

			// Calling the addTransitLineFromCSV method
			addTransitLineFromCSV(networkPath, transitSchedulePath, csvPath, "test Line", times, vehicleRefIds);
		}
	}
}
