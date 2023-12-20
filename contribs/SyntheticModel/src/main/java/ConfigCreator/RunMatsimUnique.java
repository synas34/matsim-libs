package ConfigCreator;

import MyDMC.*;
import MyDMC.Trial.SAVasRideDMCExtension;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.RaptorParameters;
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
import java.util.TreeMap;


public class RunMatsimUnique {


	public static void main(String[] args) throws IOException {

		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "examples/scenarios/Odakyu4/configSAV2.xml", new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(), new OTFVisConfigGroup(),new DiscreteModeChoiceConfigGroup(), new SwissRailRaptorConfigGroup());
		} else {
			config = ConfigUtils.loadConfig( args );
		}


		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setOutputDirectory("examples/scenarios/Odakyu4/outputDec14(200)NewUtil-1seatSAV");
		// possibly modify config here
		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation((QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime));

		// Create an instance of RaptorParameters
		SwissRailRaptorConfigGroup raptorConfig = (SwissRailRaptorConfigGroup) config.getModules().get("raptor");
		RaptorParameters raptorParameters = new RaptorParameters(raptorConfig);

		// Set the marginal utility of travel time for different travel modes
		raptorParameters.setMarginalUtilityOfTravelTime_utl_s("bike", -50000000);


		Controler controller = DrtControlerCreator.createControler(config, false);

		// Add Discrete Choice Module
		controller.addOverridingModule(new DiscreteModeChoiceModule());
		controller.addOverridingModule(new NasirSAVDMCExtension());
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);


		// Run the simulation
		controller.run();
		Desktop.getDesktop().open(new File(config.controler().getOutputDirectory() + "/modestats.txt"));

	}
}
