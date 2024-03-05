package SAVCreator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.Iterator;

public class SAVVehiclefileCreator {
	public static void addVehicles(List<DvrpVehicleSpecification> vehicles, int count, Id<Link> linkId, int size, int seats, double operationStartTime, double operationEndTime, Network network) {
		for (int i = 0; i < size; i++) {
			vehicles.add(ImmutableDvrpVehicleSpecification.newBuilder()
				.id(Id.create("drtE" + count, DvrpVehicle.class))
				.startLinkId(linkId)
				.capacity(seats)
				.serviceBeginTime(operationStartTime)
				.serviceEndTime(operationEndTime)
				.build());
			count++;
		}
	}


	public static void removeAndWriteReducedFile(List<DvrpVehicleSpecification> vehicles, String outputReducedFile, double proportionToRemove) {
		int numToRemove = (int) (vehicles.size() * proportionToRemove);
		if (numToRemove <= 0) {
			return; // No vehicles to remove
		}

		Random random = new Random();
		List<DvrpVehicleSpecification> vehiclesToRemove = new ArrayList<>();

		for (int i = 0; i < numToRemove; i++) {
			int indexToRemove = random.nextInt(vehicles.size());
			vehiclesToRemove.add(vehicles.get(indexToRemove));
			vehicles.remove(indexToRemove);
		}

		new FleetWriter(vehicles.stream()).write(outputReducedFile);
	}


	public static void main(String[] args) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		double operationStartTime = 0.;
		double operationEndTime = 32 * 3600.;
		int seats = 4;
		int count = 0;
		String xmlFilePath = "examples/scenarios/Odakyu5/network.xml";  // adjust path accordingly
		String outputDirectory = "examples/scenarios/Odakyu5";
		String drtsFile = outputDirectory + "drts" + count + "S" + seats + "Odawara.xml";

		String csvFilePath = "examples/scenarios/Odakyu5/line_Odawara.csv"; // Replace with your CSV file path
		Set<String> uniqueLinkIds = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				uniqueLinkIds.add(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		int size = uniqueLinkIds.size();
		List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
		new MatsimNetworkReader(scenario.getNetwork()).readFile(xmlFilePath);
		Network network = scenario.getNetwork();

		for (String linkIdStr : uniqueLinkIds) {
			Id<Link> linkId = Id.createLinkId(linkIdStr);
			addVehicles(vehicles, count, linkId, 1, seats, operationStartTime, operationEndTime, network);
			count++;
		}

		System.out.println(drtsFile);
		new FleetWriter(vehicles.stream()).write(drtsFile);
//

//		double proportionToRemove = 0.50; // Adjust this proportion as needed (e.g., 0.5 for half)
//		String reducedDrtsFile = outputDirectory + "reduced_drts" + proportionToRemove + "S" + seats + ".xml";
//		removeAndWriteReducedFile(vehicles, reducedDrtsFile, proportionToRemove);
	}
}
