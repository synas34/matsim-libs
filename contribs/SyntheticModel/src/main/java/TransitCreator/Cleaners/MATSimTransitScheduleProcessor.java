package TransitCreator.Cleaners;

import TransitCreator.TransitScheduleMerger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.internal.MatsimReader;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.Vehicle;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;

import java.util.ArrayList;
import java.util.List;

public class MATSimTransitScheduleProcessor {
	public void removeSaturdayHolidayDepartures(String existingTransitSchedulePath, String outputPath) throws Exception {
		// Load existing transit schedule
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitScheduleReader reader = new TransitScheduleReader(scenario);
		reader.readFile(existingTransitSchedulePath);
		TransitSchedule transitSchedule = scenario.getTransitSchedule();

		List<Departure> departuresToRemove = new ArrayList<>();

		for (TransitLine line : transitSchedule.getTransitLines().values()) {
			for (TransitRoute route : line.getRoutes().values()) {
				for (Departure departure : route.getDepartures().values()) {
					if (departure.getVehicleId().toString().endsWith("SaturdayHoliday")) {
						departuresToRemove.add(departure);
					}
				}

				for (Departure departure : departuresToRemove) {
					route.removeDeparture(departure);
				}
				departuresToRemove.clear();
			}
		}

		// Write the updated transit schedule back to the output path
		new TransitScheduleWriter(transitSchedule).writeFile(outputPath);
	}




	public static void main(String[] args) throws Exception {
		String inputFilePath = "examples/scenarios/Odakyu2/transitscheduletest.xml"; // Replace with your input file path
		String outputFilePath = "examples/scenarios/Odakyu2/updated_transitschedule.xml"; // Replace with your output file path


		new MATSimTransitScheduleProcessor().removeSaturdayHolidayDepartures(inputFilePath, outputFilePath);


	}
}

