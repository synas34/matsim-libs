package MyDMC.Trial;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class Jan12_2DMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(Jan12_2TripEstimator.class);
	}
}
