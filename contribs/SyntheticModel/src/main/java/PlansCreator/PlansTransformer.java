package PlansCreator;


import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
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

			double x = Double.parseDouble(attributes.getNamedItem("x").getNodeValue());
			double y = Double.parseDouble(attributes.getNamedItem("y").getNodeValue());

			DirectPosition2D sourcePosition = new DirectPosition2D(y, x);
			DirectPosition2D transformedPosition = new DirectPosition2D();
			transform.transform(sourcePosition, transformedPosition);

			attributes.getNamedItem("x").setNodeValue(String.valueOf(transformedPosition.getX()));
			attributes.getNamedItem("y").setNodeValue(String.valueOf(transformedPosition.getY()));
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
	public static void main(String[] args) throws Exception {
		String existingPlansPath = "examples/scenarios/Odakyu1/plansv3.xml";
		String outputPath = "examples/scenarios/Odakyu1/test/plansv3.xml";

		new PlansTransformer().transformAndSavePlans(existingPlansPath, outputPath);
	}
}
