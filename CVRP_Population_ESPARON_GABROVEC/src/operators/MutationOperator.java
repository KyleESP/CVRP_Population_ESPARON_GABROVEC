package operators;

import java.util.ArrayList;
import java.util.Collections;

import cvrp_population.GeneticAlgorithm;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class MutationOperator {
	
	private GeneticAlgorithm gen;
	
	public MutationOperator(GeneticAlgorithm gen) {
		this.gen = gen;
	}
	
	public ArrayList<Vehicle> inversionMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = gen.getRand().nextInt(locations.size());
		int b = gen.getRand().nextInt(locations.size());
		Collections.reverse(locations.subList(Math.min(a, b), Math.max(a, b)));
		return gen.reconstruct(locations);
	}
	
	public ArrayList<Vehicle> displacementMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = gen.getRand().nextInt(locations.size());
		int b = gen.getRand().nextInt(locations.size());
		ArrayList<Location> subList = new ArrayList<>(locations.subList(Math.min(a, b), Math.max(a, b)));
		locations.removeAll(subList);
		locations.addAll(gen.getRand().nextInt(locations.size()), subList);
		return gen.reconstruct(locations);
	}
}
