package operators;

import java.util.ArrayList;

import cvrp_population.GeneticAlgorithm;
import cvrp_population.Vehicle;

public class SelectionOperator {
	
	private GeneticAlgorithm ga;
	
	public SelectionOperator(GeneticAlgorithm ga) {
		this.ga = ga;
	}
	
	public ArrayList<ArrayList<Vehicle>> tournamentSelection(int nbParticipants) {
		ArrayList<ArrayList<Vehicle>> participants = new ArrayList<>(nbParticipants), population = ga.getPopulation();
		ArrayList<Vehicle> participant;
		ArrayList<Double> costs = new ArrayList<>();
		double totalCost = 0, cost;
		for (int i = 0; i < nbParticipants; i++) {
			participant = population.get(ga.getRand().nextInt(population.size()));
			participants.add(participant);
			cost = ga.objectiveFunction(participant);
			costs.add(cost);
			totalCost += cost;
		}
		ArrayList<double[]> probasRep = getProbasRepartition(costs, totalCost);
		ArrayList<ArrayList<Vehicle>> winners = new ArrayList<>();
		winners.add(getWinner(probasRep, participants));
		winners.add(getWinner(probasRep, participants));
		return winners;
	}
	
	public ArrayList<ArrayList<Vehicle>> rouletteWheelSelection() {
		ArrayList<Double> costs = new ArrayList<>();
		double totalCost = 0, cost;
		for (ArrayList<Vehicle> individual : ga.getPopulation()) {
			cost = ga.objectiveFunction(individual);
			costs.add(cost);
			totalCost += cost;
		}
		ArrayList<double[]> rouletteWheel = getProbasRepartition(costs, totalCost);
		ArrayList<ArrayList<Vehicle>> winners = new ArrayList<>();
		winners.add(getWinner(rouletteWheel, ga.getPopulation()));
		winners.add(getWinner(rouletteWheel, ga.getPopulation()));
		return winners;
	}
	
	private ArrayList<double[]> getProbasRepartition(ArrayList<Double> costs, double totalCost) {
		ArrayList<double[]> probasRep = new ArrayList<double[]>();
		double p = 0;
		double[] interval;
		for (int i = 0; i < costs.size() - 1; i++) {
			interval = new double[2];
			interval[0] = p;
			p += costs.get(i + 1) / totalCost;
			interval[1] = p;
			probasRep.add(interval);
		}
		probasRep.add(new double[] {p, 1d});
		return probasRep;
	}
	
	private ArrayList<Vehicle> getWinner(ArrayList<double[]> probasRepartition, ArrayList<ArrayList<Vehicle>> participants) {
		double[] interval;
		ArrayList<Vehicle> winner = null;
		double p = ga.getRand().nextDouble();
		for (int j = 0; j < probasRepartition.size(); j++) {
			interval = probasRepartition.get(j);
			if (p >= interval[0] && p < interval[1]) {
				winner = participants.get(j);
				break;
			}
		}
		return winner;
	}
	
}
