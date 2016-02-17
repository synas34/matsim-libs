package org.matsim.contrib.carsharing.stations;

import java.util.ArrayList;

import org.matsim.api.core.v01.network.Link;

public class FreeFloatingStation extends AbstractCarSharingStation {

	
	private int numberOfVehicles;
	private ArrayList<String> vehicleIDs = new ArrayList<String>();
	
	public FreeFloatingStation(Link link, int numberOfVehicles, ArrayList<String> vehicleIDs) {
		super(link) ;
		this.numberOfVehicles = numberOfVehicles;
		this.vehicleIDs = vehicleIDs;
	}
	
	public int getNumberOfVehicles() {
		
		return numberOfVehicles;
	}
	
	public Link getLink() {
		
		return link;
	}
	
	public ArrayList<String> getIDs() {
		
		return vehicleIDs;
	}
	
}
