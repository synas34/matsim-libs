package MyDMC.Trial;


import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class SAVasRideDMCExtension extends AbstractDiscreteModeChoiceExtension {
	@Override
	public void installExtension() {

		bindTripEstimator("NasirEstimatorName").to(SAVasRideTripEstimator.class);
	}
}
