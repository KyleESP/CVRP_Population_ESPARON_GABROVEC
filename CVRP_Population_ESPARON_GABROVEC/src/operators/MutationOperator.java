package operators;

import java.util.ArrayList;
import java.util.Collections;

import cvrp_population.GeneticAlgorithm;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class MutationOperator {
	
	private GeneticAlgorithm ga;
	
	public MutationOperator(GeneticAlgorithm ga) {
		this.ga = ga;
	}
	
	public ArrayList<Vehicle> inversionMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = ga.getRand().nextInt(locations.size());
		int b = ga.getRand().nextInt(locations.size());
		Collections.reverse(locations.subList(Math.min(a, b), Math.max(a, b)));
		return ga.reconstruct(locations);
	}
	
	public ArrayList<Vehicle> displacementMutation(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = Util.getLocations(individual);
		int a = ga.getRand().nextInt(locations.size());
		int b = ga.getRand().nextInt(locations.size());
		ArrayList<Location> subList = new ArrayList<>(locations.subList(Math.min(a, b), Math.max(a, b)));
		locations.removeAll(subList);
		locations.addAll(ga.getRand().nextInt(locations.size()), subList);
		return ga.reconstruct(locations);
	}
}
