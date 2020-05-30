package cvrp_population;

import java.util.ArrayList;

public class Program {
	
	// A3205, A3305, A3306, ..., A6509, A6909, A8010
	private static final String DATA_FILE = "A3205.txt";
	
	private static final int MAX_VEHICLES_CAPACITY = 100;
	
	private static final long NB_GENERATIONS = 300;
	private static final int NB_INDIVIDUALS = 10;
	private static final double DIFF_RATE = 0.01;
	private static final boolean IS_TOURNAMENT = true;
	private static final boolean IS_HGREX = true;
	private static final double P_MUTATION = 0.01;
	private static final boolean IS_HYBRID = true;
	
	public static void main(String[] args) {
 	    ArrayList<Location> locations = Util.readData("data/" + DATA_FILE);
	    
	    GeneticAlgorithm ga = new GeneticAlgorithm(locations, MAX_VEHICLES_CAPACITY, NB_GENERATIONS, NB_INDIVIDUALS, 
	    		P_MUTATION, DIFF_RATE, IS_TOURNAMENT, IS_HGREX, IS_HYBRID);
        ga.exec();
        
        String descGA = ga.getInlineDescription();
        String parametersDesc = "Fichier : " + DATA_FILE + " | Nombre de clients : " + (locations.size() - 1) + " | Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
        Util.drawGraph("Graphe CVRP Population", parametersDesc, descGA, ga.getBestIndividual());
        Util.drawLineChart("Line chart CVRP Population", parametersDesc, descGA, ga.getBestCostsHistory());
    }

}
