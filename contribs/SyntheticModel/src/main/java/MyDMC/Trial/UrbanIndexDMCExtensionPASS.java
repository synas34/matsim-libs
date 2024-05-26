package MyDMC.Trial;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtensionPASS extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimatorMAY.class);
	}
}
