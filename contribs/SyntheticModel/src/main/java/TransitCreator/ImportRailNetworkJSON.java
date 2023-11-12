package TransitCreator;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static TransitCreator.RailScheduleCreator.createLinkWithLength;
import static TransitCreator.RailScheduleCreator.createSingleLink;

public class ImportRailNetworkJSON {

	// Main method to execute the generation and print out every link created
	public static void main(String[] args) throws IOException, JSONException, FactoryException, TransformException {
		String stationsJson = new String(Files.readAllBytes(Paths.get("contribs/SyntheticModel/src/main/java/TransitCreator/stations.json")));
		String railwaysJson = new String(Files.readAllBytes(Paths.get("contribs/SyntheticModel/src/main/java/TransitCreator/railways.json")));
		String networkFilePath = "examples/scenarios/Odakyu1/network1.xml";
		List<Link> allRailLinksCreated = GenerateLinksJSON(stationsJson, railwaysJson, networkFilePath,true);

		// Print out every link created
		for (Link link : allRailLinksCreated) {
			System.out.println("Link ID: " + link.getId() +
				", From Node: " + link.getFromNode().getId() +
				", To Node: " + link.getToNode().getId() +
				", Length: " + link.getLength());
		}
//		Map<String, Map<String, Object>> railNetwork = importRailNetworkJSON(stationsJson, railwaysJson);
//
//		// Printing the rail network for testing
//		for (Map.Entry<String, Map<String, Object>> entry : railNetwork.entrySet()) {
//			System.out.println("Railway ID: " + entry.getKey());
//			Map<String, Object> railwayData = entry.getValue();
//			System.out.println("Title: " + railwayData.get("title"));
//			System.out.println("Color: " + railwayData.get("color"));
//			System.out.println("Car Composition: " + railwayData.get("carComposition"));
//			System.out.println("Stations:");
//
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> stationsList = (List<Map<String, Object>>) railwayData.get("stations");
//			for (Map<String, Object> station : stationsList) {
//				System.out.println("Station Title: " + station.get("title"));
//				System.out.println("Latitude: " + station.get("x"));
//				System.out.println("Longitude: " + station.get("y"));
//				System.out.println("----------------");
//			}
//			System.out.println("======================================");
//		}
	}


	public static Map<String, Map<String, Object>> importRailNetworkJSON(String stationsJson, String railwaysJson) throws JSONException {
		JSONArray stationsArray = new JSONArray(stationsJson);
		JSONArray railwaysArray = new JSONArray(railwaysJson);

		// Step 1: Process the stations and store them in a map for easy access
		Map<String, JSONObject> stationsMap = new HashMap<>();
		for (int i = 0; i < stationsArray.length(); i++) {
			JSONObject station = stationsArray.getJSONObject(i);
			// Store the whole station object with the fully qualified ID as the key
			stationsMap.put(station.getString("id"), station);
		}

		// Step 2: Create the HashMap that will be returned
		Map<String, Map<String, Object>> railwayNetworkMap = new LinkedHashMap<>();

		// Step 3: Process the railways and build the HashMap
		for (int i = 0; i < railwaysArray.length(); i++) {
			JSONObject railway = railwaysArray.getJSONObject(i);
			String railwayId = railway.getString("id");
			JSONArray stationIds = railway.getJSONArray("stations");

			Map<String, Object> railwayData = new LinkedHashMap<>();
			railwayData.put("title", railway.getJSONObject("title").getString("en"));
			railwayData.put("color", railway.getString("color"));
			railwayData.put("carComposition", railway.getInt("carComposition"));

			// Step 4: Add stations data
			List<Map<String, Object>> stationsList = new java.util.ArrayList<>();
			for (int j = 0; j < stationIds.length(); j++) {
				String stationId = stationIds.getString(j);
				JSONObject station = stationsMap.get(stationId);

				if (station != null) {
					JSONArray coord = station.getJSONArray("coord");
					Map<String, Object> stationData = new HashMap<>();
					stationData.put("x", coord.getDouble(1)); // Latitude
					stationData.put("y", coord.getDouble(0)); // Longitude
					// Use the fully qualified ID instead of just the simple name
					stationData.put("title", station.getString("id")); // Fully qualified station ID
					stationsList.add(stationData);
				}
			}

			railwayData.put("stations", stationsList);
			railwayNetworkMap.put(railwayId, railwayData);
		}

		return railwayNetworkMap;
	}

	// Method to generate links for the MATSim network and return a list of all rail links created
	public static List<Link> GenerateLinksJSON(String stationsJson, String railwaysJson, String networkFilePath, boolean skipWriting) throws IOException, FactoryException, JSONException, TransformException {
		// Import the station and railway data
		Map<String, Map<String, Object>> railNetwork = importRailNetworkJSON(stationsJson, railwaysJson);

		// Initalise and read the network
		Network network;
		if (skipWriting) {
			network = NetworkUtils.createNetwork();
		} else {
			network = NetworkUtils.readNetwork(networkFilePath);
		}

		// Set up CRS and MathTransform for coordinate transformation
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326"); // WGS84
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32654"); // UTM 54N
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

		// List to hold all created links
		List<Link> allRailLinksCreated = new ArrayList<>();

		// Process each railway
		for (String railwayId : railNetwork.keySet()) {
			Map<String, Object> railwayData = railNetwork.get(railwayId);
			List<Map<String, Object>> stations = (List<Map<String, Object>>) railwayData.get("stations");

			Node previousNode = null;

			// Process each station in the current railway
			for (Map<String, Object> stationData : stations) {
				// Original station ID
				String originalStationId = "" + stationData.get("title");

				// Processed station ID: remove dashes, dots, spaces and convert to lowercase
				String stationId = originalStationId.replaceAll("[\\-\\.\\s]", "").toLowerCase();
				double x = (Double) stationData.get("x");
				double y = (Double) stationData.get("y");

				// Transform coordinates
				DirectPosition2D sourcePosition = new DirectPosition2D(x, y);
				DirectPosition2D transformedPosition = new DirectPosition2D();
				transform.transform(sourcePosition, transformedPosition);
				double transformedX = transformedPosition.getX();
				double transformedY = transformedPosition.getY();

				// Create or get existing node
				Node currentNode = network.getNodes().get(Id.createNodeId(stationId));
				if (currentNode == null) {
					currentNode = NetworkUtils.createAndAddNode(network, Id.createNodeId(stationId), new Coord(transformedX, transformedY));
				}
				// Create links
				if (previousNode != null) {
					Id<Link> linkId = Id.createLinkId(previousNode.getId().toString() + "_" + currentNode.getId().toString());
					Link currentLink = network.getLinks().get(linkId);
					if (currentLink == null) {
						createSingleLink(network, previousNode, currentNode);
						currentLink = network.getLinks().get(linkId);
						allRailLinksCreated.add(currentLink);
						System.out.println(currentLink + "currents");
					}
					Id<Link> selflinkId = Id.createLinkId(currentNode.getId().toString() + "_" + currentNode.getId().toString());
					Link selfLink = network.getLinks().get(selflinkId);
					if (selfLink == null) {
						createLinkWithLength(network, currentNode, currentNode, 1.0);
						selfLink = network.getLinks().get(selflinkId);
						allRailLinksCreated.add(selfLink);
						System.out.println(selfLink + "selfs");
					}

				} else {
					Id<Link> selflinkId = Id.createLinkId(currentNode.getId().toString() + "_" + currentNode.getId().toString());
					Link selfLink = network.getLinks().get(selflinkId);
					if (selfLink == null) {
						createLinkWithLength(network, currentNode, currentNode, 1.0);
						selfLink = network.getLinks().get(selflinkId);
						allRailLinksCreated.add(selfLink);
						System.out.println(selfLink+ "selfs");
					}
				}
				previousNode = currentNode;

			}
		}


		// Check if we need to write the network to a file
		if (!skipWriting) {
			// Use the provided file path for the network file
			NetworkWriter networkWriter = new NetworkWriter(network);
			networkWriter.write(networkFilePath);
			System.out.println("Network written to file: " + networkFilePath);
		}  else {
			// Extract the directory from the provided network file path
			File networkFile = new File(networkFilePath);
			String directoryPath = networkFile.getParent();
			// Generate a new file name in the same directory
			String newFileName = directoryPath + File.separator + "network_" + System.currentTimeMillis() + ".xml";
			NetworkWriter networkWriter = new NetworkWriter(network);
			networkWriter.write(newFileName);
			System.out.println("Network written to new file: " + newFileName);
		}

		return allRailLinksCreated;
	}


}


