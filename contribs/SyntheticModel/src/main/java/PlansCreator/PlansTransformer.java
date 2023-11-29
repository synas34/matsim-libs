package PlansCreator;


import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlansTransformer {

	public void transformAndSavePlans(String existingPlansPath, String outputPath) throws Exception {
		System.out.println("Starting transformation process...");

		System.out.println("Loading plans XML...");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(existingPlansPath);
		NodeList actList = document.getElementsByTagName("act");

		System.out.println("Setting up coordinate systems and transformation...");
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326"); // WGS84
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32654"); // UTM 54N
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

		System.out.println("Transforming coordinates...");
		for (int i = 0; i < actList.getLength(); i++) {
			if (Math.pow(2, (int) (Math.log(i + 1) / Math.log(2))) == (i + 1)) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
				System.out.printf("%s  INFO PlansTransformer:  node # %d%n", timeStamp, i + 1);
			}

			Node act = actList.item(i);
			NamedNodeMap attributes = act.getAttributes();

			String xValue = attributes.getNamedItem("x").getNodeValue();
			String yValue = attributes.getNamedItem("y").getNodeValue();
			Node idAttribute = attributes.getNamedItem("id"); // Assuming there's an 'id' attribute in your Node.

			if (!xValue.equals("NA") && !yValue.equals("NA")) {
				double x = Double.parseDouble(xValue);
				double y = Double.parseDouble(yValue);

			DirectPosition2D sourcePosition = new DirectPosition2D(x, y);
			DirectPosition2D transformedPosition = new DirectPosition2D();
			transform.transform(sourcePosition, transformedPosition);

			attributes.getNamedItem("x").setNodeValue(String.valueOf(transformedPosition.getX()));
			attributes.getNamedItem("y").setNodeValue(String.valueOf(transformedPosition.getY()));
			} else {
				// Debugging statement for NA values
				String nodeId = idAttribute != null ? idAttribute.getNodeValue() : "unknown";
				System.err.println("Debug: Node with id '" + nodeId + "' has 'NA' for x coordinate.");
			}
		}

		System.out.println("Writing transformed plans to output path...");

		FileWriter fileWriter = new FileWriter(outputPath);
		fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fileWriter.write("<!DOCTYPE plans SYSTEM \"http://www.matsim.org/files/dtd/plans_v4.dtd\">\n");

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(fileWriter);
		transformer.transform(source, result);

		fileWriter.close();
		System.out.println("Transformation completed!");
	}


	public void transformStartingMode(String existingPlansPath, String outputPath) {
		System.out.println("Starting transformation process...");

		// Load the existing MATSim scenario
		Config config = ConfigUtils.createConfig();
		config.plans().setInputFile(existingPlansPath);
		Scenario scenario = ScenarioUtils.loadScenario(config);

		// Access the population
		Population population = scenario.getPopulation();

		// Iterate through the population and modify each leg to 'pt'
		for (Person person : population.getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for (PlanElement element : plan.getPlanElements()) {
					if (element instanceof Leg leg) {
						leg.setMode("walk");
					}
				}
			}
		}

		// Write the modified plans to output path
		new PopulationWriter(population).write(outputPath);
		System.out.println("Transformation completed!");
	}

	public static void main(String[] args) throws Exception {
		String existingPlansPath = "examples/scenarios/Odakyu3/plansv5.xml";
		String outputPath = "examples/scenarios/Odakyu3/plansv56.xml";
		new PlansTransformer().transformStartingMode(existingPlansPath, outputPath);

//		new PlansTransformer().transformAndSavePlans(existingPlansPath, outputPath);
	}
}
