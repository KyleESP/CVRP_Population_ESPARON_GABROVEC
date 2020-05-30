package cvrp_population;

import java.util.ArrayList;
import java.util.TreeMap;

public class Program {
	
	// A3205, A3705, A6109, A6409, A8010...
	private static final String DATA_FILE = "A3205.txt";
	
	private static final int MAX_VEHICLES_CAPACITY = 100;
	
	private static final long NB_GENERATIONS = 300;
	private static final int NB_INDIVIDUALS = 10;
	private static final double P_MUTATION = 0.01;
	private static final double DIFF_RATE = 0.01;
	private static final boolean IS_TOURNAMENT = true;
	private static final boolean IS_HGREX = true;
	private static final boolean IS_HYBRID = true;
	
	public static void main(String[] args) {
		ArrayList<Object[]> parametersTest = new ArrayList<>();
		// Object = [file, nbExec, nbGenerations, nbIndividuals]
		//parametersTest.add(new Object[] {"A3205.txt", 3, 300, 20});
		//parametersTest.add(new Object[] {"A3705.txt", 3, 500, 20});
		//parametersTest.add(new Object[] {"A6109.txt", 3, 500, 30});
		//parametersTest.add(new Object[] {"A6409.txt", 3, 800, 30});
		//parametersTest.add(new Object[] {"A8010.txt", 3, 4000, 50});
		test(parametersTest);
		/*
 	    ArrayList<Location> locations = Util.readData("data/" + DATA_FILE);
	    
	    GeneticAlgorithm ga = new GeneticAlgorithm(locations, MAX_VEHICLES_CAPACITY, NB_GENERATIONS, NB_INDIVIDUALS, 
	    		P_MUTATION, DIFF_RATE, IS_TOURNAMENT, IS_HGREX, IS_HYBRID);
        ga.exec();
        
        String descGA = ga.getInlineDescription();
        String parametersDesc = "Fichier : " + DATA_FILE + " | Nombre de clients : " + (locations.size() - 1) + " | Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
        Util.drawGraph("Graphe CVRP Population", parametersDesc, descGA, ga.getBestIndividual(), true);
        Util.drawLineChart("Line chart CVRP Population", parametersDesc, descGA, ga.getBestCostsHistory());
        */
    }
	
	private static void test(ArrayList<Object[]> parameters) {
		ArrayList<String> results = new ArrayList<>();
		ArrayList<Location> locations;
		ArrayList<Vehicle> bestIndividual = null;
		TreeMap<Integer, Double> bestOfBestCostsHistory = new TreeMap<>();
		double bestCost, currBestCost, mean;
		String file, parametersDesc, bestInlineDescription = "";
		int nbExec, nbGenerations, nbIndividuals;
		for (Object[] p : parameters) {
			file = (String)p[0];
			nbExec = (int)p[1];
			nbGenerations = (int)p[2];
			nbIndividuals = (int)p[3];
			bestCost = Double.POSITIVE_INFINITY;
			mean = 0;
			locations = Util.readData("data/" + (String)p[0]);
			GeneticAlgorithm ga = new GeneticAlgorithm(locations, MAX_VEHICLES_CAPACITY, nbGenerations, nbIndividuals, 
		    		P_MUTATION, DIFF_RATE, IS_TOURNAMENT, IS_HGREX, IS_HYBRID);
			for (int i = 0; i < nbExec; i++) {
				ga.exec();
				mean += (currBestCost = ga.getBestCost());
				if (currBestCost < bestCost) {
					bestIndividual = Util.createDeepCopyIndividual(ga.getBestIndividual());
					bestOfBestCostsHistory.clear();
					bestOfBestCostsHistory.putAll(ga.getBestCostsHistory());
					bestInlineDescription = ga.getInlineDescription();
					bestCost = currBestCost;
				}
			}
			mean /= nbExec;
			results.add(file + ", nbExec = " + nbExec + ", nbGen = " + nbGenerations + 
					", nbIndi = " + nbIndividuals + " --> " + "Moyenne = " + mean);
	        parametersDesc = "Fichier : " + file + " | Nombre de clients : " + (locations.size() - 1) 
	        		+ " | Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
	        Util.drawGraph("Graphe CVRP Population", parametersDesc, bestInlineDescription, bestIndividual, false);
	        Util.drawLineChart("Line chart CVRP Population", parametersDesc, bestInlineDescription, bestOfBestCostsHistory);
		}
		for (String s : results) {
			System.out.println(s);
		}
	}
}
