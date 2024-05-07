package Analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
public class GzipExtractor {

	public static void extractGzipToFile(String gzipFilePath, String outputFilePath) {
		try (
			InputStream fileInputStream = Files.newInputStream(Paths.get(gzipFilePath));
			GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String gzipFilePath = "examples/scenarios/Odakyu5/configGEOSIMPLE_UrbanIndexDMCExtensionSIMPLE_x2/output_trips.csv.gz";
		String outputFilePath = "C:/Users/MATSIM/Downloads/trips(May01)_x2.csv";
		extractGzipToFile(gzipFilePath, outputFilePath);
//		String gzipFilePath2 = "examples/scenarios/Odakyu5/outputJan13_2dmc2/output_trips.csv.gz";
//		String outputFilePath2 = "C:/Users/Public/Documents/RStudio/SAVMNLv1/trips(Req%30%75).csv";
//		extractGzipToFile(gzipFilePath2, outputFilePath2);
//		String gzipFilePath3 = "examples/scenarios/Odakyu5/outputJan13_1dmc1/output_vehicles.xml.gz";
//		String outputFilePath3 = "C:/Users/MATSIM/Downloads/output_vehicles.csv";
//		extractGzipToFile(gzipFilePath3, outputFilePath3);
	}
}
