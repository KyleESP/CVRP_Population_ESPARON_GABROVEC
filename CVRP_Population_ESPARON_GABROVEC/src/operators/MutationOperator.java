package operators;

import java.util.ArrayList;
import java.util.Collections;

import cvrp_population.Genetic;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class MutationOperator {
	
	private Genetic gen;
	
	public MutationOperator(Genetic gen) {
		this.gen = gen;
	}
	
	public ArrayList<Vehicle> getInversionMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = gen.getRand().nextInt(locations.size());
		int b;
		do {
			b = gen.getRand().nextInt(locations.size());
		} while (b == a);
		Collections.reverse(a < b ? locations.subList(a, b) : locations.subList(b, a));
		ArrayList<Vehicle> reconstruction = gen.reconstruct(locations);
		return reconstruction;
	}
}
