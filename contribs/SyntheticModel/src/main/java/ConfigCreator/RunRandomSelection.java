package ConfigCreator;

import MyDMC.NasirDMCExtension;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;


public class RunRandomSelection {
	static public void main(String[] args) {
		String configURL = "examples/scenarios/Odakyu2/config.xml";

		Config config = ConfigUtils.loadConfig(configURL, new DiscreteModeChoiceConfigGroup());
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("examples/scenarios/Odakyu2/output");

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controller = new Controler(scenario);

		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(new NasirDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);
		controller.run();

	}
}
