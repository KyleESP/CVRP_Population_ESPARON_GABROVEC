package cvrp_population;

import java.util.ArrayList;

public class Program {
	// A3205, A3305, A3306, ..., A6509, A6909, A8010
	private static final String DATA_FILE = "A3205.txt";
	
	private static final int MAX_VEHICLES_CAPACITY = 100;
	
	private static final long NB_GENERATIONS = 100000;
	private static final int NB_INDIVIDUALS = 30;
	private static final double P_MUTATION = 0.01;
	private static final double DIFF_RATE = 0.01;
	
	/*private static final long NB_GENERATIONS_2 = 30000;
	private static final int NB_INDIVIDUALS_2 = 90;
	private static final int NB_BEST_2 = 10;
	private static final double P_MUTATION_2 = 0.01;*/
	
	public static void main(String[] args) {
 	    ArrayList<Location> locations = Util.readData("data/" + DATA_FILE);
	    
	    GeneticAlgorithm ga = new GeneticAlgorithm(locations, MAX_VEHICLES_CAPACITY, NB_GENERATIONS, NB_INDIVIDUALS, P_MUTATION, DIFF_RATE);
        ga.exec();
        
        String descGA = "Algorithme génétique (" + ga.getInlineDescription() + ")";
        String parametersDesc = "Fichier : " + DATA_FILE + " | Nombre de locations : " + locations.size() + " | Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
        Util.drawGraph("Graphe CVRP Population", parametersDesc, descGA, ga.getBestIndividual());
        Util.drawLineChart("Line chart CVRP Population", parametersDesc, descGA, ga.getBestCostsHistory());
    }

}
