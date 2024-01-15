package MyDMC.Sensitivity;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimator.class);
	}
}
