package TransitCreator;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class TransitScheduleMerger {

	public void transformAndSaveTransitSchedule(String existingTransitSchedulePath, String outputPath) throws Exception {
		// Load existing transit schedule
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitScheduleReader reader = new TransitScheduleReader(scenario);
		reader.readFile(existingTransitSchedulePath);
		TransitSchedule transitSchedule = scenario.getTransitSchedule();

		// Define source and target coordinate systems
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326"); // WGS84
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32654"); // UTM 54N

		// Create a transformation
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

		// Iterate over all TransitStopFacility and update their coordinates
		for (TransitStopFacility stop : transitSchedule.getFacilities().values()) {
			double x = stop.getCoord().getX();
			double y = stop.getCoord().getY();

			// Create a DirectPosition for transformation
			DirectPosition2D sourcePosition = new DirectPosition2D(y, x);
			DirectPosition2D transformedPosition = new DirectPosition2D();

			try {
				// Perform the transformation
				transform.transform(sourcePosition, transformedPosition);

				// Modify x and y coordinates
				double newX = transformedPosition.getX();
				double newY = transformedPosition.getY();

				// Convert the modified values back to MATSim's Coord
				Coord newCoord = new Coord(newX, newY);
				stop.setCoord(newCoord);
			} catch (Exception e) {
				e.printStackTrace();
				// Handle the transformation exception appropriately
			}
		}

		// Write the updated transit schedule back to the output path
		new TransitScheduleWriter(transitSchedule).writeFile(outputPath);
	}

	public void adjustAndSaveTransitSchedule(String existingTransitSchedulePath, String outputPath) throws Exception {
		// Load existing transit schedule
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitScheduleReader reader = new TransitScheduleReader(scenario);
		reader.readFile(existingTransitSchedulePath);
		TransitSchedule transitSchedule = scenario.getTransitSchedule();

		// Define the x and y shift values
		double xShift = 394366.1936210745;
		double yShift = 3984739.1570861016;

		// Iterate over all TransitStopFacility and update their coordinates
		for (TransitStopFacility stop : transitSchedule.getFacilities().values()) {
			double x = stop.getCoord().getX();
			double y = stop.getCoord().getY();

			// Adjust x and y coordinates
			double newX = x + xShift;
			double newY = y + yShift;

			// Set the modified coordinates back to the TransitStopFacility
			Coord newCoord = new Coord(newX, newY);
			stop.setCoord(newCoord);
		}

		// Write the updated transit schedule back to the output path
		new TransitScheduleWriter(transitSchedule).writeFile(outputPath);
	}


	public static void main(String[] args) throws Exception {
		// Hardcoded paths
		String existingTransitSchedulePath = "examples/scenarios/Odakyu1/test/transit_schedule_tokyo.xml";
		String outputPath = "examples/scenarios/Odakyu1/test/Ishibashitransitschedule.xml";

		new TransitScheduleMerger().adjustAndSaveTransitSchedule(existingTransitSchedulePath, outputPath);
	}
}
