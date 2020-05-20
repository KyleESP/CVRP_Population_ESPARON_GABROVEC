package operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class MutationOperator {
	
	private ArrayList<Location> locations;
	private int maxCapacity;
	
	public MutationOperator(ArrayList<Location> locations, int maxCapacity) {
		this.locations = locations;
		this.maxCapacity = maxCapacity;
	}
	
	public ArrayList<Vehicle> getInversionMutation(ArrayList<Vehicle> individual, Random rand) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = rand.nextInt(locations.size());
		int b;
		do {
			b = rand.nextInt(locations.size());
		} while (b == a);
		Collections.reverse(a < b ? locations.subList(a, b) : locations.subList(b, a));
		ArrayList<Vehicle> reconstruction = reconstruct(locations);
		return reconstruction;
	}
	
	private ArrayList<Vehicle> reconstruct(ArrayList<Location> brokenLocations) {
		ArrayList<Vehicle> newChild = new ArrayList<>();
		Vehicle v = new Vehicle(maxCapacity);
		Location depot = Util.getLocationById(0, locations);
		v.routeLocation(depot);
		for (int i = 0; i < brokenLocations.size(); i++) {
			if (!v.routeLocation(brokenLocations.get(i))) {
				v.routeLocation(depot);
				newChild.add(v);
				v = new Vehicle(maxCapacity);
				v.routeLocation(depot);
				v.routeLocation(brokenLocations.get(i));
			}
		}
		v.routeLocation(depot);
		newChild.add(v);
		return newChild;
	}
}
