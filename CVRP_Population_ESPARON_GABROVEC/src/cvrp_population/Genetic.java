package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Genetic {
	
	private long nbGenerations;
	private int nbIndividuals;
	private int nbBest;
	private double pCross;
	private int maxCapacity;
    private int nbVehicles;
	private ArrayList<Location> locations;
	private ArrayList<ArrayList<Vehicle>> population;
    private ArrayList<Vehicle> bestVehicles;
    private double bestCost;
    private ArrayList<Double> costsHistory;
	private Random rand;
	
	public Genetic(ArrayList<Location> locations, int nbVehicles, int maxCapacity, long nbGenerations, int nbIndividuals, int nbBest, double pCross) {
		this.nbVehicles = nbVehicles;
    	this.maxCapacity = maxCapacity;
    	this.nbGenerations = nbGenerations;
    	this.nbIndividuals = nbIndividuals;
    	this.nbBest = nbBest;
    	this.pCross = pCross;
    	population = new ArrayList<ArrayList<Vehicle>>(nbIndividuals);
    	this.locations = Util.createDeepCopyLocations(locations);
    	rand = new Random();
    	costsHistory = new ArrayList<>();
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
		bestCost = Double.POSITIVE_INFINITY;
		updateBestSolution();
		//displayDescription();
		for (int i = 0; i < nbGenerations; i++) {
			ArrayList<ArrayList<Vehicle>> reproductedPopulation = rouletteWheelReproduction();
			bestSolutionsReproduction();
			for (int j = nbBest + 1; j < nbIndividuals; j++) {
				/*ArrayList<ArrayList<Vehicle>> newPopulation = (rand.nextDouble() < pCross) ? crossover(reproductedPopulation) : mutation(reproductedPopulation);
				population.addAll(newPopulation);*/
			}
			updateBestSolution();
		}
	    //displayBestSolution();
    }
	
	private ArrayList<ArrayList<Vehicle>> crossover(ArrayList<ArrayList<Vehicle>> population) {
		return null;
	}
	
	private ArrayList<ArrayList<Vehicle>> mutation(ArrayList<ArrayList<Vehicle>> population) {
		return null;
	}
	
	private ArrayList<ArrayList<Vehicle>> rouletteWheelReproduction() {
		ArrayList<Double> costs = new ArrayList<>();
		double total = 0;
		for (ArrayList<Vehicle> individual : population) {
			double cost = objectiveFunction(individual);
			costs.add(cost);
			total += cost;
		}
		ArrayList<double[]> rouletteWheel = new ArrayList<double[]>();
		double p = 0;
		for (int i = 0; i < costs.size() - 1; i++) {
			double[] interval = new double[2];
			interval[0] = p;
			p += costs.get(i + 1) / total;
			interval[1] = p;
			rouletteWheel.add(interval);
		}
		rouletteWheel.add(new double[] {p, 1d});
		
		ArrayList<ArrayList<Vehicle>> nextPopulation = new ArrayList<>();
		// Launch wheel nbindividuals times to create the nextPopulation
		for (int i = 0; i < nbIndividuals; i++) {
			p = rand.nextDouble();
			nextPopulation.add(launchWheel(p, rouletteWheel));
		}
		return nextPopulation;
	}
	
	private ArrayList<Vehicle> launchWheel(Double p, ArrayList<double[]> rouletteWheel) {
		double[] interval;
		ArrayList<Vehicle> winner = null;
		for (int j = 0; j < rouletteWheel.size(); j++) {
			interval = rouletteWheel.get(j);
			if (p >= interval[0] && p < interval[1]) {
				winner = population.get(j);
				break;
			}
		}
		return winner;
	}
	
	private void bestSolutionsReproduction() {
		population.sort((idv1, idv2) -> Double.compare(objectiveFunction(idv1), objectiveFunction(idv2)));
		ArrayList<ArrayList<Vehicle>> tmpPopulation = new ArrayList<>();
		for (int i = 0; i < nbBest; i++) {
			tmpPopulation.add(population.get(i));
		}
		population = tmpPopulation;
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
    }
    
	private boolean hasAnUnroutedLocation(ArrayList<Location> locations) {
        for(Location l : locations) {
            if (!l.getIsRouted()) {
            	return true;
            }
        }
        return false;
    }
	
	private void updateBestSolution() {
		double fMin = Double.POSITIVE_INFINITY;
		ArrayList<Vehicle> xMin = null;
		double fCurr;
		for (ArrayList<Vehicle> vehicles : population) {
			if ((fCurr = objectiveFunction(vehicles)) < fMin) {
				fMin = fCurr;
				xMin = vehicles;
			}
		}
		if (fMin < bestCost) {
			bestCost = fMin;
			bestVehicles = xMin;
		}
    	costsHistory.add(fMin);
	}
	
	public void displayDescription() {
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("Algorithme génétique :");
		String description = "Coût initial = " + (double) Math.round(bestCost * 1000) / 1000;
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
    
    public void displaySolution(ArrayList<Vehicle> vehicles) {
    	double cost = objectiveFunction(vehicles);
    	System.out.println("----------------------------------------------------------------------------------------------------");
        for (int i = 0 ; i < vehicles.size() ; i++) {
            System.out.println("Véhicule n°" + (i + 1) + " : " + getRouteString(vehicles.get(i).getRoute()));
        }
        System.out.println("\nCoût de la solution : " + cost);
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
    
    public void displayPopulation(ArrayList<ArrayList<Vehicle>> population) {
    	for (int i = 0; i < population.size(); i++) {
    		System.out.println("Individu n°" + (i + 1));
    		displaySolution(population.get(i));
    		System.out.println();
    	}
    }
    
    public ArrayList<ArrayList<Vehicle>> getPopulation() {
    	return population;
    }
}
