package operators;

import java.util.ArrayList;
import java.util.Random;

import cvrp_population.Util;
import cvrp_population.Vehicle;

public class SelectionOperator {
	
	private Random rand;
	
	public SelectionOperator(Random rand) {
		this.rand = rand;
	}
	
	public ArrayList<Vehicle> tournament(ArrayList<ArrayList<Vehicle>> population, int nbParticipants) {
		ArrayList<ArrayList<Vehicle>> participants = new ArrayList<>(nbParticipants);
		double totalCost = 0;
		ArrayList<Vehicle> participant;
		ArrayList<Double> costs = new ArrayList<>();
		double cost;
		for (int i = 0; i < nbParticipants; i++) {
			participant = population.get(rand.nextInt(population.size()));
			participants.add(participant);
			cost = Util.objectiveFunction(participant);
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
	
}
