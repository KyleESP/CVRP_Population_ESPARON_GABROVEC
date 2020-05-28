package operators;

import java.util.ArrayList;

import cvrp_population.GeneticAlgorithm;
import cvrp_population.Vehicle;

public class SelectionOperator {
	
	private GeneticAlgorithm gen;
	
	public SelectionOperator(GeneticAlgorithm gen) {
		this.gen = gen;
	}
	
	public ArrayList<Vehicle> tournament(int nbParticipants) {
		ArrayList<ArrayList<Vehicle>> participants = new ArrayList<>(nbParticipants), population = gen.getPopulation();
		ArrayList<Vehicle> participant;
		ArrayList<Double> costs = new ArrayList<>();
		double totalCost = 0, cost;
		for (int i = 0; i < nbParticipants; i++) {
			participant = population.get(gen.getRand().nextInt(population.size()));
			participants.add(participant);
			cost = gen.objectiveFunction(participant);
			costs.add(cost);
			totalCost += cost;
		}
		return getWinner(getProbasRepartition(costs, totalCost), participants);
	}
	
	public ArrayList<ArrayList<Vehicle>> rouletteWheel() {
		ArrayList<Double> costs = new ArrayList<>();
		double totalCost = 0, cost;
		for (ArrayList<Vehicle> individual : gen.getPopulation()) {
			cost = gen.objectiveFunction(individual);
			costs.add(cost);
			totalCost += cost;
		}
		ArrayList<double[]> rouletteWheel = getProbasRepartition(costs, totalCost);
		ArrayList<ArrayList<Vehicle>> nextPopulation = new ArrayList<>();
		for (int i = 0; i < gen.getNbIndividuals(); i++) {
			nextPopulation.add(getWinner(rouletteWheel, gen.getPopulation()));
		}
		return nextPopulation;
	}
	
	private ArrayList<double[]> getProbasRepartition(ArrayList<Double> costs, double totalCost) {
		ArrayList<double[]> probasRep = new ArrayList<double[]>();
		double p = 0;
		for (int i = 0; i < costs.size() - 1; i++) {
			double[] interval = new double[2];
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
		double p = gen.getRand().nextDouble();
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
