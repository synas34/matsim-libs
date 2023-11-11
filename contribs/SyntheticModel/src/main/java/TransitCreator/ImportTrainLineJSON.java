package TransitCreator;


import org.json.JSONException;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;
import org.opengis.referencing.FactoryException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static TransitCreator.RailScheduleCreator.*;

public class ImportTrainLineJSON {

	public static void ImportTrainLineJSON(String networkPath, String transitSchedulePath, String jsonFilePath) {
		// Remove the file extension and path to get the name for the TransitLine
		String transitLineName = new File(jsonFilePath).getName();
		transitLineName = transitLineName.substring(0, transitLineName.lastIndexOf('.'));

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);

		// Read the existing network and transit schedule
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath);
		new TransitScheduleReader(scenario).readFile(transitSchedulePath);
		TransitScheduleFactory scheduleFactory = scenario.getTransitSchedule().getFactory();
		TransitLine transitLine = scheduleFactory.createTransitLine(Id.create(transitLineName, TransitLine.class));

		// Check if the transit line is already in the transit schedule
		if (!scenario.getTransitSchedule().getTransitLines().containsKey(transitLine.getId())) {

		// Extract linkIds, departure times, and vehicleRefIds from the JSON file
		Map<List<Id<Link>>, List<ScheduleInfo>> scheduleMap = extractLinkIdsFromJson(jsonFilePath);
		// Printing the schedule map for testing
		for (Map.Entry<List<Id<Link>>, List<ScheduleInfo>> entry : scheduleMap.entrySet()) {
			System.out.println("Unique LinkIDs Combination:");
			for (Id<Link> linkId : entry.getKey()) {
				System.out.print(linkId + ", ");
			}
			System.out.println("\nAssociated Schedules:");
			for (ScheduleInfo info : entry.getValue()) {
				System.out.println("Departure Time: " + info.departureTime + ", Vehicle Ref ID: " + info.vehicleRefId);
			}
			System.out.println("--------------------------------------");
		}
			// Loop through each unique linkIDs combination to create a TransitRoute and DepartureCreator
			for (Map.Entry<List<Id<Link>>, List<ScheduleInfo>> entry : scheduleMap.entrySet()) {
				List<Id<Link>> linkIds = entry.getKey();
				List<ScheduleInfo> schedules = entry.getValue();

				// Sort the schedule information
				schedules.sort(Comparator.comparing(s -> s.departureTime));

				// Extract departure times and vehicleRefIds
				String[] departureTimes = schedules.stream().map(s -> s.departureTime).toArray(String[]::new);
				String[] vehicleRefIds = schedules.stream().map(s -> s.vehicleRefId).toArray(String[]::new);
				System.out.println("Departure Time: " + departureTimes);

				// Creating Network Route and Stops
				Id<Link> startLinkId = linkIds.get(0);
				Id<Link> endLinkId = linkIds.get(linkIds.size() - 1);
				List<Id<Link>> intermediateLinkIds = linkIds.subList(1, linkIds.size() - 1);
				NetworkRoute networkRoute = RouteUtils.createLinkNetworkRouteImpl(startLinkId, intermediateLinkIds, endLinkId);
				System.out.println("Network Route: " + networkRoute);

				List<TransitStopFacility> stopFacilities = createStopFacilities(scenario, scheduleFactory, linkIds);
				List<TransitRouteStop> transitRouteStops = createTransitRouteStopsForFacilities(scheduleFactory, stopFacilities);

				// Creating Transit Routes
				TransitRoute transitRoute = scheduleFactory.createTransitRoute(Id.create(transitLineName + "_" + entry.getKey().toString(), TransitRoute.class), networkRoute, transitRouteStops, "pt");

				// Populate the departures for the created routes
				DepartureCreator(scheduleFactory, transitRoute, departureTimes, vehicleRefIds);

				// Add the route to the TransitLine
				transitLine.addRoute(transitRoute);
			}

			// Add the TransitLine to the scenario
			scenario.getTransitSchedule().addTransitLine(transitLine);

			// Write the updated transit schedule back to the file
			new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(transitSchedulePath);
		} else {
			// Log statement if the transit line is already present
			System.out.println("Transit line " + transitLine.getId() + " is already present in the transit schedule.");
		}
	}


	private static class ScheduleInfo {
		String departureTime;
		String vehicleRefId;

		ScheduleInfo(String departureTime, String vehicleRefId) {
			this.departureTime = departureTime;
			this.vehicleRefId = vehicleRefId;
		}
	}

	public static Map<List<Id<Link>>, List<ScheduleInfo>> extractLinkIdsFromJson(String jsonFilePath) {
		Map<List<Id<Link>>, List<ScheduleInfo>> scheduleMap = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(jsonFilePath)) {
			JSONTokener tokener = new JSONTokener(fis);
			JSONArray trainSchedules = new JSONArray(tokener);

			// Iterate over all train schedules
			for (int i = 0; i < trainSchedules.length(); i++) {
				JSONObject trainSchedule = trainSchedules.getJSONObject(i);
				JSONArray timetable = trainSchedule.getJSONArray("tt");

				List<Id<Link>> linkIds = new ArrayList<>();
				for (int j = 0; j < timetable.length(); j++) {
					JSONObject stationSchedule = timetable.getJSONObject(j);
					String stationId = stationSchedule.getString("s");

					// Insert a hyphen before the second capital letter after the last period
					int lastPeriodIndex = stationId.lastIndexOf('.');
					if (lastPeriodIndex != -1 && lastPeriodIndex < stationId.length() - 1) {
						int secondCapitalIndex = findSecondCapitalIndex(stationId, lastPeriodIndex + 1);
						if (secondCapitalIndex != -1) {
							stationId = stationId.substring(0, secondCapitalIndex) + "-" + stationId.substring(secondCapitalIndex);
						}
					}

					String concatenatedId = stationId + "_" + stationId;
					linkIds.add(Id.create(concatenatedId, Link.class));
				}

				List<Id<Link>> uniqueLinkIds = new ArrayList<>(new LinkedHashSet<>(linkIds));

				String departureTime = timetable.getJSONObject(0).getString("d");
				String vehicleRefId = trainSchedule.getString("id");
				ScheduleInfo scheduleInfo = new ScheduleInfo(departureTime, vehicleRefId);

				scheduleMap.computeIfAbsent(uniqueLinkIds, k -> new ArrayList<>()).add(scheduleInfo);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		return scheduleMap;
	}

	private static int findSecondCapitalIndex(String str, int start) {
		boolean foundFirstCapital = false;
		for (int i = start; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				if (foundFirstCapital) {
					return i;
				}
				foundFirstCapital = true;
			}
		}
		return -1;
	}




	public static void main(String[] args) throws IOException {

		String networkPath = "examples/scenarios/Odakyu1/network_1699671185214.xml";
		String transitSchedulePath = "examples/scenarios/Odakyu1/transitschedule11.xml";
		String jsonFolder = "contribs/SyntheticModel/src/main/java/TransitCreator/train-timetables"; // Folder containing JSON files

		// Get the list of files in the specified folder
		Files.walk(Paths.get(jsonFolder))
			.filter(Files::isRegularFile)
			.forEach(filePath -> {
				if (filePath.toString().endsWith(".json")) {
					String jsonFilePath = filePath.toString();
					Map<List<Id<Link>>, List<ScheduleInfo>> scheduleMap = extractLinkIdsFromJson(jsonFilePath);
					ImportTrainLineJSON(networkPath, transitSchedulePath, jsonFilePath);
				}
			});
	}


}


