package MyDMC;

import MyDMC.Trial.UrbanIndexTripEstimatorMAY;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class DMCExtensionSIMPLE extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(TripEstimatorSIMPLE.class);
	}
}
