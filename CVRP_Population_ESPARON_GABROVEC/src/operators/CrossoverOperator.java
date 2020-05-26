package operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cvrp_population.GeneticAlgorithm2;
import cvrp_population.GeneticAlgorithm;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class CrossoverOperator {
	
	private GeneticAlgorithm gen;
	
	public CrossoverOperator(GeneticAlgorithm gen) {
		this.gen = gen;
	}
	
	public ArrayList<ArrayList<Vehicle>> oxCrossover(ArrayList<Vehicle> p1, ArrayList<Vehicle> p2) {
		ArrayList<Location> p1Locations = Util.getLocations(p1);
		ArrayList<Location> p2Locations = Util.getLocations(p2);
		int firstPoint = gen.getRand().nextInt(p1Locations.size() - 1);
		int secondPoint = gen.getRand().nextInt(p1Locations.size());
		int minPoint = Math.min(firstPoint, secondPoint);
		int maxPoint = Math.max(firstPoint, secondPoint);
		ArrayList<Location> child1 = new ArrayList<>();
		ArrayList<Location> child2 = new ArrayList<>();
		child1.addAll(p1Locations.subList(minPoint, maxPoint));
		child2.addAll(p2Locations.subList(minPoint, maxPoint));
		
		int currLocIdx = 0;
		Location currLocP1, currLocP2;
		for (int i = 0; i < p1Locations.size(); i++) {
			currLocIdx = (maxPoint + i) % p1Locations.size();
			currLocP1 = p1Locations.get(currLocIdx);
			currLocP2 = p2Locations.get(currLocIdx);
			if (!child1.contains(currLocP2)) {
				child1.add(currLocP2);
			}
			if (!child2.contains(currLocP1)) {
				child2.add(currLocP1);
			}
		}
		
		Collections.rotate(child1, minPoint);
		Collections.rotate(child2, minPoint);
		ArrayList<ArrayList<Vehicle>> childs = new ArrayList<>();
		childs.add(gen.reconstruct(child1));
		childs.add(gen.reconstruct(child2));
		
		return childs;
	}
	
	public ArrayList<Vehicle> hGreXCrossover(ArrayList<Vehicle> p1, ArrayList<Vehicle> p2) {
		ArrayList<Location> p1Locations = Util.getLocations(p1);
		HashMap<int[], Double> pCosts = getEdgesCosts(p1Locations);
		ArrayList<Location> p2Locations = Util.getLocations(p2);
		pCosts.putAll(getEdgesCosts(p2Locations));
		double distance, minCost;
		int[] minEdge, key;
		HashMap<Integer, HashMap<Integer, Double>> distances = Util.getDistances();
		
		ArrayList<Integer> child = new ArrayList<>();
		child.add(p1Locations.get(0).getId());
		int lastLocId = p1Locations.get(1).getId();
		child.add(lastLocId);
		
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
		
		return gen.reconstruct(child);
	}
	
	private HashMap<int[], Double> getEdgesCosts(ArrayList<Location> locations) {
		HashMap<int[], Double> edgesCosts = new HashMap<>();
		int idSource, idDest;
		double distance;
		for (int i = 0; i < locations.size() - 1; i++) {
			idSource = locations.get(i).getId();
			idDest = locations.get(i + 1).getId();
			distance = Util.getDistances().get(idSource).get(idDest);
			edgesCosts.put(new int[] {idSource, idDest}, distance);
		}
		return edgesCosts;
	}
}
