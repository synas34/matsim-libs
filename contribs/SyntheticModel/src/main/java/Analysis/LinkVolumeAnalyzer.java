package Analysis;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LinkVolumeAnalyzer implements LinkEnterEventHandler {

	private final Set<String> linkIdsOfInterest;
	private final Map<String, int[]> linkHourlyCounts = new HashMap<>();

	public LinkVolumeAnalyzer(Set<String> linkIdsOfInterest) {
		this.linkIdsOfInterest = linkIdsOfInterest;
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (linkIdsOfInterest.contains(event.getLinkId().toString())) {
			int hour = (int) event.getTime() / 3600;  // convert time to hours

			linkHourlyCounts.computeIfAbsent(event.getLinkId().toString(), k -> new int[24])[hour]++;
		}
	}

	public static void main(String[] args) {
		String pathToEventsFile = "examples/scenarios/UrbanLine/SuburbanCorex5/outputFMLM/output_events.xml.gz";  // Modify this to your events file
		String outputPath = "contribs/SyntheticModel/test/volume_counts.csv";  // Modify this to your desired output path

		Set<String> linkIdsOfInterest = new HashSet<>(Arrays.asList("100718_100719", "100716_100715_r", "link-id_station952.0_station1988.0"));  // Replace with your link IDs

		EventsManager eventsManager = EventsUtils.createEventsManager();
		LinkVolumeAnalyzer analyzer = new LinkVolumeAnalyzer(linkIdsOfInterest);
		eventsManager.addHandler(analyzer);

		MatsimEventsReader eventsReader = new MatsimEventsReader(eventsManager);
		eventsReader.readFile(pathToEventsFile);

		analyzer.writeToCSV(outputPath);
	}

	public void writeToCSV(String outputPath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
			writer.write("LinkID");
			for (int i = 0; i < 24; i++) {
				writer.write(",Hour_" + i);
			}
			writer.newLine();

			for (String linkId : linkIdsOfInterest) {
				writer.write(linkId);
				int[] counts = linkHourlyCounts.getOrDefault(linkId, new int[24]);
				for (int count : counts) {
					writer.write("," + count);
				}
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reset(int iteration) {
		linkHourlyCounts.clear();
	}
}
