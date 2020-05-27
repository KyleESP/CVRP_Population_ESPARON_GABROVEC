package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import operators.CrossoverOperator;
import operators.MutationOperator;
import operators.SelectionOperator;

public class GeneticAlgorithm {
	
	private CrossoverOperator crossoverOperator;
	private MutationOperator mutationOperator;
	private SelectionOperator selectionOperator;
	private long nbGenerations;
	private int nbIndividuals;
	private double pMutation;
	private int maxCapacity;
	private double diffRate;
	private ArrayList<Location> locations;
	private ArrayList<ArrayList<Vehicle>> population;
    private ArrayList<Vehicle> bestIndividual;
    private double bestCost;
    private ArrayList<Object[]> bestCostsHistory;
	private Random rand;
	
	public GeneticAlgorithm(ArrayList<Location> locations, int maxCapacity, long nbGenerations, int nbIndividuals, double pMutation, double diffRate) {
    	this.maxCapacity = maxCapacity;
    	this.nbGenerations = nbGenerations;
    	this.nbIndividuals = nbIndividuals;
    	this.pMutation = pMutation;
    	this.diffRate = diffRate;
    	population = new ArrayList<>(nbIndividuals);
    	this.locations = locations;
    	rand = new Random();
    	selectionOperator = new SelectionOperator(this);
    	mutationOperator = new MutationOperator(this);
    	crossoverOperator = new CrossoverOperator(this);
    	bestCostsHistory = new ArrayList<>();
		initPopulation();
	}
	
    public void exec() {
		bestCost = Double.POSITIVE_INFINITY;
		updateBestIndividual(0);
		displayDescription();
		int percentage = -1, newPercentage;
		ArrayList<Vehicle> parent1, parent2, child;
		ArrayList<Vehicle> parentMutation, mutant;
		for (int i = 0; i < nbGenerations; i++) {
			parent1 = selectionOperator.tournament(3);
			parent2 = selectionOperator.tournament(3);
			child = crossoverOperator.hGreXCrossover(parent1, parent2);
			setSimilarIndividual(child);
			if (rand.nextDouble() < pMutation && (parentMutation = getRandomButNotBest()) != null) {
				mutant = rand.nextDouble() < 0.5 ? mutationOperator.inversionMutation(parentMutation) : mutationOperator.displacementMutation(parentMutation);
				population.remove(parentMutation);
				population.add(mutant);
			}
			updateBestIndividual(i + 1);
			if ((newPercentage = (int)(((double)(i + 1) / nbGenerations) * 100)) != percentage) {
				percentage = newPercentage;
				System.out.println(percentage + "%");
			}
		}
	    displayIndividual(bestIndividual);
    }
	
	private ArrayList<Vehicle> getRandomButNotBest() {
		ArrayList<ArrayList<Vehicle>> notBests = new ArrayList<>();
		for (ArrayList<Vehicle> vehicles : population) {
			if (objectiveFunction(vehicles) != bestCost) {
				notBests.add(vehicles);
			}
		}
		ArrayList<Vehicle> randomIndividual = null;
		if (!notBests.isEmpty()) {
			randomIndividual = notBests.get(rand.nextInt(notBests.size()));
		}
		return randomIndividual;
	}
	
	private void setSimilarIndividual(ArrayList<Vehicle> individual) {
		double indCost = objectiveFunction(individual), currIndCost;
		boolean hasSimilar = false;
		if (areSimilar(bestCost, indCost, diffRate)) {
			for (ArrayList<Vehicle> currInd : population) {
				if (areSimilar(bestCost, (currIndCost = objectiveFunction(currInd)), diffRate)) {
					hasSimilar = true;
					if (indCost < currIndCost) {
						population.remove(currInd);
						population.add(individual);
						break;
					}
				}
			}
		}
		if (!hasSimilar) {
			ArrayList<Vehicle> badIndividual = selectionOperator.tournament(2);
			population.remove(badIndividual);
			population.add(individual);
		}
	}
	
	public boolean areSimilar(double a, double b, double perc) {
		return Math.abs(a - b) / Math.min(a, b) <= perc;
	}
	
	private void initPopulation() {
		for (int i = 0; i < nbIndividuals; i++) {
			ArrayList<Vehicle> vehicles = new ArrayList<>();
			population.add(vehicles);
	        int vIdx = 0;
	        Vehicle v;
	        ArrayList<Location> locationsCopy = Util.createDeepCopyLocations(locations);
	        Location depot = Util.getLocationById(0, locationsCopy);
	        Collections.shuffle(locationsCopy);
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
		boolean hasUnroutedLocation = false;
        for(Location l : locations) {
            if (!l.getIsRouted()) {
            	hasUnroutedLocation = true;
            	break;
            }
        }
        return hasUnroutedLocation;
    }
	
	private void updateBestIndividual(int i) {
		double fMin = Double.POSITIVE_INFINITY, fCurr;
		ArrayList<Vehicle> xMin = null;
		for (ArrayList<Vehicle> vehicles : population) {
			if ((fCurr = objectiveFunction(vehicles)) < fMin) {
				fMin = fCurr;
				xMin = vehicles;
			}
		}
		if (fMin < bestCost) {
			bestCost = fMin;
			bestIndividual = xMin;
			bestCostsHistory.add(new Object[] {i, bestCost});
		} else if (i == nbGenerations) {
			bestCostsHistory.add(new Object[] {i, bestCost});
		}
	}
	
	public <T> ArrayList<Vehicle> reconstruct(ArrayList<T> reconstructibleLocations) {
		ArrayList<Vehicle> reconstructedLocations = new ArrayList<>();
		Location depot = Util.getLocationById(0, locations), l;
		Vehicle v = new Vehicle(maxCapacity);
		v.routeLocation(depot);
		for (int i = 0; i < reconstructibleLocations.size(); i++) {
			l = (reconstructibleLocations.get(i) instanceof Integer) ? Util.getLocationById((Integer)reconstructibleLocations.get(i), locations) : (Location)reconstructibleLocations.get(i);
			if (!v.routeLocation(l)) {
				v.routeLocation(depot);
				reconstructedLocations.add(v);
				v = new Vehicle(maxCapacity);
				v.routeLocation(depot);
				v.routeLocation(l);
			}
		}
		v.routeLocation(depot);
		reconstructedLocations.add(v);
		return reconstructedLocations;
	}
	
	public double objectiveFunction(ArrayList<Vehicle> vehicles) {
		double sumDist = 0, sumVehicles = 0;
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
	
	public void displayDescription() {
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("Algorithme génétique :");
		String description = "Coût initial = " + (double) Math.round(bestCost * 1000) / 1000;
		description += "\nNombre de générations = " + nbGenerations;
		description += "\nNombre d'individus = " + nbIndividuals;
		description += "\nProbabilité de mutation = " + pMutation;
		System.out.println(description);
		System.out.println("----------------------------------------------------------------------------------------------------");
	}
	
	public String getInlineDescription() {
		String description = "Coût final = " + (double) Math.round(bestCost * 1000) / 1000;
		description += " | Nb véhicules = " + bestIndividual.size() + " | ";
		description += " | Nombre d'individus = " +  nbIndividuals;
		description += " | Nombre de générations = " + nbGenerations;
		description += " | Probabilité de mutation = " + pMutation;
		return description;
	}
	
    public void displayIndividual(ArrayList<Vehicle> vehicles) {
    	System.out.println("----------------------------------------------------------------------------------------------------");
        for (int i = 0 ; i < vehicles.size() ; i++) {
            System.out.println("Véhicule n°" + (i + 1) + " : " + getRouteString(vehicles.get(i).getRoute()));
        }
        System.out.println("\nCoût de la solution : " + objectiveFunction(vehicles));
    	System.out.println("----------------------------------------------------------------------------------------------------");
    }
    
    public String getRouteString(ArrayList<Location> route) {
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
    
    public ArrayList<Vehicle> getBestIndividual() {
    	return bestIndividual;
    }
    
    public double getBestCost() {
    	return bestCost;
    }
    
    public ArrayList<Object[]> getBestCostsHistory() {
    	return bestCostsHistory;
    }
    
    public Random getRand() {
    	return rand;
    }
    
    public int getMaxCapacity() {
    	return maxCapacity;
    }
    
    public ArrayList<Location> getLocations() {
    	return locations;
    }
    
    public int getNbIndividuals() {
    	return nbIndividuals;
    }
}
