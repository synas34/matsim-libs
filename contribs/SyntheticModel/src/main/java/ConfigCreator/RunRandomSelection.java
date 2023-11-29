package ConfigCreator;

import MyDMC.NDMCExtension;
import MyDMC.NasirDMCExtension;
import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.URL;
import org.matsim.analysis.TripsAndLegsCSVWriter;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contribs.discrete_mode_choice.components.filters.TourLengthFilter;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.config.TourLengthFilterConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.examples.ExamplesUtils;


public class RunRandomSelection {
	static public void main(String[] args) {
		String configURL = "examples/scenarios/Odakyu3/configbi-levelpt.xml";

		Config config = ConfigUtils.loadConfig(configURL,new DiscreteModeChoiceConfigGroup());
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("examples/scenarios/Odakyu3/test");

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controller = new Controler(scenario);
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(new NasirDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		// Add Tour Length Filter Module
		TourLengthFilterConfigGroup filterConfig = new TourLengthFilterConfigGroup("tourFilter", "TourLengthFilter");
		filterConfig.setMaximumLength(10);
		config.addModule(filterConfig);


		controller.run();


//		final String runId = scenario.getConfig().controler().getRunId();
//		System.out.println("run ID _" + runId);


	}
}
