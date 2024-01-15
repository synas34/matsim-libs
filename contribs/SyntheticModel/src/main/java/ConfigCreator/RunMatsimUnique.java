package ConfigCreator;

import MyDMC.*;
import MyDMC.Sensitivity.*;
import MyDMC.Trial.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.run.*;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class RunMatsimUnique {

	static class SimulationConfig {
		String configFilePath;
		AbstractDiscreteModeChoiceExtension DMCExtension;
		String outputFilePath;

		SimulationConfig(String configFilePath, AbstractDiscreteModeChoiceExtension DMCExtension) {
			this.configFilePath = configFilePath;
			this.DMCExtension = DMCExtension;
			this.outputFilePath = getUniqueOutputFilePath(configFilePath, DMCExtension);
		}
	}

	private static String getUniqueOutputFilePath(String configFilePath, AbstractDiscreteModeChoiceExtension DMCExtension) {
		String baseOutputPath = "output_directory/";
		String configFileName = new File(configFilePath).getName();
		String DMCExtensionName = DMCExtension.getClass().getSimpleName();
		String outputPath = baseOutputPath + configFileName + "_" + DMCExtensionName;

		// Check if the file already exists and modify the name to avoid overwriting
		int counter = 1;
		while (new File(outputPath).exists()) {
			outputPath = baseOutputPath + configFileName + "_" + DMCExtensionName + "_" + counter;
			counter++;
		}
		return outputPath;
	}

	public static void runSimulation(SimulationConfig simConfig) throws IOException {
		Config config = ConfigUtils.loadConfig(simConfig.configFilePath, new DiscreteModeChoiceConfigGroup());
		config.controler(	).setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(simConfig.outputFilePath);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controller = new Controler(scenario);
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(simConfig.DMCExtension);
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		controller.run();
		Desktop.getDesktop().open(new File(simConfig.outputFilePath + "/modestats.txt"));
	}

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

	public static void main(String[] args) {

		// Example configuration - replace with actual paths and extensions as needed
		String configFilePath = "your_config_file_path.xml";
		AbstractDiscreteModeChoiceExtension DMCExtension = new Jan06DMCExtension();
		SimulationConfig simConfig = new SimulationConfig(configFilePath, DMCExtension);

		try {
			runSimulation(simConfig);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath + " with DMC Extension: " + DMCExtension.getClass().getSimpleName());
			e.printStackTrace();
		}

//		AbstractDiscreteModeChoiceExtension[] DMCExtensions = {
//			new Jan06DMCExtension(),
//			// Add other extensions as needed
//		};
//
//		String[] configFiles = {
//			"examples/scenarios/Odakyu5/configSAV(Req%12%50).xml",
//			"examples/scenarios/Odakyu5/configSAV(Req%30%75).xml",
//			"examples/scenarios/Odakyu5/configSAV(Req%30%50).xml",
//		};
//		for (String configFilePath : configFiles) {
//			for (AbstractDiscreteModeChoiceExtension DMCExtension : DMCExtensions) {
//				SimulationConfig simConfig = new SimulationConfig(configFilePath, DMCExtension);
//				try {
//					runSimulation(simConfig);
//				} catch (Exception e) {
//					System.err.println("Simulation failed for config file: " + configFilePath + " with DMC Extension: " + DMCExtension.getClass().getSimpleName());
//					e.printStackTrace();
//				}
//			}
//		}



	}
}
