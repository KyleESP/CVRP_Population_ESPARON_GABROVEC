package cvrp_population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Genetic {
	
	private long nbGenerations;
	private int nbIndividuals;
	private double pMutation;
	private int maxCapacity;
	private ArrayList<Location> locations;
	private ArrayList<ArrayList<Vehicle>> population;
    private ArrayList<Vehicle> bestVehicles;
    private double bestCost;
    private ArrayList<Double> costsHistory;
	private Random rand;
	
	public Genetic(ArrayList<Location> locations, int nbVehicles, int maxCapacity, long nbGenerations, int nbIndividuals, double pMutation) {
    	this.maxCapacity = maxCapacity;
    	this.nbGenerations = nbGenerations;
    	this.nbIndividuals = nbIndividuals;
    	this.pMutation = pMutation;
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
		getInversionMutation(population.get(0));
		bestCost = Double.POSITIVE_INFINITY;
		updateBestSolution();
		//displayDescription();
		double percentage;
		for (int i = 0; i < nbGenerations; i++) {
			ArrayList<Vehicle> p1 = tournament(3);
			ArrayList<Vehicle> p2 = tournament(3);
			ArrayList<Vehicle> c = hGreXCrossover(p1, p2);
			setSimilarIndividual(c);
			if (rand.nextDouble() <= pMutation) {
				ArrayList<Vehicle> parent = getRandomButNotBest();
				ArrayList<Vehicle> mutant = getInversionMutation(parent);
				population.remove(parent);
				population.add(mutant);
			}
			updateBestSolution();
			percentage = ((double)(i + 1) / nbGenerations) * 100;
        	if(percentage % 1 == 0) {
        		System.out.println((int)percentage + "%");
        	}
		}
	    //displayBestSolution();
    }
	
	private ArrayList<Vehicle> getBestIndividual() {
		double fMin = Double.POSITIVE_INFINITY;
		ArrayList<Vehicle> xMin = null;
		double fCurr;
		for (ArrayList<Vehicle> vehicles : population) {
			if ((fCurr = objectiveFunction(vehicles)) < fMin) {
				fMin = fCurr;
				xMin = vehicles;
			}
		}
		return xMin;
	}
	
	private ArrayList<Vehicle> getInversionMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = getLocations(individual);
		int a = rand.nextInt(locations.size());
		int b;
		do {
			b = rand.nextInt(locations.size());
		} while (b == a);
		Collections.reverse(a < b ? locations.subList(a, b) : locations.subList(b, a));
		ArrayList<Vehicle> reconstruction = reconstruct(locations);
		return reconstruction;
	}
	
	private ArrayList<Vehicle> getRandomButNotBest() {
		ArrayList<Vehicle> bestIndividual = getBestIndividual();
		ArrayList<Vehicle> randomInd;
		do {
			randomInd = population.get(rand.nextInt(population.size()));
		} while(randomInd != bestIndividual);
		return randomInd;
	}
	
	private void setSimilarIndividual(ArrayList<Vehicle> individual) {
		double indCost = objectiveFunction(individual);
		double currIndCost;
		boolean indCostBetter;
		boolean hasSimilar = false;
		for (ArrayList<Vehicle> currInd : population) {
			currIndCost = objectiveFunction(currInd);
			indCostBetter = currIndCost < indCost;
			if (Math.abs(currIndCost - indCost) / (indCostBetter ? currIndCost : indCost) < 0.01) {
				if (indCostBetter) {
					population.remove(currInd);
					population.add(individual);
				}
				hasSimilar = true;
				break;
			}
		}
		if (!hasSimilar) {
			ArrayList<Vehicle> badIndividual = tournament(2);
			population.remove(badIndividual);
			population.add(individual);
		}
	}
	
	private ArrayList<Vehicle> tournament(int nbParticipants) {
		ArrayList<ArrayList<Vehicle>> participants = new ArrayList<>(nbParticipants);
		double totalCost = 0;
		ArrayList<Vehicle> participant;
		ArrayList<Double> costs = new ArrayList<>();
		double cost;
		for (int i = 0; i < nbParticipants; i++) {
			participant = population.get(rand.nextInt(population.size()));
			participants.add(participant);
			cost = objectiveFunction(participant);
			costs.add(cost);
			totalCost += cost;
		}
		ArrayList<double[]> probasMass = new ArrayList<double[]>();
		double p = 0;
		for (int i = 0; i < participants.size() - 1; i++) {
			double[] interval = new double[2];
			interval[0] = p;
			p += costs.get(i + 1) / totalCost;
			interval[1] = p;
			probasMass.add(interval);
		}
		probasMass.add(new double[] {p, 1d});
		p = rand.nextDouble();
		double[] interval;
		ArrayList<Vehicle> winner = null;
		for (int j = 0; j < probasMass.size(); j++) {
			interval = probasMass.get(j);
			if (p >= interval[0] && p < interval[1]) {
				winner = participants.get(j);
				break;
			}
		}
		return winner;
	}
	
	private ArrayList<Vehicle> hGreXCrossover(ArrayList<Vehicle> p1, ArrayList<Vehicle> p2) {
		ArrayList<Location> p1Locations = getLocations(p1);
		HashMap<int[], Double> pCosts = getEdgesCosts(p1Locations);
		ArrayList<Location> p2Locations = getLocations(p2);
		pCosts.putAll(getEdgesCosts(p2Locations));
		ArrayList<Integer> child = new ArrayList<>();
		child.add(p1Locations.get(0).getId());
		int lastLocId = p1Locations.get(1).getId();
		child.add(lastLocId);
		double distance;
		double minCost;
		int[] minEdge, key;
		HashMap<Integer, HashMap<Integer, Double>> distances = Util.getDistances();
		while (child.size() < p1Locations.size()) {
			minCost = Double.POSITIVE_INFINITY;
			minEdge = null;
			for (Map.Entry<int[], Double> entry : pCosts.entrySet()) {
				key = entry.getKey();
				if (key[0] == lastLocId && !child.contains(key[1]) && entry.getValue() < minCost) {
					minEdge = key;
					minCost = entry.getValue();
				}
	        }
			if (minEdge == null) {
				for (Location l : p1Locations) {
					if (!child.contains(l.getId()) && (distance = distances.get(lastLocId).get(l.getId())) < minCost) {
						minEdge = new int[] {lastLocId, l.getId()};
						minCost = distance;
					}
				}
			}
			lastLocId = minEdge[1];
			child.add(lastLocId);
		}
		
		ArrayList<Vehicle> newChild = reconstructWithIds(child);
		return newChild;
	}
	
	private ArrayList<Vehicle> reconstructWithIds(ArrayList<Integer> locations) {
		ArrayList<Vehicle> newChild = new ArrayList<>();
		Vehicle v = new Vehicle(maxCapacity);
		Location depot = getLocationById(0);
		v.routeLocation(depot);
		for (int i = 0; i < locations.size(); i++) {
			if (!v.routeLocation(getLocationById(locations.get(i)))) {
				v.routeLocation(depot);
				newChild.add(v);
				v = new Vehicle(maxCapacity);
				v.routeLocation(depot);
				v.routeLocation(getLocationById(locations.get(i)));
			} else if (i == locations.size() - 1) {
				v.routeLocation(depot);
				newChild.add(v);
			}
		}
		return newChild;
	}
	
	private ArrayList<Vehicle> reconstruct(ArrayList<Location> locations) {
		ArrayList<Vehicle> newChild = new ArrayList<>();
		Vehicle v = new Vehicle(maxCapacity);
		Location depot = getLocationById(0);
		v.routeLocation(depot);
		for (int i = 0; i < locations.size(); i++) {
			if (!v.routeLocation(locations.get(i))) {
				v.routeLocation(depot);
				newChild.add(v);
				v = new Vehicle(maxCapacity);
				v.routeLocation(depot);
				v.routeLocation(locations.get(i));
			} else if (i == locations.size() - 1) {
				v.routeLocation(depot);
				newChild.add(v);
			}
		}
		return newChild;
	}
	
	private Location getLocationById(int id) {
		for (Location l : locations) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}
	private HashMap<int[], Double> getEdgesCosts(ArrayList<Location> locations) {
		HashMap<int[], Double> edgesCosts = new HashMap<>();
		HashMap<Integer, HashMap<Integer, Double>> distances = Util.getDistances();
		int idSource, idDest;
		double distance;
		for (int i = 0; i < locations.size() - 1; i++) {
			idSource = locations.get(i).getId();
			idDest = locations.get(i + 1).getId();
			int[] edge = new int[] {idSource, idDest};
			distance = distances.get(idSource).get(idDest);
			edgesCosts.put(edge, distance);
		}
		return edgesCosts;
	}
	
	private ArrayList<Location> getLocations(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = new ArrayList<>();
		for (Vehicle v : individual) {
			for (Location l : v.getRoute()) {
				if (l.getId() != 0) {
					locations.add(l);
				}
			}
		}
		return locations;
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
		description += "\nProbabilité de croisement = " + pMutation;
		System.out.println(description);
		System.out.println("----------------------------------------------------------------------------------------------------");
	}
	
	public String getInlineDescription() {
		String description = "Coût final = " + (double) Math.round(bestCost * 1000) / 1000;
		description += " | Nb véhicules = " + bestVehicles.size() + " | ";
		description += " | Nombre d'individus = " +  nbIndividuals;
		description += " | Nombre de générations = " + nbGenerations;
		description += " | Probabilité de croisement = " + pMutation;
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
