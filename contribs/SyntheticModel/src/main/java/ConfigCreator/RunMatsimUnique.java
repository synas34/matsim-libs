package ConfigCreator;

import MyDMC.*;
import MyDMC.Trial.SAVasRideDMCExtension;
import MyDMC.Trial.UrbanIndexSAVDMCExtension;
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


	public static void RunSAVSimulation (String configpath, String outputpath, AbstractDiscreteModeChoiceExtension DMCExtension) throws IOException {


		Config config;
			config = ConfigUtils.loadConfig( configpath, new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(), new OTFVisConfigGroup(),new DiscreteModeChoiceConfigGroup());


		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setOutputDirectory(outputpath);
		// possibly modify config here
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation((QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime));

		Controler controller = DrtControlerCreator.createControler(config, false);

		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(DMCExtension);
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);


		// Run the simulation
		controller.run();

		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));



	}

	public static void main(String[] args) throws IOException {

		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "examples/scenarios/Odakyu4/confignewSAV.xml", new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(), new OTFVisConfigGroup(),new DiscreteModeChoiceConfigGroup());
		} else {
			config = ConfigUtils.loadConfig( args );
		}

		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setOutputDirectory("examples/scenarios/Odakyu4/output(200)Jan07NEWBASE");
		// possibly modify config here
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation((QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime));

		Controler controller = DrtControlerCreator.createControler(config, false);

		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(new UrbanIndexSAVDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);

		// Run the simulation
		controller.run();
		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));



	}
}
