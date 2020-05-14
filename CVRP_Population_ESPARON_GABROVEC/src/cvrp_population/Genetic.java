package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Genetic {
	
	private long nbGenerations;
	private int nbIndividuals;
	private double pCross;
	private int maxCapacity;
    private int nbVehicles;
	private ArrayList<Location> locations;
	private ArrayList<ArrayList<Vehicle>> population;
	private double cost;
    private ArrayList<Vehicle> bestVehicles;
    private double bestCost;
    private ArrayList<Double> costsHistory;
	private Random rand;
	
	public Genetic(ArrayList<Location> locations, int nbVehicles, int maxCapacity, long nbGenerations, int nbIndividuals, double pCross) {
		this.nbVehicles = nbVehicles;
    	this.maxCapacity = maxCapacity;
    	this.nbGenerations = nbGenerations;
    	this.nbIndividuals = nbIndividuals;
    	this.pCross = pCross;
    	population = new ArrayList<ArrayList<Vehicle>>(nbIndividuals);
    	this.locations = Util.createDeepCopyLocations(locations);
    	rand = new Random();
		initPopulation();
	}
    
    public ArrayList<Vehicle> getBestVehicles() {
    	return bestVehicles;
    }
    
    public double getBestCost() {
    	return bestCost;
    }
    
    public ArrayList<Double> getCostsHistory() {
    	return costsHistory;
    }
	
	public void exec() {
		displayDescription();
	    displayBestSolution();
    }
	
	private void initPopulation() {
		for (int i = 0; i < nbIndividuals; i++) {
			ArrayList<Vehicle> vehicles = new ArrayList<>();
			population.add(vehicles);
	        int vIdx = 0;
	        Vehicle v;
	        ArrayList<Location> locationsCopy = Util.createDeepCopyLocations(locations);
	        Location depot = locationsCopy.get(0);
	        Collections.shuffle(locationsCopy, new Random(i));
	        while (hasAnUnroutedLocation(locationsCopy)) {
	        	if (vIdx >= vehicles.size()) {
	        		Vehicle newV = new Vehicle(maxCapacity);
	        		newV.routeLocation(depot);
	        		vehicles.add(vIdx, newV);
	        	}
	        	v = vehicles.get(vIdx);
	            Location choseLocation = null;
	            int currentLocationId = v.getCurrentLocationId();
	            for(Location l : locationsCopy) {
	            	if((currentLocationId != l.getId()) && !l.getIsRouted() && v.fits(l.getNbOrders())) {
	                    choseLocation = l;
	            	}
	            }
	            
	            if(choseLocation != null) {
	            	v.routeLocation(choseLocation);
	            } else {
	                v.routeLocation(depot);
	                vIdx++;
	            }
	        }
	        vehicles.get(vIdx).routeLocation(depot);
		}
		bestVehicles = getBestIndividual(population);
		bestCost = objectiveFunction(bestVehicles);
		cost = bestCost;
		costsHistory = new ArrayList<>();
    	costsHistory.add(cost);
    	//ArrayList<ArrayList<Vehicle>> otherIndividuals = getIndividuals(bestVehicles, nbIndividuals - 1);
    	
    }
    
	private boolean hasAnUnroutedLocation(ArrayList<Location> locations) {
        for(Location l : locations) {
            if (!l.getIsRouted()) {
            	return true;
            }
        }
        return false;
    }
	
	private ArrayList<Vehicle> getBestIndividual(ArrayList<ArrayList<Vehicle>> population) {
		double bestCost = Double.POSITIVE_INFINITY;
		ArrayList<Vehicle> bestIndividual = null;
		double fCurr;
		for (ArrayList<Vehicle> vehicles : population) {
			fCurr = objectiveFunction(vehicles);
			if (fCurr < bestCost) {
				bestCost = fCurr;
				bestIndividual = vehicles;
			}
		}
		return bestIndividual;
	}
	
	public void displayDescription() {
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("Algorithme génétique :");
		String description = "Coût initial = " + cost;
		description += "\nNombre de générations = " + nbGenerations;
		description += "\nNombre d'individus = " + nbIndividuals;
		description += "\nProbabilité de croisement = " + pCross;
		System.out.println(description);
		System.out.println("----------------------------------------------------------------------------------------------------");
	}
	
	public String getInlineDescription() {
		String description = "Coût final = " + (double) Math.round(bestCost * 1000) / 1000;
		description += " | Nb véhicules = " + bestVehicles.size() + " | ";
		description += " | Nombre d'individus = " +  nbIndividuals;
		description += " | Nombre de générations = " + nbGenerations;
		description += " | Probabilité de croisement = " + pCross;
		return description;
	}
    
	private ArrayList<Vehicle> swapRoutes(ArrayList<Vehicle> vehicles, ArrayList<Location> routeFrom, ArrayList<Location> routeTo, int vFromIdx, int vToIdx, int locFromIdx, int locToIdx) {
		Vehicle newVFrom = new Vehicle(maxCapacity);
		Vehicle newVTo = new Vehicle(maxCapacity);
		
		int i;
        for (i = 0; i <= locFromIdx; i++) {
        	newVFrom.routeLocation(routeFrom.get(i));
        }
        for (i = locToIdx + 1; i < routeTo.size(); i++) {
        	if (!newVFrom.routeLocation(routeTo.get(i))) {
        		return null;
        	}
        }
        
        for (i = 0; i <= locToIdx; i++) {
        	newVTo.routeLocation(routeTo.get(i));
        }
        for (i = locFromIdx + 1; i < routeFrom.size(); i++) {
        	if (!newVTo.routeLocation(routeFrom.get(i))) {
        		return null;
        	}
        }
        
        ArrayList<Vehicle> newVehicles = Util.createDeepCopyVehicles(vehicles);
        newVehicles.set(vFromIdx, newVFrom);
        newVehicles.set(vToIdx, newVTo);
		return newVehicles;
    }
    
    private ArrayList<Vehicle> deleteOneRoute(ArrayList<Vehicle> vehicles, ArrayList<Location> routeFrom, ArrayList<Location> routeTo, int vFromIdx, int vToIdx) {
    	ArrayList<Vehicle> newVehicles = Util.createDeepCopyVehicles(vehicles);
    	Vehicle newV = new Vehicle(maxCapacity);
		
		int i;
		for (i = 0; i < routeFrom.size() - 1; i++) {
			newV.routeLocation(routeFrom.get(i));
		}
		for (i = 1; i < routeTo.size(); i++) {
			newV.routeLocation(routeTo.get(i));
		}
		
		newVehicles.set(vFromIdx, newV);
		newVehicles.remove(vToIdx);
		
		return newVehicles;
    }
	
	private ArrayList<Vehicle> swapTwoOpt(ArrayList<Vehicle> vehicles, int vIdx, int locFromIdx, int locToIdx) {
		Vehicle newV = new Vehicle(maxCapacity);
		ArrayList<Location> route = vehicles.get(vIdx).getRoute();
		
		int i;
        for (i = 0; i <= locFromIdx - 1; i++) {
            newV.routeLocation(route.get(i));
        }
        int dcr = 0;
        for (i = locFromIdx; i <= locToIdx; i++) {
            newV.routeLocation(route.get(locToIdx - dcr));
            dcr++;
        }
        for (i = locToIdx + 1; i < route.size(); i++) {
        	newV.routeLocation(route.get(i));
        }
        
        ArrayList<Vehicle> newVehicles = Util.createDeepCopyVehicles(vehicles);
        newVehicles.set(vIdx, newV);
        return newVehicles;
    }
	
	private double objectiveFunction(ArrayList<Vehicle> vehicles) {
		double sumDist = 0;
    	double sumVehicles = 0;
    	ArrayList<Location> route;
    	for(Vehicle v : vehicles) {
    		route = v.getRoute();
			sumVehicles++;
			for (int i = 0; i < route.size() - 1; i++) {
    			sumDist += Util.getDistances().get(route.get(i).getId()).get(route.get(i + 1).getId());
    		}
    	}
    	return sumDist + sumVehicles;
    }
    
    public void displayBestSolution() {
    	System.out.println("----------------------------------------------------------------------------------------------------");
        for (int i = 0 ; i < bestVehicles.size() ; i++) {
            System.out.println("Véhicule n°" + (i + 1) + " : " + getRouteString(bestVehicles.get(i).getRoute()));
        }
        System.out.println("\nCoût de la solution : " + bestCost);
    	System.out.println("----------------------------------------------------------------------------------------------------");
    }
    
    private String getRouteString(ArrayList<Location> route) {
    	int routeSize = route.size();
    	String routeString = "";
    	for (int i = 0; i < routeSize ; i++) {
        	routeString += "(" + route.get(i).getId() + ")" + ((i != routeSize - 1) ? " == " : "");
        }
    	return routeString;
    }
    
    public ArrayList<ArrayList<Vehicle>> getPopulation() {
    	return population;
    }
}
