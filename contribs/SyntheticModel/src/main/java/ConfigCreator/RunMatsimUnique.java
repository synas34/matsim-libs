package ConfigCreator;

import MyDMC.*;
import MyDMC.Sensitivity.Jan11DMCExtension1;
import MyDMC.Sensitivity.Jan11DMCExtension_2;
import MyDMC.Sensitivity.Jan11DMCExtension_3;
import MyDMC.Sensitivity.Jan11DMCExtension_4;
import MyDMC.Trial.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.extension.preplanned.run.PreplannedDrtModeModule;
import org.matsim.contrib.drt.run.*;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contribs.discrete_mode_choice.components.filters.TourLengthFilter;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.config.TourLengthFilterConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.analysis.VehicleTracker;
import org.matsim.vis.otfvis.OTFVisConfigGroup;
import org.matsim.api.core.v01.Id;
import org.matsim.pt.analysis.TransitRouteAccessEgressAnalysis;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;


public class RunMatsimUnique {

	public static void runSAVSimulation(String configFilePath, String outputFilePath, AbstractDiscreteModeChoiceExtension DMCExtension) throws IOException {
		Config config = ConfigUtils.loadConfig(configFilePath, new MultiModeDrtConfigGroup(),
			new DvrpConfigGroup(), new OTFVisConfigGroup(), new DiscreteModeChoiceConfigGroup());

		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(outputFilePath);
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation(QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime);

		Controler controller = DrtControlerCreator.createControler(config, false);

		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(DMCExtension);
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		// Run the simulation
		controller.run();
		// Open modestats.txt in the output directory
		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));
	}

	public static void runSimulation(String configFilePath, String outputFilePath, AbstractDiscreteModeChoiceExtension DMCExtension) throws IOException {
		Config config = ConfigUtils.loadConfig(configFilePath, new DiscreteModeChoiceConfigGroup());

		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(outputFilePath);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controller = new Controler(scenario);
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(DMCExtension);
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		controller.run();
		// Open modestats.txt in the output directory
		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));
	}

	public static void main(String[] args) throws IOException {
		String[] configFiles = {
			"examples/scenarios/Odakyu4/confignewbase.xml",
		};

		AbstractDiscreteModeChoiceExtension[] DMCExtensions = {
			new Jan06DMCExtension(),
		};

		for (String configFilePath : configFiles) {
			int dmc = 1;
			for (AbstractDiscreteModeChoiceExtension DMCExtension : DMCExtensions) {
//				String outputFilePath = configFilePath + "_" + dmc;
				String outputFilePath = "examples/scenarios/Odakyu4/testnewbase_" + dmc;
				dmc ++;
//				runSAVSimulation(configFilePath, outputFilePath, DMCExtension);
				runSimulation(configFilePath, outputFilePath, DMCExtension);
			}
		}
	}

	private static String getOutputFilePath(String configFilePath, AbstractDiscreteModeChoiceExtension DMCExtension) {
		String configFileName = new File(configFilePath).getName();
		String DMCExtensionName = DMCExtension.getClass().getSimpleName();
		return "output_directory/" + configFileName + "_" + DMCExtensionName;
	}

}


