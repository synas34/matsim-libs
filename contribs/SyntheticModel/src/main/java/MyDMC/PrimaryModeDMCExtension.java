package MyDMC;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class PrimaryModeDMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("NasirEstimatorName").to(PrimaryModeTripEstimator.class);
	}
}
