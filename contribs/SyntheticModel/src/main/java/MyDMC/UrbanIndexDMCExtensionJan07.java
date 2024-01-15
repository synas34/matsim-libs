package MyDMC;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtensionJan07 extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimatorJan07.class);
	}
}
