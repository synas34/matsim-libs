package ConfigCreator;

import MyDMC.*;
import MyDMC.Sensitivity.*;
import MyDMC.Sensitivity.UrbanIndexDMCExtension;
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
		String baseOutputPath = "examples/scenarios/Odakyu5/";
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

	public static void main(String[] args) {

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath1 = "examples/scenarios/Odakyu5/configSAVbase.xml";
		AbstractDiscreteModeChoiceExtension DMCExtension1 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig1 = new SimulationConfig(configFilePath1, DMCExtension1);
		System.err.println("Simulation failed for output file: " + simConfig1.outputFilePath );

		try {
			runSAVSimulation(simConfig1);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath1 + " with DMC Extension: " + DMCExtension1.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath2 = "examples/scenarios/Odakyu5/configSAV(30pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension2 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig2 = new SimulationConfig(configFilePath2, DMCExtension2);
			System.err.println("Simulation failed for output file: " + simConfig2.outputFilePath );

		try {
			runSAVSimulation(simConfig2);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath2 + " with DMC Extension: " + DMCExtension2.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath3 = "examples/scenarios/Odakyu5/configSAV(50pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension3 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig3 = new SimulationConfig(configFilePath3, DMCExtension3);
			System.err.println("Simulation failed for output file: " + simConfig3.outputFilePath );

		try {
			runSAVSimulation(simConfig3);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath3 + " with DMC Extension: " + DMCExtension3.getClass().getSimpleName());
			e.printStackTrace();		}


		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath4 = "examples/scenarios/Odakyu5/configSAVbase.xml";
		AbstractDiscreteModeChoiceExtension DMCExtension4 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig4 = new SimulationConfig(configFilePath4, DMCExtension4);
		System.err.println("Simulation failed for output file: " + simConfig4.outputFilePath );

		try {
			runSAVSimulation(simConfig4);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath4 + " with DMC Extension: " + DMCExtension4.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath5 = "examples/scenarios/Odakyu5/configSAV(30pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension5 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig5 = new SimulationConfig(configFilePath5, DMCExtension5);
		System.err.println("Simulation failed for output file: " + simConfig5.outputFilePath );

		try {
			runSAVSimulation(simConfig5);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath5 + " with DMC Extension: " + DMCExtension5.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath6 = "examples/scenarios/Odakyu5/configSAV(50pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension6 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig6 = new SimulationConfig(configFilePath6, DMCExtension6);
		System.err.println("Simulation failed for output file: " + simConfig6.outputFilePath );

		try {
			runSAVSimulation(simConfig6);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath6 + " with DMC Extension: " + DMCExtension6.getClass().getSimpleName());
			e.printStackTrace();		}


		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath7 = "examples/scenarios/Odakyu5/configSAVbase.xml";
		AbstractDiscreteModeChoiceExtension DMCExtension7 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig7 = new SimulationConfig(configFilePath7, DMCExtension7);
		System.err.println("Simulation failed for output file: " + simConfig7.outputFilePath );

		try {
			runSAVSimulation(simConfig7);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath7 + " with DMC Extension: " + DMCExtension7.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath8 = "examples/scenarios/Odakyu5/configSAV(30pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension8 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig8 = new SimulationConfig(configFilePath8, DMCExtension8);
		System.err.println("Simulation failed for output file: " + simConfig8.outputFilePath );

		try {
			runSAVSimulation(simConfig8);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath8 + " with DMC Extension: " + DMCExtension8.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath9 = "examples/scenarios/Odakyu5/configSAV(50pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension9 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig9 = new SimulationConfig(configFilePath9, DMCExtension9);
		System.err.println("Simulation failed for output file: " + simConfig9.outputFilePath );

		try {
			runSAVSimulation(simConfig9);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath9 + " with DMC Extension: " + DMCExtension9.getClass().getSimpleName());
			e.printStackTrace();		}


		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath10 = "examples/scenarios/Odakyu5/configSAVbase.xml";
		AbstractDiscreteModeChoiceExtension DMCExtension10 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig10 = new SimulationConfig(configFilePath10, DMCExtension10);
		System.err.println("Simulation failed for output file: " + simConfig10.outputFilePath );

		try {
			runSAVSimulation(simConfig10);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath10 + " with DMC Extension: " + DMCExtension10.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath11 = "examples/scenarios/Odakyu5/configSAV(30pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension11 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig11 = new SimulationConfig(configFilePath11, DMCExtension11);
		System.err.println("Simulation failed for output file: " + simConfig11.outputFilePath );

		try {
			runSAVSimulation(simConfig11);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath11 + " with DMC Extension: " + DMCExtension11.getClass().getSimpleName());
			e.printStackTrace();		}

		////////////////////////////////////////////////////// Example configuration - replace with actual paths and extensions as needed

		String configFilePath12 = "examples/scenarios/Odakyu5/configSAV(50pc).xml";
		AbstractDiscreteModeChoiceExtension DMCExtension12 = new UrbanIndexDMCExtension();
		SimulationConfig simConfig12 = new SimulationConfig(configFilePath12, DMCExtension12);
		System.err.println("Simulation failed for output file: " + simConfig12.outputFilePath );

		try {
			runSAVSimulation(simConfig12);
		} catch (Exception e) {
			System.err.println("Simulation failed for config file: " + configFilePath12 + " with DMC Extension: " + DMCExtension12.getClass().getSimpleName());
			e.printStackTrace();		}

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
