package MyDMC.Trial;

import MyDMC.Sensitivity.UrbanIndexTripEstimatorTAXI;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class UrbanIndexDMCExtensionTAXI_LONGDIST extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(UrbanIndexTripEstimatorTAXI.class);
	}
}
