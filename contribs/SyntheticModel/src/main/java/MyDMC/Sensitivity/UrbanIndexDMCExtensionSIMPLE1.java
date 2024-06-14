package MyDMC.Sensitivity;

import MyDMC.Trial.UrbanIndexTripEstimatorSIMPLE;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtensionSIMPLE1 extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimatorSIMPLE1.class);
	}
}
