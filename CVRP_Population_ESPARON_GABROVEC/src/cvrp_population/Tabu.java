package cvrp_population;

import java.util.ArrayList;
import java.util.Arrays;

public class Tabu extends Method {
	
	private long maxIteration;
	private int maxTabuListSize;
	private ArrayList<Vehicle[]> tabuList;
	
	public Tabu(ArrayList<Location> locations, int nbVehicles, int maxCapacity, long maxIteration, int maxTabuListSize) {
		super(locations, nbVehicles, maxCapacity);
		this.maxIteration = maxIteration;
		this.maxTabuListSize = maxTabuListSize;
		this.tabuList = new ArrayList<>();
	}
	
	@Override
	public void exec() {
		displayDescription();
		tabuList.clear();
		double percentage, deltaF, fX;
        for (int i = 0; i < maxIteration; i++) {
        	ArrayList<Vehicle[]> V = getNeighbors();
        	fX = cost;
         	getBestNeighbor(V); // xi <- xi+1
        	deltaF = cost - fX;
        	if (deltaF >= 0) {
        		if (tabuList.size() == maxTabuListSize) {
        			tabuList.remove(0);
        		}
        		tabuList.add(vehicles);
        	}
        	if (cost < bestCost) {
        		bestVehicles = vehicles;
        		bestCost = cost;
        	}
            percentage = ((double)(i + 1) / maxIteration) * 100;
        	if(percentage % 1 == 0) {
        		System.out.println((int)percentage + "%");
        	}
        }
        displayBestSolution();
    }
	
	@Override
	public void displayDescription() {
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("Recherche Tabou :");
		String description = "Coût initial = " + cost;
		description += "\nMaximum d'itération = " + maxIteration;
		description += "\nTaille maximum de la liste = " + maxTabuListSize;
		System.out.println(description);
		System.out.println("----------------------------------------------------------------------------------------------------");
	}
	
	@Override
	public String getInlineDescription() {
		String description = "Coût final = " + bestCost;
		description += " | Nb véhicules = " + getActiveVehicles().size();
		description += " | Maximum d'itération = " + maxIteration;
		description += " | Taille maximum de la liste = " + maxTabuListSize;
		return description;
	}
	
	private void getBestNeighbor(ArrayList<Vehicle[]> neighbors) {
		Vehicle[] xMin = null;
		double fMin = Double.POSITIVE_INFINITY;
		double costNeighbor;
		for (Vehicle[] neighbor : neighbors) {
			if (!tabuListContainsSolution(neighbor)) {
				costNeighbor = objectiveFunction(neighbor);
				if(costNeighbor < fMin) {
					xMin = neighbor;
					fMin = costNeighbor;
				}
			}
		}
		if (xMin == null) {
			System.err.println("La taille maximum de la liste est trop grande. La liste contient tous les voisins actuels.");
			System.exit(0);
		}
		vehicles = xMin;
		cost =  fMin;
		costsHistory.add(cost);
	}
	
	private boolean tabuListContainsSolution(Vehicle[] vehicles) {
		boolean contains = false;
		for (Vehicle[] currVehicles : tabuList) {
			if (sameVehiclesSize(currVehicles, vehicles) && Arrays.equals(currVehicles, vehicles)) {
				contains = true;
				break;
			}
		}
		return contains;
	}
	
	private boolean sameVehiclesSize(Vehicle[] vehicles1, Vehicle[] vehicles2) {
		if (vehicles1.length != vehicles2.length) {
			return false;
		}
		boolean sameVehiclesSize = true;
		for (int i = 0; i < vehicles1.length; i++) {
			if (vehicles1[i].getRoute().size() != vehicles2[i].getRoute().size()) {
				sameVehiclesSize = false;
				break;
			}
		}
		return sameVehiclesSize;
	}
	
	public void setMaxIteration(long maxIteration) {
		this.maxIteration = maxIteration;
	}

	public void setMaxTabuListSize(int maxTabuListSize) {
		this.maxTabuListSize = maxTabuListSize;
	}
}
