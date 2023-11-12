package ConfigCreator;

import MyDMC.MyDMCExtension;
import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.URL;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.examples.ExamplesUtils;


public class RunRandomSelection {
	static public void main(String[] args) {
		String configURL = "examples/scenarios/UrbanLine/Extension/base/config.xml";

		Config config = ConfigUtils.loadConfig(configURL);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("examples/scenarios/UrbanLine/Extension/base/output");

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controller = new Controler(scenario);
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		DiscreteModeChoiceConfigurator.configureAsSubtourModeChoiceReplacement(config);
		controller.addOverridingModule(new MyDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);
		controller.run();
	}
}
