package MyDMC;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class NasirDMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("NasirEstimatorName").to(NasirTripEstimator.class);
	}
}
