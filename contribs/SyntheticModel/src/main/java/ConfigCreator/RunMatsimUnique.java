package ConfigCreator;

import MyDMC.*;
import MyDMC.Sensitivity.*;
import MyDMC.Sensitivity.UrbanIndexDMCExtension;
import MyDMC.Sensitivity.UrbanIndexDMCExtensionSIMPLE;
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
import java.util.Arrays;
import java.util.List;

import static Analysis.GzipExtractor.extractGzipToFile;

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
		String baseOutputPath = "examples/scenarios/Odakyu6/";
		String configFileName = new File(configFilePath).getName().replace(".xml", "");
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

	public static void runSAVSimulation(SimulationConfig simConfig) throws IOException {
		Config config = ConfigUtils.loadConfig(simConfig.configFilePath, new MultiModeDrtConfigGroup(),
			new DvrpConfigGroup(), new OTFVisConfigGroup(), new DiscreteModeChoiceConfigGroup());

		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(simConfig.outputFilePath);
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation(QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime);

		Controler controller = DrtControlerCreator.createControler(config, false);

		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(simConfig.DMCExtension);
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		// Run the simulation
		controller.run();
		// Open modestats.txt in the output directory
		Desktop.getDesktop().open(new File(simConfig.outputFilePath + "/modestats.txt"));
	}

	public static void runAllSimulations() {
		List<SimulationConfig> simulationConfigs = Arrays.asList(
//			new SimulationConfig("examples/scenarios/Odakyu5/configTAXIbase.xml", new UrbanIndexDMCExtensionTAXI()),
//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXITEST.xml", new UrbanIndexDMCExtensionTAXI())
//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXIbaseKantoWide.xml", new UrbanIndexDMCExtensionSAVTAXI())
//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXI(30pc).xml", new UrbanIndexDMCExtensionSAVTAXI())

//			new SimulationConfig("examples/scenarios/Odakyu6/configSAVSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE()),
//			new SimulationConfig("examples/scenarios/Odakyu6/configGEOSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE())

//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXIbase.xml", new UrbanIndexDMCExtensionSAVTAXI_075())
//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXIbase.xml", new UrbanIndexDMCExtensionSAVTAXI_P()),
//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXI(30pc).xml", new UrbanIndexDMCExtensionSAVTAXI_075())

//			new SimulationConfig("examples/scenarios/Odakyu5/configSAVTAXI(50pc).xml", new UrbanIndexDMCExtensionSAVTAXI_P())


//			new SimulationConfig("examples/scenarios/Odakyu6/configGEOSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE_15()),
//			new SimulationConfig("examples/scenarios/Odakyu6/configGEOSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE_x2())
//			new SimulationConfig("examples/scenarios/Odakyu6/configSAVSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE_COST()),
			new SimulationConfig("examples/scenarios/Odakyu6/configGEOSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE_15())

//			new SimulationConfig("examples/scenarios/Odakyu6/configGEOSIMPLE.xml", new UrbanIndexDMCExtensionSIMPLE())


			// Add more configurations as needed
		);

		for (SimulationConfig simConfig : simulationConfigs) {
			try {
				runSAVSimulation(simConfig);
				System.gc(); // This is a suggestion, not a guarantee.
			} catch (Exception e) {
				System.err.println("Simulation failed for config file: " + simConfig.configFilePath + " with DMC Extension: " + simConfig.getClass().getSimpleName());
				e.printStackTrace();
			}

			// Suggest garbage collection
			System.gc(); // This is a suggestion, not a guarantee.
			System.gc(); // This is a suggestion, not a guarantee.

			// Nullify references if they are no longer needed
			simConfig = null;
		}
	}
	public static void main(String[] args) {

		// Place necessary simConfigs into runAllSimulations, then press play
		runAllSimulations();



	}
}
