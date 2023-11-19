package ConfigCreator;

import MyDMC.NDMCExtension;
import MyDMC.NasirDMCExtension;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.extension.preplanned.run.PreplannedDrtModeModule;
import org.matsim.contrib.drt.run.*;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
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

	public static void main(String[] args) throws IOException {

		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "examples/scenarios/UrbanLine/Extension/zone1/config.xml", new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(), new OTFVisConfigGroup(),new DiscreteModeChoiceConfigGroup() );
		} else {
			config = ConfigUtils.loadConfig( args );
		}

		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setOutputDirectory("examples/scenarios/UrbanLine/Extension/zone1/output");
		// possibly modify config here
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation((QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime));
//		config.changeMode().setModes();

		Controler controller = DrtControlerCreator.createControler(config, false);

		Scenario scenario = controller.getScenario();
		// Add transit schedule
		TransitSchedule transitSchedule = scenario.getTransitSchedule();
		TransitRoute transitRoute = transitSchedule.getTransitLines().get(Id.create("Shuttle", TransitRoute.class)).getRoutes().get(Id.create("Suburbs", TransitRoute.class));
		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(new NDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);
		// Add VehicleTracker
		VehicleTracker vehicleTracker = new VehicleTracker();
		TransitRouteAccessEgressAnalysis analysis = new TransitRouteAccessEgressAnalysis(transitRoute, vehicleTracker);

		// Add the analysis and vehicle tracker as event handlers
		controller.getEvents().addHandler(analysis);
		controller.getEvents().addHandler(vehicleTracker);

		// Run the simulation
		controller.run();

		// Print the statistics after the simulation
		analysis.printStats();
		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));



	}
}
