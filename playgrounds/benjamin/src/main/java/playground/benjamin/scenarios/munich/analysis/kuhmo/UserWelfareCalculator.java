/* *********************************************************************** *
 * project: org.matsim.*
 * WelfareCalculator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.benjamin.scenarios.munich.analysis.kuhmo;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.gbl.Gbl;

/**
 * @author benjamin
 *
 */
public class UserWelfareCalculator {
	private static final Logger logger = Logger.getLogger(UserWelfareCalculator.class);

	private final double betaLogit;
	private final double marginalUtlOfMoney;
	private int nullScore = 0;
	private int minusScore = 0;
	private int noValidPlanScore = 0;
	private final int maxWarnCnt = 3;
	
	public UserWelfareCalculator(String configFile) {
		Config config = ConfigUtils.loadConfig(configFile);
		PlanCalcScoreConfigGroup pcs = config.planCalcScore();
		this.betaLogit = pcs.getBrainExpBeta();
		this.marginalUtlOfMoney = pcs.getMarginalUtilityOfMoney();
	}

	public void reset() {
		nullScore = 0;
		minusScore = 0;
		noValidPlanScore = 0;
	}
	
	public double calculateLogsum(Population pop) {
		double logsum = 0.0;

		for(Person person : pop.getPersons().values()){
			double logsumOfPerson = calculateLogsumOfPerson(person);
			logsum += logsumOfPerson;
		}
		return logsum;
	}

	public double calculateLogsumOfPerson(Person person) {
		double logsumOfPerson = 0.0;
		double sumOfExpScore = 0.0;

		for(Plan plan : person.getPlans()){
			if(plan.getScore() == null){
				nullScore++;
				if(nullScore <= maxWarnCnt) {
					logger.warn("Score for person " + person.getId() + " is " + plan.getScore() 
						+ ". The score cannot be used for utility calculation.");
						if(nullScore == maxWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED + "\n");
				}
			} else if(plan.getScore() <= 0.0){
				minusScore++;
				if(minusScore <= maxWarnCnt) {
					logger.warn("Score for person " + person.getId() + " is " + plan.getScore() 
							+ ". The score cannot be used for utility calculation.");
						if(minusScore == maxWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED + "\n");
				}
			} else{
				double expScoreOfPlan = Math.exp(betaLogit * plan.getScore());
				sumOfExpScore += expScoreOfPlan;
			}
		}
		if(sumOfExpScore == 0.0){
			noValidPlanScore++;
			if(noValidPlanScore <= maxWarnCnt) {
				logger.warn("Person " + person.getId() + " has no valid plans. " +
						"This person is not considered for utility calculations.");
					if(noValidPlanScore == maxWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED + "\n");
			}
		} else{
			logsumOfPerson = (1. / (betaLogit * marginalUtlOfMoney)) * Math.log(sumOfExpScore);
		}
		return logsumOfPerson;
	}

	public int getNoValidPlanCnt() {
		return noValidPlanScore;
	}

}

//TODO: what is the difference to Kai's version???
//
//private static double logsum(Person person) {
//	double bestScore = bestScore(person) ;
//	
//	double beta = 2.0 ;
//	double sum = 0. ;
//	for ( Plan plan : person.getPlans() ) {
//		sum += Math.exp( beta*(plan.getScore()-bestScore) ) ;
//	}
//	return bestScore + (1./beta) * Math.log( sum ) ;
//}
//
//private static double bestScore(Person person) {
//	double bestScore = Double.NEGATIVE_INFINITY ;
//	for ( Plan plan : person.getPlans() ) {
//		if ( plan.getScore() > bestScore ) {
//			bestScore = plan.getScore() ;
//		}
//	}
//	return bestScore ;
//}