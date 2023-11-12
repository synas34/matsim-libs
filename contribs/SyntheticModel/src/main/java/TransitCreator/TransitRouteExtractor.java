package TransitCreator;

import org.json.JSONException;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;

import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;

public class TransitRouteExtractor {

	/**
	 * Adds intermediate links to all routes of a specified transit line in a MATSim TransitSchedule.
	 *
	 * @param transitSchedulePath       The path to the TransitSchedule to be modified.
	 * @param transitLineName The name (ID) of the TransitLine to be modified.
	 */
	public static void addIntermediateLinksToLine(String transitSchedulePath, String transitLineName) {
		// Create a new TransitSchedule
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitSchedule schedule = scenario.getTransitSchedule();
		TransitScheduleFactory scheduleFactory = scenario.getTransitSchedule().getFactory();

		// Read the TransitSchedule from the file
		TransitScheduleReader reader = new TransitScheduleReader(scenario);
		reader.readFile(Paths.get(transitSchedulePath).toString());

		// Retrieve the TransitLine by its name (ID)
		TransitLine line = schedule.getTransitLines().get(Id.create(transitLineName, TransitLine.class));

		if (line == null) {
			throw new IllegalArgumentException("TransitLine with ID " + transitLineName + " not found.");
		}

		// Process each route in the line
		for (TransitRoute route : new ArrayList<>(line.getRoutes().values())) {
			// Create a new TransitRoute with intermediate links
			TransitRoute newRoute = createRouteWithIntermediateLinks(route, scheduleFactory);

			// Replace the old route with the new route in the line
			line.removeRoute(route);
			line.addRoute(newRoute);
		}

		TransitScheduleWriter writer = new TransitScheduleWriter(schedule);
		writer.writeFile(Paths.get(transitSchedulePath).toString());

	}
	public static TransitRoute createRouteWithIntermediateLinks(TransitRoute originalRoute, TransitScheduleFactory scheduleFactory) {
		NetworkRoute originalNetworkRoute = originalRoute.getRoute();
		List<Id<Link>> originalIntermediateLinks = originalNetworkRoute.getLinkIds();
		List<Id<Link>> newIntermediateLinks = new ArrayList<>();

		// Handle the start link
		Id<Link> firstIntermediate = createIntermediateLinkId(
			originalNetworkRoute.getStartLinkId(),
			originalIntermediateLinks.isEmpty() ? originalNetworkRoute.getEndLinkId() : originalIntermediateLinks.get(0)
		);
		newIntermediateLinks.add(firstIntermediate);

		for (int i = 0; i < originalIntermediateLinks.size(); i++) {
			Id<Link> currentLinkId = originalIntermediateLinks.get(i);
			newIntermediateLinks.add(currentLinkId);

			// Add an intermediate link unless it's the last link
			if (i < originalIntermediateLinks.size() - 1) {
				Id<Link> nextLinkId = originalIntermediateLinks.get(i + 1);
				Id<Link> intermediateLinkId = createIntermediateLinkId(currentLinkId, nextLinkId);
				newIntermediateLinks.add(intermediateLinkId);
				System.out.println( "Intermediate Link:" + intermediateLinkId);
			}
		}

		NetworkRoute newNetworkRoute = RouteUtils.createLinkNetworkRouteImpl(
			originalNetworkRoute.getStartLinkId(),
			newIntermediateLinks,
			originalNetworkRoute.getEndLinkId()
		);
		System.out.println( "Intermediate Routes:" + newNetworkRoute);
		System.out.println( "Intermediate Routes:" + originalRoute.getRoute());
		originalRoute.setRoute(newNetworkRoute);
		System.out.println( "Intermediate Routes:" + originalRoute.getRoute());
		TransitRoute transitRoute = scheduleFactory.createTransitRoute(Id.create(originalRoute.getId(), TransitRoute.class), newNetworkRoute, originalRoute.getStops(), "pt");

		return transitRoute;
	}

	public static Id<Link> createIntermediateLinkId(Id<Link> linkId1, Id<Link> linkId2) {
		String firstPart = linkId1.toString().substring(0, linkId1.toString().length() / 2);
		String secondPart = linkId2.toString().substring(linkId2.toString().length() / 2);
		System.out.println(firstPart + secondPart);
		return Id.create(firstPart + secondPart, Link.class);
	}


	/**
	 * Read the railway JSON file and create network routes for each railway.
	 *
	 * @param jsonFilePath Path to the railway.json file.
	 * @return Map of railway IDs to their corresponding NetworkRoute.
	 */
	public static Map<String, NetworkRoute> createNetworkRoutesFromRailway(String jsonFilePath) throws IOException, JSONException {
		Map<String, NetworkRoute> railwayRoutes = new HashMap<>();

		String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
		JSONArray railways = new JSONArray(jsonString);

		for (int i = 0; i < railways.length(); i++) {
			JSONObject railway = railways.getJSONObject(i);
			String railwayId = railway.getString("id").replaceAll("[\\-\\.\\s]", "").toLowerCase();
			JSONArray stations = railway.getJSONArray("stations");

			List<Id<Link>> links = new ArrayList<>();
			List<Id<Link>> links_r = new ArrayList<>();
			String firstStation = stations.getString(0).replaceAll("[\\-\\.\\s]", "").toLowerCase();
			links.add(Id.create(firstStation + "_" + firstStation, Link.class));
			String firstStation_r = stations.getString(stations.length() - 1).replaceAll("[\\-\\.\\s]", "").toLowerCase();
			links_r.add(Id.create(firstStation_r + "_" + firstStation_r, Link.class));
			for (int j = 0; j < stations.length() - 1; j++) {
				String startStation = stations.getString(j).replaceAll("[\\-\\.\\s]", "").toLowerCase();
				String endStation = stations.getString(j + 1).replaceAll("[\\-\\.\\s]", "").toLowerCase();
				links.add(Id.create(startStation + "_" + startStation, Link.class));
				links.add(Id.create(startStation + "_" + endStation, Link.class));
			}
			for (int j = stations.length() - 1; j > 0; j--) {
				String startStation = stations.getString(j).replaceAll("[\\-\\.\\s]", "").toLowerCase();
				String endStation = stations.getString(j - 1).replaceAll("[\\-\\.\\s]", "").toLowerCase();
				links_r.add(Id.create(startStation + "_" + startStation, Link.class));
				links_r.add(Id.create(startStation + "_" + endStation, Link.class));
			}
			// Add a self-link for the last station
			String lastStation = stations.getString(stations.length() - 1).replaceAll("[\\-\\.\\s]", "").toLowerCase();
			links.add(Id.create(lastStation + "_" + lastStation, Link.class));
			String lastStation_r = stations.getString(0).replaceAll("[\\-\\.\\s]", "").toLowerCase();
			links_r.add(Id.create(lastStation_r + "_" + lastStation_r, Link.class));
			if (links.size() > 2) {
				NetworkRoute route = RouteUtils.createLinkNetworkRouteImpl(
					links.get(0), links.subList(1, links.size() - 1), links.get(links.size() - 1));
				railwayRoutes.put(railwayId, route);
			} else if (links.size() == 2) {
				NetworkRoute route = RouteUtils.createLinkNetworkRouteImpl(
					links.get(0), Collections.emptyList(), links.get(1));
				railwayRoutes.put(railwayId, route);
			} else {}

			if (links_r.size() > 2) {
				NetworkRoute route_r = RouteUtils.createLinkNetworkRouteImpl(
					links_r.get(0), links_r.subList(1, links_r.size() - 1), links_r.get(links_r.size() - 1));
				railwayRoutes.put(railwayId + "_r", route_r); // Use a different key for the reversed route
			} else if (links_r.size() == 2) {
				NetworkRoute route_r = RouteUtils.createLinkNetworkRouteImpl(
					links_r.get(0), Collections.emptyList(), links_r.get(1));
				railwayRoutes.put(railwayId + "_r", route_r);
			} else {}
			}

		return railwayRoutes;
	}

	public static void replaceIntermediateLinks(String transitSchedulePath, Map<String, NetworkRoute> railwayRoutes) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitSchedule schedule = scenario.getTransitSchedule();
		TransitScheduleReader reader = new TransitScheduleReader(scenario);
		reader.readFile(Paths.get(transitSchedulePath).toString());

		for (TransitLine line : schedule.getTransitLines().values()) {
			for (TransitRoute route : new ArrayList<>(line.getRoutes().values())) {
				replaceRouteIntermediateLinks(line,route, railwayRoutes, scenario.getNetwork());
//				line.addRoute(route); // Update the route in the line
			}
		}

		TransitScheduleWriter writer = new TransitScheduleWriter(schedule);
		writer.writeFile(Paths.get(transitSchedulePath).toString());
	}
	// Method to replace intermediate links in TransitRoutes with those from the corresponding NetworkRoute
	private static void replaceRouteIntermediateLinks(TransitLine line, TransitRoute transitRoute, Map<String, NetworkRoute> railwayRoutes, Network network) {
		String lineId = line.getId().toString().replaceAll("[\\-\\.\\s]", "").toLowerCase();
		String routeId = transitRoute.getId().toString();
		NetworkRoute railwayRoute = railwayRoutes.get(lineId);
		NetworkRoute reverseRailwayRoute = railwayRoutes.get(lineId + "_r");

//		if (railwayRoute == null) {
//			throw new IllegalArgumentException("No corresponding railway route found for TransitRoute ID: " + lineId);
//		}

		// Extract intermediate links from the railway route
		List<Id<Link>> newIntermediateLinks = extractIntermediateLinks(transitRoute, railwayRoute, reverseRailwayRoute);

		// Replace the intermediate links in the transit route
		NetworkRoute newNetworkRoute = RouteUtils.createLinkNetworkRouteImpl(
			transitRoute.getRoute().getStartLinkId(),
			newIntermediateLinks,
			transitRoute.getRoute().getEndLinkId()
		);
		transitRoute.setRoute(newNetworkRoute);
	}

	private static List<Id<Link>> extractIntermediateLinks(TransitRoute transitRoute, NetworkRoute railwayRoute, NetworkRoute reverseRailwayRoute) {
		List<Id<Link>> newIntermediateLinks = new ArrayList<>();

		Id<Link> startLinkId = transitRoute.getRoute().getStartLinkId();
		Id<Link> endLinkId = transitRoute.getRoute().getEndLinkId();

		System.out.println("Starting to find intermediate links. StartLinkId: " + startLinkId + ", EndLinkId: " + endLinkId);

		// Try to find the route in the original order
		newIntermediateLinks = findIntermediateLinks(startLinkId, endLinkId, railwayRoute.getLinkIds());

		// If no links were found, try the reverse railway route
		if (newIntermediateLinks.isEmpty()) {
			System.out.println("No intermediate links found in original order. Trying reverse railway route.");
			System.out.println("Reverse: " + reverseRailwayRoute.getLinkIds());
			System.out.println("Normal: " + railwayRoute.getLinkIds());
			newIntermediateLinks = findIntermediateLinks(startLinkId, endLinkId, reverseRailwayRoute.getLinkIds());
		}

		System.out.println("Intermediate links found: " + newIntermediateLinks);
		return newIntermediateLinks;
	}


	private static List<Id<Link>> findIntermediateLinks(Id<Link> startLinkId, Id<Link> endLinkId, List<Id<Link>> linkIds) {
		List<Id<Link>> intermediateLinks = new ArrayList<>();
		boolean startAdding = false;

		for (Id<Link> linkId : linkIds) {
			if (linkId.equals(startLinkId)) {
				startAdding = true;
			}
			if (startAdding) {
				intermediateLinks.add(linkId);
			}
			if (linkId.equals(endLinkId)) {
				break;
			}
		}
		System.out.println("Start adding links from: " + intermediateLinks);
		return intermediateLinks;
	}

	public static void main(String[] args) throws JSONException, IOException {
		String transitSchedulePath = "examples/scenarios/Odakyu1/transitschedule15.xml";
//		String excludedTransitLineId = "jreast-chuosobulocal";
//
//		// Create a new TransitSchedule
//		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//		TransitSchedule schedule = scenario.getTransitSchedule();
//
//		// Read the TransitSchedule from the file
//		TransitScheduleReader reader = new TransitScheduleReader(scenario);
//		reader.readFile(Paths.get(transitSchedulePath).toString());
//
//		// Iterate over each TransitLine in the schedule
//		for (TransitLine transitLine : new ArrayList<>(schedule.getTransitLines().values())) {
//			String currentLineId = transitLine.getId().toString();
//
//			// Check if the current line is not the excluded one
//			if (!currentLineId.equals(excludedTransitLineId)) {
//				// Add intermediate links to the current line
//				addIntermediateLinksToLine(transitSchedulePath, currentLineId);
//			}
//		}

		String railwayJsonPath = "contribs/SyntheticModel/src/main/java/TransitCreator/railways.json";

		// Create network routes from the railway data
		Map<String, NetworkRoute> railwayRoutes = createNetworkRoutesFromRailway(railwayJsonPath);

		// Replace intermediate links in transit routes
		replaceIntermediateLinks(transitSchedulePath, railwayRoutes);
		// ...
		for (Map.Entry<String, NetworkRoute> entry : railwayRoutes.entrySet()) {
			System.out.println("Railway Route ID: " + entry.getKey());
			NetworkRoute route = entry.getValue();
			System.out.println("Start Link ID: " + route.getStartLinkId());
			System.out.println("End Link ID: " + route.getEndLinkId());
			System.out.println("Intermediate Link IDs:");
			for (Id<Link> linkId : route.getLinkIds()) {
				System.out.println("Link ID: " + linkId);}
			System.out.println("======================================");}

	}

}
