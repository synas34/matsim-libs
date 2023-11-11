package TransitCreator;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class TransitVehicleGenerator {

	public static void main(String[] args) {
		String inputFile = "examples/scenarios/Odakyu1/transitschedule11.xml";
		String outputFile = "examples/scenarios/Odakyu1/test/transitVehicles11.xml";
		generateTransitVehicleXml(inputFile, outputFile);
	}

	public static void generateTransitVehicleXml(String inputFile, String outputFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(inputFile));

			NodeList departures = doc.getElementsByTagName("departure");
			StringBuilder vehicleIds = new StringBuilder();

			for (int i = 0; i < departures.getLength(); i++) {
				Node departure = departures.item(i);
				NamedNodeMap attributes = departure.getAttributes();
				String vehicleRefId = attributes.getNamedItem("vehicleRefId").getNodeValue();
				vehicleIds.append("\t<vehicle id=\"").append(vehicleRefId).append("\" type=\"pt\"/>\n");
			}

			String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
				"<vehicleDefinitions xmlns=\"http://www.matsim.org/files/dtd\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:schemaLocation=\"http://www.matsim.org/files/dtd " +
				"http://www.matsim.org/files/dtd/vehicleDefinitions_v2.0.xsd\">\n\n" +
				"\t<vehicleType id=\"pt\">\n\n" +
				"\t\t<capacity seats=\"333\" standingRoomInPersons=\"667\">\n\n" +
				"\t\t</capacity>\n" +
				"\t\t<length meter=\"36.0\"/>\n" +
				"\t\t<width meter=\"1.0\"/>\n" +
				"\t\t<maximumVelocity meterPerSecond=\"25.0\"/>\n" +
				"\t\t<costInformation>\n\n" +
				"\t\t</costInformation>\n" +
				"\t\t<passengerCarEquivalents pce=\"1.0\"/>\n" +
				"\t\t<networkMode networkMode=\"car\"/>\n" +
				"\t\t<flowEfficiencyFactor factor=\"1.0\"/>\n" +
				"\t</vehicleType>\n\n" +
				vehicleIds.toString() +
				"</vehicleDefinitions>";

			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write(xmlContent);
			writer.close();

			System.out.println("File " + outputFile + " has been generated successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
