package NetworkCreator;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

public class NetworkMerger {

		private static final String NETWORK_FILENAME = "network.xml";

		public void transformAndSaveNetwork(String existingNetworkPath, String outputPath) throws Exception {
			// Load existing network
			Network network = NetworkUtils.readNetwork(existingNetworkPath);

			// Define source and target coordinate systems
			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326"); // WGS84
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32654"); // UTM 54N

			// Create a transformation
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

			// Iterate over all nodes and update their coordinates
			for (Node node : network.getNodes().values()) {
				double x = node.getCoord().getX();
				double y = node.getCoord().getY();

				// Create a DirectPosition for transformation
				DirectPosition2D sourcePosition = new DirectPosition2D(x, y);
				DirectPosition2D transformedPosition = new DirectPosition2D();

				try {
					// Perform the transformation
					transform.transform(sourcePosition, transformedPosition);

					// Convert DirectPosition2D back to MATSim's Coord
					Coord newCoord = new Coord(transformedPosition.getX(), transformedPosition.getY());
					node.setCoord(newCoord);
				} catch (Exception e) {
					e.printStackTrace();
					// Handle the transformation exception appropriately
				}
			}



			// Write the updated network back to the output path
			new NetworkWriter(network).write(outputPath);
		}

	public static void main(String[] args) throws Exception {
		// Hardcoded paths
		String existingNetworkPath = "examples/scenarios/Odakyu1/rrte.xml";
		String outputPath = "examples/scenarios/Odakyu1/test/rttte.xml";

		new NetworkMerger().transformAndSaveNetwork(existingNetworkPath, outputPath);
	}

}


