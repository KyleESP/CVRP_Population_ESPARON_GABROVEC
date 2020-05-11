package cvrp_population;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing extends Method {
	
	private double mu;
	private double initialTemperature;
	private double temperature;
	private int n1;
	private int n2;
	private double pAcceptingCostingSol;
	private double pAcceptingSameSol;
	private Random rand;
	
	public SimulatedAnnealing(ArrayList<Location> locations, int nbOfVehicles, int maxCapacity, 
			double mu, int n2, double pAcceptingCostingSol, double pAcceptingSameSol) {
		super(locations, nbOfVehicles, maxCapacity);
		this.n2 = n2;
		this.pAcceptingCostingSol = pAcceptingCostingSol;
		this.pAcceptingSameSol = pAcceptingSameSol;
		this.mu = mu;
		this.rand = new Random();
	}
	
	@Override
	public void exec() {
		setParameters();
		displayDescription();
	    ArrayList<Vehicle[]> neighbors = new ArrayList<>();
	    double deltaF;
	    double fCurr;
	    for (int k = 0; k < n1; k++) {
	    	for (int l = 0; l < n2; l++) {
	    		neighbors = getNeighbors();
	    		Vehicle[] newVehicles = getRandomNeighbor(neighbors);
	    		fCurr = objectiveFunction(newVehicles);
	    		deltaF = fCurr - cost;
	    		if (deltaF < 0) {
	    			vehicles = newVehicles;
	    			cost = fCurr;
	    			if (fCurr < bestCost) {
	    				bestVehicles = vehicles;
	    				bestCost = fCurr;
	    			}
	    		} else if (rand.nextDouble() <= Math.exp(-deltaF / temperature)) {
    				vehicles = newVehicles;
    				cost = fCurr;
	    		}
	    		costsHistory.add(cost);
	    	}
	    	temperature *= mu;
        	System.out.println((int)(((double)(k + 1) / n1) * 100) + "%");
	    }
	    displayBestSolution();
    }
	
	private void setParameters() {
		double deltaF;
		double sum = 0;
		ArrayList<Vehicle[]> neighbors = getNeighbors();
		int nbDeltaFs = (int)(0.85 * neighbors.size());
		for (int i = 0; i < nbDeltaFs; i++) {
			do {
				Vehicle[] neighbor = getRandomNeighbor(neighbors);
				deltaF = objectiveFunction(neighbor) - cost;
			} while (deltaF <= 0);
			sum += deltaF;
		}
		double meanDeltaFs = sum / nbDeltaFs;
		initialTemperature = -meanDeltaFs / Math.log(pAcceptingCostingSol);
	    temperature = initialTemperature;
	    n1 = (int)(Math.log(-meanDeltaFs / (temperature * Math.log(pAcceptingSameSol))) / Math.log(mu));
	}
	
	@Override
	public void displayDescription() {
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("Recuit simulé :");
		String description = "Coût initial = " + cost;
		description += "\nt0 = " + initialTemperature;
		description += "\nmu = " + mu;
		description += "\nn1 = " + n1;
		description += "\nn2 = " + n2;
		description += "\nProbabilité d'accepter des solutions coûteuses = " + pAcceptingCostingSol;
		description += "\nProbabilité d'accepter la même solution = " + pAcceptingSameSol;
		System.out.println(description);
		System.out.println("----------------------------------------------------------------------------------------------------");
	}
	
	@Override
	public String getInlineDescription() {
		DecimalFormat df = new DecimalFormat("#.##");
		String description = "Coût final = " + bestCost;
		description += " | Nb véhicules = " + getActiveVehicles().size() + " | ";
		description += " | t0 = " +  df.format(initialTemperature);
		description += " | t final = " + df.format(temperature);
		description += " | mu = " + mu;
		description += " | n1 = " + n1;
		description += " | n2 = " + n2;
		description += " | pCostingSol = " + pAcceptingCostingSol;
		description += " | pSameSol = " + pAcceptingSameSol;
		return description;
	}
	
	private Vehicle[] getRandomNeighbor(ArrayList<Vehicle[]> neighbors) {
		int randIdx = rand.nextInt(neighbors.size());
		return neighbors.get(randIdx);
	}
	
	public void setMu(double mu) {
		this.mu = mu;
	}

	public void setN1(int n1) {
		this.n1 = n1;
	}

	public void setpAcceptingCostingSol(double pAcceptingCostingSol) {
		this.pAcceptingCostingSol = pAcceptingCostingSol;
	}
	
	public void setpAcceptingSameSol(double pAcceptingSameSol) {
		this.pAcceptingSameSol = pAcceptingSameSol;
	}
}
