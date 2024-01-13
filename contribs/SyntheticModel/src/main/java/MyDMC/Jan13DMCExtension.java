package MyDMC;

import MyDMC.Trial.Jan06TripEstimator;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class Jan13DMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("MyEstimatorName").to(Jan13TripEstimator.class);
	}
}
