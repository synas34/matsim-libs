package MyDMC;

import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class MyDMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {
		bindTripEstimator("MyEstimatorName").to(MyTripEstimator.class);
	}
}
