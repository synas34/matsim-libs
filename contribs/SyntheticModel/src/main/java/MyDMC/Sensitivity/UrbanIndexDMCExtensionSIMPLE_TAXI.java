package MyDMC.Sensitivity;

import MyDMC.Trial.UrbanIndexTripEstimatorSIMPLE;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtensionSIMPLE_TAXI extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimatorSIMPLE_TAXI.class);
	}
}
