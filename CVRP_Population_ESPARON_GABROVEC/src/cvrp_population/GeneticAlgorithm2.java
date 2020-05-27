package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import operators.CrossoverOperator;
import operators.MutationOperator;
import operators.SelectionOperator;

public class GeneticAlgorithm2 {
	
	private CrossoverOperator crossoverOperator;
	private MutationOperator mutationOperator;
	private SelectionOperator selectionOperator;
	private long nbGenerations;
	private int nbIndividuals;
	private int nbBest;
	private double pMutation;
	private int maxCapacity;
	private ArrayList<Location> locations;
	private ArrayList<ArrayList<Vehicle>> population;
    private ArrayList<Vehicle> bestIndividual;
    private double bestCost;
    private ArrayList<Object[]> bestCostsHistory;
	private Random rand;
	
	public GeneticAlgorithm2(ArrayList<Location> locations, int maxCapacity, long nbGenerations, int nbIndividuals, int nbBest, double pMutation) {
    	this.maxCapacity = maxCapacity;
    	this.nbGenerations = nbGenerations;
    	this.nbIndividuals = nbIndividuals;
    	this.nbBest = nbBest;
    	this.pMutation = pMutation;
    	population = new ArrayList<>(nbIndividuals);
    	this.locations = locations;
    	rand = new Random();
    	/*selectionOperator = new SelectionOperator(this);
    	mutationOperator = new MutationOperator(this);
    	crossoverOperator = new CrossoverOperator(this);*/
    	bestCostsHistory = new ArrayList<>();
		initPopulation();
	}
    
    public void exec() {
		bestCost = Double.POSITIVE_INFINITY;
		updateBestIndividual(0);
		displayDescription();
		int percentage = -1, newPercentage;
		ArrayList<Vehicle> p1, p2;
		ArrayList<ArrayList<Vehicle>> reproductedPopulation, childs;
		for (int i = 0; i < nbGenerations; i++) {
			reproductedPopulation = selectionOperator.rouletteWheel();
			bestSolutionsReproduction();
			while (population.size() < nbIndividuals) {
				p1 = reproductedPopulation.get(rand.nextInt(reproductedPopulation.size()));
				if (rand.nextDouble() < pMutation) {
					population.add(rand.nextDouble() < 0.5 ? mutationOperator.displacementMutation(p1) : mutationOperator.inversionMutation(p1));
				} else {
					p2 = reproductedPopulation.get(rand.nextInt(reproductedPopulation.size()));
					childs = crossoverOperator.oxCrossover(p1, p2);
					population.add(childs.get(0));
					if (population.size() < nbIndividuals) {
						population.add(childs.get(1));
					}
				}
			}
			updateBestIndividual(i + 1);
			if ((newPercentage = (int)(((double)(i + 1) / nbGenerations) * 100)) != percentage) {
				percentage = newPercentage;
				System.out.println(percentage + "%");
			}
		}
		displaySolution(bestIndividual);
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
		ArrayList<Location> locationsCopy = Util.createDeepCopyLocations(locations);
		locationsCopy.remove(Util.getLocationById(0, locationsCopy));
		for (int i = 0; i < nbIndividuals; i++) {
	        Collections.shuffle(locationsCopy);
	        population.add(reconstruct(locationsCopy));
		}
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
	
    public void displaySolution(ArrayList<Vehicle> vehicles) {
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
