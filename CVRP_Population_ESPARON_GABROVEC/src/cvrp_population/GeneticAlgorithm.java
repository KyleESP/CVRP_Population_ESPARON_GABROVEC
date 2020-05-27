package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

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
    private TreeMap<Integer, Double> bestCostsHistory;
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
    	bestCostsHistory = new TreeMap<>();
	}
	
    public void exec() {
    	initPopulation();
		displayDescription();
		int percentage = -1, newPercentage;
		ArrayList<Vehicle> parent1, parent2, child;
		ArrayList<Vehicle> parentMutation, mutant;
		for (int i = 1; i <= nbGenerations; i++) {
			parent1 = selectionOperator.tournament(3);
			parent2 = selectionOperator.tournament(3);
			child = crossoverOperator.hGreXCrossover(parent1, parent2);
			setSimilarIndividual(child, i);
			if (rand.nextDouble() < pMutation && (parentMutation = getRandomButNotBest()) != null) {
				mutant = rand.nextDouble() < 0.5 ? mutationOperator.inversionMutation(parentMutation) : mutationOperator.displacementMutation(parentMutation);
				population.remove(parentMutation);
				population.add(mutant);
				updateBestIndividual(mutant, objectiveFunction(mutant), i);
			}
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
	
	private void setSimilarIndividual(ArrayList<Vehicle> individual, int i) {
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
			population.remove(getRandomButNotBest());
			population.add(individual);
		}
		updateBestIndividual(individual, indCost, i);
	}
	
	private boolean areSimilar(double a, double b, double perc) {
		return Math.abs(a - b) / Math.min(a, b) <= perc;
	}
	
	private void initPopulation() {
		double minCost = Double.POSITIVE_INFINITY, currCost;
		ArrayList<Vehicle> minInd = null;
		ArrayList<Location> locationsCopy = Util.createDeepCopyLocations(locations);
		locationsCopy.remove(Util.getLocationById(0, locationsCopy));
		for (int i = 0; i < nbIndividuals; i++) {
	        Collections.shuffle(locationsCopy);
	        ArrayList<Vehicle> individual = reconstruct(locationsCopy);
	        population.add(individual);
	        if ((currCost = objectiveFunction(individual)) < minCost) {
	        	minCost = currCost;
	        	minInd = individual;
	        }
		}
		bestCost = minCost;
		bestIndividual = minInd;
		bestCostsHistory.put(0, bestCost);
    }
	
	private void updateBestIndividual(ArrayList<Vehicle> individual, double cost, int i) {
		if (cost < bestCost) {
			bestCost = cost;
			bestIndividual = individual;
			bestCostsHistory.put(i, bestCost);
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
    
    public TreeMap<Integer, Double> getBestCostsHistory() {
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
