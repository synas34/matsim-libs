package PlansCreator;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlansModifier {

	public void modifyPlans(String existingPlansPath, String outputPath) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(existingPlansPath);

		NodeList personList = document.getElementsByTagName("person");
		int personCounter = 0;
		int studentsAffected = 0;

		for (int i = 0; i < personList.getLength(); i++) {
			Node person = personList.item(i);
			Element personElement = (Element) person;
			String personId = personElement.getAttribute("id");
			personCounter++;

			boolean isStudent = false;
			// Check if the person is a student
			NodeList activityList = personElement.getElementsByTagName("act");
			for (int actIndex = 0; actIndex < activityList.getLength(); actIndex++) {
				Node act = activityList.item(actIndex);
				Element actElement = (Element) act;
				String actType = actElement.getAttribute("type");
				if ("education".equals(actType)) {
					isStudent = true;
					break;
				}
			}
			NodeList planList = personElement.getElementsByTagName("plan");

			for (int j = 0; j < planList.getLength(); j++) {
				Node plan = planList.item(j);
				NodeList planElements = plan.getChildNodes();

				String lastEndTime = "00:00:00";
				boolean isPreviousNodeAct = false;

				for (int k = 0; k < planElements.getLength(); k++) {
					Node element = planElements.item(k);

					// Remove whitespace nodes
					if (element instanceof Text && element.getNodeValue().trim().isEmpty()) {
						plan.removeChild(element);
						k--;
						continue;
					}

					if (element.getNodeName().equals("act")) {
						NamedNodeMap attributes = element.getAttributes();
						Node endTimeAttr = attributes.getNamedItem("end_time");
						if (endTimeAttr != null) {
							String endTime = endTimeAttr.getNodeValue();
							if (endTime.compareTo(lastEndTime) < 0) {
								plan.removeChild(element);
								logActivityRemoval(personId, isStudent);
								k--;
								continue;
							} else {
								lastEndTime = endTime;
							}
						}
						if (endTimeAttr == null && isPreviousNodeAct) {
							// Add the same end time as the previous activity
							((Element) element).setAttribute("end_time", lastEndTime);
							logEndTimeAdded(personId, lastEndTime, isStudent); // Log the addition
						}
						isPreviousNodeAct = true;
					} else if (element.getNodeName().equals("leg")) {
						if (!isPreviousNodeAct) {
							plan.removeChild(element);
							logLegRemoval(personId, isStudent);
							k--;
							continue;
						}
						isPreviousNodeAct = false;
					}
				}
			}

			if (isStudent) {
				studentsAffected++;
			}
		}

		// Set the proper XML header and DOCTYPE
		document.setXmlStandalone(false);
		Element root = document.getDocumentElement();
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.matsim.org/files/dtd/plans_v4.dtd");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(outputPath));
		transformer.transform(source, result);

		logSummary(personCounter, studentsAffected);
	}

	private void logActivityRemoval(String personId, boolean isStudent) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
		System.out.printf("%s  INFO PlansModifier: Activity removed for person ID %s%s%n", timeStamp, personId, isStudent ? " (student)" : "");
	}

	private void logLegRemoval(String personId, boolean isStudent) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
		System.out.printf("%s  INFO PlansModifier: Leg removed for person ID %s%s%n", timeStamp, personId, isStudent ? " (student)" : "");
	}

	private void logSummary(int totalPersons, int studentsAffected) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
		System.out.printf("%s  INFO PlansModifier: Modification process completed. Total persons processed: %d, Students affected: %d.%n", timeStamp, totalPersons, studentsAffected);
	}

	private void logEndTimeAdded(String personId, String endTime, boolean isStudent) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS").format(new Date());
		System.out.printf("%s  INFO PlansModifier: End time '%s' added to activity for person ID %s%s%n", timeStamp, endTime, personId, isStudent ? " (student)" : "");
	}

	public Set<String> findPersonsWithNonFinalActivitiesWithoutEndTimes(String plansFilePath) {
		Set<String> personsWithMissingEndTimes = new LinkedHashSet<>();
		try {
			File inputFile = new File(plansFilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			NodeList personList = doc.getElementsByTagName("person");

			for (int i = 0; i < personList.getLength(); i++) {
				Node personNode = personList.item(i);
				if (personNode.getNodeType() == Node.ELEMENT_NODE) {
					Element personElement = (Element) personNode;
					String personId = personElement.getAttribute("id");
					NodeList planList = personElement.getElementsByTagName("plan");

					for (int j = 0; j < planList.getLength(); j++) {
						Node planNode = planList.item(j);
						NodeList activityList = ((Element) planNode).getElementsByTagName("act");

						for (int k = 0; k < activityList.getLength() - 1; k++) { // Ignore the last activity
							Node activityNode = activityList.item(k);

							if (activityNode.getNodeType() == Node.ELEMENT_NODE) {
								Element activityElement = (Element) activityNode;

								if (!activityElement.hasAttribute("end_time")) {
									personsWithMissingEndTimes.add(personId);
									// No need to check further activities for this person
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return personsWithMissingEndTimes;
	}

	public static void main(String[] args) throws Exception {
		PlansModifier analyzer = new PlansModifier();
		Set<String> personsWithMissingEndTimes = analyzer.findPersonsWithNonFinalActivitiesWithoutEndTimes("C:\\Users\\MATSIM\\IdeaProjects\\matsim-libs\\examples\\scenarios\\Odakyu2\\test\\plansv1.xml");

		for (String personId : personsWithMissingEndTimes) {
			System.out.println("Person with ID " + personId + " has at least one activity without an end time.");
		}
//		String existingPlansPath = "C:\\Users\\MATSIM\\IdeaProjects\\matsim-libs\\examples\\scenarios\\Odakyu2\\plansv1.xml";
//		String outputPath = "C:\\Users\\MATSIM\\IdeaProjects\\matsim-libs\\examples\\scenarios\\Odakyu2\\test\\plansv1.xml";
//
//		new PlansModifier().modifyPlans(existingPlansPath, outputPath);

	}
}
