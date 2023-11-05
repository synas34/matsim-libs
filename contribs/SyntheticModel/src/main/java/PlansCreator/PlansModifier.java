package PlansCreator;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlansModifier {

	public void modifyPlans(String existingPlansPath, String outputPath) throws Exception {
		System.out.println("Starting modification process...");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(existingPlansPath);

		NodeList personList = document.getElementsByTagName("person");
		int personCounter = 0;

		for (int i = 0; i < personList.getLength(); i++) {
			Node person = personList.item(i);
			personCounter++;

			// Log every 100th person
			if (personCounter % 100 == 0) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
				System.out.printf("%s  INFO PlansModifier: Processing person # %d%n", timeStamp, personCounter);
			}

			NodeList planList = ((Element) person).getElementsByTagName("plan");

			for (int j = 0; j < planList.getLength(); j++) {
				Node plan = planList.item(j);
				NodeList planElements = plan.getChildNodes();

				String lastEndTime = "00:00:00";
				boolean activityRemoved = false;

				for (int k = 0; k < planElements.getLength(); k++) {
					Node element = planElements.item(k);

					if (element.getNodeName().equals("act")) {
						NamedNodeMap attributes = element.getAttributes();
						Node endTimeAttr = attributes.getNamedItem("end_time");
						if (endTimeAttr != null) {
							String endTime = endTimeAttr.getNodeValue();
							if (endTime.compareTo(lastEndTime) < 0) {
								plan.removeChild(element);
								k--; // Adjust the index to account for the removed node
								activityRemoved = true;
								// Check and remove the following leg if it exists
								if (k + 1 < planElements.getLength()) {
									Node nextNode = planElements.item(k + 1);
									if (nextNode.getNodeName().equals("leg")) {
										plan.removeChild(nextNode);
										k--; // Adjust the index again after removing the leg
									}
								}
							} else {
								lastEndTime = endTime;
							}
						}
					}
				}

				if (activityRemoved) {
					String personId = ((Element) person).getAttribute("id");
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
					System.out.printf("%s  INFO PlansModifier: Removed activities for person ID %s%n", timeStamp, personId);
				}
			}
		}

		// Adjust the root element and document type
		Element root = document.getDocumentElement();
		document.setXmlStandalone(false); // This sets standalone="no"
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

		System.out.println("Saving modified plans to output path...");
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.matsim.org/files/dtd/plans_v4.dtd");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(outputPath));
		transformer.transform(source, result);
		System.out.println("Modification completed!");
	}

	public static void main(String[] args) throws Exception {
		String existingPlansPath = "C:\\Users\\MATSIM\\IdeaProjects\\matsim-libs\\examples\\scenarios\\Odakyu2\\plansv1.xml";
		String outputPath = "C:\\Users\\MATSIM\\IdeaProjects\\matsim-libs\\examples\\scenarios\\Odakyu2\\test\\plansv1.xml";

		new PlansModifier().modifyPlans(existingPlansPath, outputPath);
	}
}
