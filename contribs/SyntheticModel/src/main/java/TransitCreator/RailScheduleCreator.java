package TransitCreator;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	 * @param networkFilePath  Path to the existing Network file with station names and coordinates.
	 * @param csvFilePath     Path to the CSV file with station names and coordinates.
	 * @throws IOException If there's a problem reading the files.
	 */

	public static List<Id<Link>> loadFromCsvAndGenerateLinks(String networkFilePath, String csvFilePath, boolean skipWriting) throws IOException, FactoryException {
		Network network = NetworkUtils.readNetwork(networkFilePath);
		List<Id<Link>> linkIds = new ArrayList<>();

		// Set up CRS and MathTransform for coordinate transformation
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326"); // WGS84
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32654"); // UTM 54N
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String line;
			Node previousNode = null;

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

				String stationName = parts[0].replace("\"", "").trim();
				String[] coordinates = parts[1].replace("\"", "").split(",");

				double latitude = Double.parseDouble(coordinates[0].trim());
				double longitude = Double.parseDouble(coordinates[1].trim());
				System.out.println("Created node: " + latitude +"and"+ longitude);

				// Transform coordinates
				DirectPosition2D sourcePosition = new DirectPosition2D(latitude, longitude);
				DirectPosition2D transformedPosition = new DirectPosition2D();
				transform.transform(sourcePosition, transformedPosition);
				double transformedX = transformedPosition.getX();
				double transformedY = transformedPosition.getY();
				System.out.println("transformed node: " + transformedX +"and"+ transformedY);

				// Use existing node if it exists, otherwise create a new one
				Node currentNode = network.getNodes().get(Id.createNodeId(stationName));
				if (currentNode == null) {
					Coord coord = new Coord(transformedX, transformedY);
					currentNode = network.getFactory().createNode(Id.createNodeId(stationName), coord);
					network.addNode(currentNode);
				}



				if (previousNode != null) {
					Id<Link> linkId = Id.createLinkId(previousNode.getId().toString() + "_" + currentNode.getId().toString());
					Link currentLink = network.getLinks().get(linkId);
					linkIds.add(linkId);
						if (currentLink == null) {
								createSingleLink(network, previousNode, currentNode);
						}
					Id<Link> selflinkId = Id.createLinkId(currentNode.getId().toString() + "_" + currentNode.getId().toString());
					Link selfLink = network.getLinks().get(selflinkId);
					linkIds.add(selflinkId);
						if (selfLink == null) {
							createLinkWithLength(network, currentNode, currentNode, 1.0);
						}

				} else {
					Id<Link> selflinkId = Id.createLinkId(currentNode.getId().toString() + "_" + currentNode.getId().toString());
					Link selfLink = network.getLinks().get(selflinkId);
					linkIds.add(selflinkId);
					if (selfLink == null) {
						createLinkWithLength(network, currentNode, currentNode, 1.0);
					}

				}

				previousNode = currentNode;
			}
		} catch (TransformException e) {
			throw new RuntimeException(e);
		}
		// Write the modified network back to the XML file only if skipWriting is false
		if (!skipWriting) {
			new NetworkWriter(network).write(networkFilePath);
		}
		System.out.println("LinkIDs that are stops: " + linkIds );
		return linkIds;
	}


	// The function to create a link with specified length
	private static void createLinkWithLength(Network network, Node fromNode, Node toNode, double length) {
		Link link = network.getFactory().createLink(Id.createLinkId(fromNode.getId() + "_" + toNode.getId()), fromNode, toNode);
		link.setLength(length);

		// Setting specified attributes for the link
		link.setCapacity(9999);
		link.setFreespeed(20.0 / 3.6); // Convert km/h to m/s
		Set<String> modes = new HashSet<>();
		modes.add("pt");
		link.setAllowedModes(modes);
		network.addLink(link);
		System.out.println("Created Link: " + link.getId());
	}
	private static void createSingleLink(Network network, Node fromNode, Node toNode) {
		Link link = network.getFactory().createLink(Id.createLinkId(fromNode.getId() + "_" + toNode.getId()), fromNode, toNode);

		// Setting specified attributes for the link
		link.setCapacity(9999);
		link.setFreespeed(20.0 / 3.6); // Convert km/h to m/s
		Set<String> modes = new HashSet<>();
		modes.add("pt");
		link.setAllowedModes(modes);
		network.addLink(link);
		System.out.println("Created Link: " + link.getId());
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
			Id<Vehicle> vehicleId = Id.create(vehicleRefIds[i], Vehicle.class);
			Departure departure = scheduleFactory.createDeparture(departureId, departureTime);
			departure.setVehicleId(vehicleId);
			transitRoute_r.addDeparture(departure);
			System.out.println("Created Departure : " + departure);
		}
	}


	public static void addTransitLineFromCSV(String networkPath, String transitSchedulePath, String folderPath) {
		String csvPath = folderPath + "/JR Yokohama - Stations.csv";
		String departuresCsvPath = folderPath + "/JR Yokohama - Departure Times.csv";
		String transitLineName = new File(folderPath).getName();  // Extracting the folder name

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);

		// Read the existing network and transit schedule
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath);
		new TransitScheduleReader(scenario).readFile(transitSchedulePath);

		// Load linkIds from the CSV file
		List<Id<Link>> linkIds = new ArrayList<>();
		try {
			linkIds = loadFromCsvAndGenerateLinks(networkPath, csvPath, false);  // Set to true to skip writing
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
            throw new RuntimeException(e);
        }

        // Read departure times from the CSV file
		List<String> departureTimesList = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(departuresCsvPath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				departureTimesList.add(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert departureTimesList to an array
		String[] departureTimes = departureTimesList.toArray(new String[0]);

		// Generate vehicleRefIds based on the departure times and the transit line name
		String[] vehicleRefIds = Arrays.stream(departureTimes).map(time -> transitLineName + "_" + time.replace(":", "")).toArray(String[]::new);

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


			// Populate the departures for the created routes using the first half of vehicleRefIds
			DepartureCreator(scheduleFactory, transitRoute, departureTimes, vehicleRefIds);

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
			String networkPath = "examples/scenarios/Odakyu1/test/network.xml";
			String transitSchedulePath = "examples/scenarios/Odakyu1/test/transitschedule.xml";
			String foldersPath = "examples/scenarios/Odakyu1/TransitNetworkData/Jr Yokohama Local - r";


			// Calling the addTransitLineFromCSV method
			addTransitLineFromCSV(networkPath, transitSchedulePath, foldersPath);
		}
	}

