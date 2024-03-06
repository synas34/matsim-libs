package MyDMC.Trial;

import MyDMC.Sensitivity.UrbanIndexTripEstimatorSAVTAXI;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class FEB09DMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(Feb09TripEstimator.class);
	}
}
