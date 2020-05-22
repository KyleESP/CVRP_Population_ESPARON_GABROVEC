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
	
	public ArrayList<Vehicle> inversionMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = gen.getRand().nextInt(locations.size());
		int b = gen.getRand().nextInt(locations.size());
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		Collections.reverse(locations.subList(min, max));
		
		ArrayList<Vehicle> reconstruction = gen.reconstruct(locations);
		return reconstruction;
	}
	
	public ArrayList<Vehicle> displacementMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = gen.getRand().nextInt(locations.size());
		int b;
		do {
			b = gen.getRand().nextInt(locations.size());
		} while (Math.abs(a - b) == locations.size() - 1);
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		ArrayList<Location> subList = new ArrayList<>();
		for (int i = min; i < max; i++) {
			subList.add(locations.get(i));
		}
		locations.removeAll(subList);
		int newPosition = gen.getRand().nextInt(locations.size());
		locations.addAll(newPosition, subList);
		ArrayList<Vehicle> reconstruction = gen.reconstruct(locations);
		return reconstruction;
	}
}
