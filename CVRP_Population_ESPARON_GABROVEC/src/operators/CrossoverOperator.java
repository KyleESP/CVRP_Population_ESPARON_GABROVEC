package operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cvrp_population.Genetic;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class CrossoverOperator {
	
	private Genetic gen;
	
	public CrossoverOperator(Genetic gen) {
		this.gen = gen;
	}
	
	public ArrayList<Vehicle> hGreXCrossover(ArrayList<Vehicle> p1, ArrayList<Vehicle> p2) {
		ArrayList<Location> p1Locations = Util.getLocations(p1);
		HashMap<int[], Double> pCosts = getEdgesCosts(p1Locations);
		ArrayList<Location> p2Locations = Util.getLocations(p2);
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
	
	private HashMap<int[], Double> getEdgesCosts(ArrayList<Location> locations) {
		HashMap<int[], Double> edgesCosts = new HashMap<>();
		HashMap<Integer, HashMap<Integer, Double>> distances = Util.getDistances();
		int idSource, idDest;
		double distance;
		int[] edge;
		for (int i = 0; i < locations.size() - 1; i++) {
			idSource = locations.get(i).getId();
			idDest = locations.get(i + 1).getId();
			edge = new int[] {idSource, idDest};
			distance = distances.get(idSource).get(idDest);
			edgesCosts.put(edge, distance);
		}
		return edgesCosts;
	}
	
	private ArrayList<Vehicle> reconstructWithIds(ArrayList<Integer> brokenLocations) {
		ArrayList<Vehicle> newChild = new ArrayList<>();
		Vehicle v = new Vehicle(gen.getMaxCapacity());
		Location depot = Util.getLocationById(0, gen.getLocations());
		v.routeLocation(depot);
		for (int i = 0; i < brokenLocations.size(); i++) {
			if (!v.routeLocation(Util.getLocationById(brokenLocations.get(i), gen.getLocations()))) {
				v.routeLocation(depot);
				newChild.add(v);
				v = new Vehicle(gen.getMaxCapacity());
				v.routeLocation(depot);
				v.routeLocation(Util.getLocationById(brokenLocations.get(i), gen.getLocations()));
			}
		}
		v.routeLocation(depot);
		newChild.add(v);
		return newChild;
	}
}
