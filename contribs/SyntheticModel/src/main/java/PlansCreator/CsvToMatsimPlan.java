package PlansCreator;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvToMatsimPlan {

	public static void main(String[] args) throws IOException {
		String csvFile = "contribs/SyntheticModel/test/Apr03Trips(col_null).csv"; // Replace with your CSV file path
		String line;
		String cvsSplitBy = ","; // CSV delimiter

		// Initialize MATSim Scenario and Population
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			while ((line = br.readLine()) != null) {
				// Use comma as separator
				String[] trip = line.split(cvsSplitBy);

				String personId = trip[0].replace("\"", "");
				String originActType = trip[2].replace("\"", "");
				String destActType = trip[3].replace("\"", "");
				double departureTime = convertTimeToSeconds(trip[4]);
				double origLon = Double.parseDouble(trip[5]);
				double origLat = Double.parseDouble(trip[6]);
				DirectPosition2D transformedOrigin = CoordinateTransformer(origLat,origLon , "EPSG:4326", "EPSG:32654");
				Coord origcoord = new Coord(transformedOrigin.getX(), transformedOrigin.getY());
				double destLon = Double.parseDouble(trip[7]);
				double destLat = Double.parseDouble(trip[8]);
				DirectPosition2D transformedDest = CoordinateTransformer(destLat , destLon, "EPSG:4326", "EPSG:32654");
				Coord destcoord = new Coord(transformedDest.getX(), transformedDest.getY());
				String carAvail = trip[9].replace("\"", ""); // Assuming the car_avail is the 9th column (index 8)
				String bikeAvail = trip[10].replace("\"", "");
				String license = trip[11].replace("\"", "");
				String choice = trip[12].replace("\"", "");
				String access = trip[13].replace("\"", "");
				String InScope = trip[14].replace("\"", "");
				String EndScope = trip[15].replace("\"", "");
				String UrbanContext = trip[18].replace("\"", "");
				String DestUrbanContext = trip[19].replace("\"", "");

				// Create a person and plan
				Person person = populationFactory.createPerson(Id.createPersonId(personId));
				Plan plan = populationFactory.createPlan();
				PersonUtils.setCarAvail(person, carAvail);
				PersonUtils.setLicence(person, license);
				person.getAttributes().putAttribute("hasBike",bikeAvail);
//				person.getCustomAttributes().put("hasBike",bikeAvail);
				person.getAttributes().putAttribute("MainModeChoice",choice);
				person.getAttributes().putAttribute("AccessModeChoice",access);
				person.getAttributes().putAttribute("InScope",InScope);
				person.getAttributes().putAttribute("EndScope",EndScope);
				person.getAttributes().putAttribute("UrbanContext",UrbanContext);
				person.getAttributes().putAttribute("DestUrbanContext",DestUrbanContext);

				// Create and add origin activity
				Activity originActivity = populationFactory.createActivityFromCoord(originActType, origcoord);
				originActivity.setEndTime(departureTime);
				plan.addActivity(originActivity);

				// Create and add leg
				Leg leg = populationFactory.createLeg("pt");
				plan.addLeg(leg);

				// Create and add destination activity
				Activity destinationActivity = populationFactory.createActivityFromCoord(destActType, destcoord);
				plan.addActivity(destinationActivity);

				// Add plan to person and person to population
				person.addPlan(plan);
				population.addPerson(person);
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Write the population to a file
		new PopulationWriter(population).write("contribs/SyntheticModel/test/plans.xml");
	}

	private static double convertTimeToSeconds(String time) {
		String cleanTime = time.replace("\"", "");
		String[] parts = cleanTime.split(":");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		int seconds = Integer.parseInt(parts[2]);
		return hours * 3600 + minutes * 60 + seconds;
	}


	private static DirectPosition2D CoordinateTransformer(double lon, double lat, String sourceCRSCode, String targetCRSCode) throws Exception {
		CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSCode);
		CoordinateReferenceSystem targetCRS = CRS.decode(targetCRSCode);
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		DirectPosition2D sourcePosition = new DirectPosition2D(lon, lat);
		DirectPosition2D transformedPosition = new DirectPosition2D();
		transform.transform(sourcePosition, transformedPosition);
		return transformedPosition;
	}

}
