package cvrp_population;

import java.util.ArrayList;
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
			population.add(new ArrayList<>());
		}
		for (ArrayList<Vehicle> vehicles : population) {
	        int vIdx = 0;
	        Vehicle v;
	        ArrayList<Location> locationsCopy = Util.createDeepCopyLocations(locations);
	        Location depot = locationsCopy.get(0);
	        while (hasAnUnroutedLocation(locationsCopy)) {
	        	if (vIdx >= vehicles.size()) {
	        		Vehicle newV = new Vehicle(maxCapacity);
	        		newV.routeLocation(depot);
	        		vehicles.add(vIdx, newV);
	        	}
	        	v = vehicles.get(vIdx);
	            Location choseLocation = null;
	            double minCost = Double.POSITIVE_INFINITY;
	            int currentLocationId = v.getCurrentLocationId();
	            double dist;
	            for(Location l : locationsCopy) {
	            	if((currentLocationId != l.getId()) && !l.getIsRouted() && v.fits(l.getNbOrders()) 
	            			&& minCost > (dist = Util.getDistances().get(currentLocationId).get(l.getId()))) {
						minCost = dist;
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
		String description = "Coût final = " + bestCost;
		description += " | Nb véhicules = " + bestVehicles.size() + " | ";
		description += " | Nombre d'individus = " +  nbIndividuals;
		description += " | Nombre de générations = " + nbGenerations;
		description += " | Probabilité de croisement = " + pCross;
		return description;
	}
    
    protected ArrayList<ArrayList<Vehicle>> getNeighbors(ArrayList<Vehicle> vehicles) {
		ArrayList<ArrayList<Vehicle>> neighbors = new ArrayList<>();
		ArrayList<Location> routeFrom, routeTo;
		int routeFromSize, routeToSize;
		boolean isSameRoute;
		int totalLoading;
		for (int vFromIdx = 0; vFromIdx < vehicles.size(); vFromIdx++) {
			routeFrom = vehicles.get(vFromIdx).getRoute();
			routeFromSize = routeFrom.size();
			for (int vToIdx = vFromIdx; vToIdx < vehicles.size(); vToIdx++) {
				routeTo = vehicles.get(vToIdx).getRoute();
				routeToSize = routeTo.size();
				isSameRoute = (vFromIdx == vToIdx);
				if (!isSameRoute) {
					totalLoading = vehicles.get(vFromIdx).getCurrentLoading() + vehicles.get(vToIdx).getCurrentLoading();
					if (totalLoading > (maxCapacity * 2)) {
						continue;
					} else if (totalLoading <= maxCapacity) {
						neighbors.add(deleteOneRoute(vehicles, routeFrom, routeTo, vFromIdx, vToIdx));
					}
				}
				for (int locFromIdx = 1; locFromIdx < routeFromSize - (isSameRoute ? 2 : 1); locFromIdx++) {
					for (int locToIdx = (isSameRoute ? (locFromIdx + 1) : (locFromIdx == routeFromSize - 2 ? 1 : 0)); locToIdx < routeToSize - ((isSameRoute && locFromIdx == 1 || !isSameRoute && locFromIdx == routeFromSize - 2) ? 2 : 1); locToIdx++) {
						ArrayList<Vehicle> newVehicles = isSameRoute ? swapTwoOpt(vehicles, vFromIdx, locFromIdx, locToIdx) : swapRoutes(vehicles, routeFrom, routeTo, vFromIdx, vToIdx, locFromIdx, locToIdx);
						if (newVehicles != null) {
							neighbors.add(newVehicles);
						}
					}
				}
			}
		}
		return neighbors;
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
	
	protected double objectiveFunction(ArrayList<Vehicle> vehicles) {
    	double sumDist = 0;
    	int sumVehicles = 0;
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
    
    protected String getRouteString(ArrayList<Location> route) {
    	int routeSize = route.size();
    	String routeString = "";
    	for (int i = 0; i < routeSize ; i++) {
        	routeString += "(" + route.get(i).getId() + ")" + ((i != routeSize - 1) ? " == " : "");
        }
    	return routeString;
    }
}
