/**
 * 
 */
package playground.yu.newPlans;

import java.util.List;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.routes.GenericRouteImpl;

/**
 * @author yu
 * 
 */
public class NewPopWithRouteFilter extends NewPopulation {
	private String criterion;
	private boolean hasCriterion = false;
	private PersonImpl pi = null;

	public NewPopWithRouteFilter(PopulationImpl population, String filename,
			String criterion) {
		super(population, filename);
		this.criterion = criterion;
	}

	@Override
	public void run(Person person) {
		pi = new PersonImpl(person.getId());
		hasCriterion = false;
		for (Plan plan : person.getPlans()) {
			boolean retain = run((PlanImpl) plan);
			if (retain)
				pi.addPlan(plan);
			hasCriterion |= retain;
		}
		if (hasCriterion)
			pw.writePerson(pi);
	}

	public boolean run(Plan plan) {
		boolean retain = false;
		List<PlanElement> pes = plan.getPlanElements();
		for (int i = 1; i < pes.size(); i += 2) {
			Leg leg = (Leg) pes.get(i);
			if (leg.getMode().equals(TransportMode.pt)) {
				GenericRouteImpl gri = (GenericRouteImpl) leg.getRoute();
				if (gri.getRouteDescription().contains(criterion)) {
					return true;
				}
			}
		}
		return retain;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Gbl.startMeasurement();

		final String netFilename = "../berlin-bvg09/pt/baseplan_900s_smallnetwork/network.multimodal.xml.gz";
		final String plansFilename = "../berlin-bvg09/pt/baseplan_900s_smallnetwork/plan.routedOevModell.xml.gz";
		final String outputFilename = "../berlin-bvg09/pt/baseplan_900s_smallnetwork/test/plan.routedOevModell.BVB344.xml.gz";

		Scenario s = new ScenarioImpl();

		NetworkLayer network = (NetworkLayer) s.getNetwork();
		new MatsimNetworkReader(network).readFile(netFilename);

		PopulationImpl population = (PopulationImpl) s.getPopulation();

		NewPopWithRouteFilter npwp = new NewPopWithRouteFilter(population,
				outputFilename, "BVB----344");

		new MatsimPopulationReader(population, network).readFile(plansFilename);

		npwp.run(population);

		npwp.writeEndPlans();

		System.out.println("--> Done!");
		Gbl.printElapsedTime();
		System.exit(0);
	}

}
