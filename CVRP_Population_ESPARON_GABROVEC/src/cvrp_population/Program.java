package cvrp_population;

import java.util.ArrayList;
import java.util.HashMap;

public class Program {
	// A3205, A3305, A3306, ..., A6509, A6909, A8010
	private static final String DATA_FILE = "A3205";
	
	private static final int NB_VEHICLES = 20;
	private static final int MAX_VEHICLES_CAPACITY = 100;
	
	private static final long NB_GENERATIONS = 600;
	private static final int NB_INDIVIDUALS = 30;
	private static final double P_CROSS = 0.99999999999999999;
	
	public static void main(String[] args) {
		HashMap<String, ArrayList<Vehicle>> routesList = new HashMap<>();
		HashMap<String, ArrayList<Double>> costsHistories = new HashMap<>();
	    ArrayList<Location> locations = Util.readData("data/" + DATA_FILE + ".txt");
	    
	    Genetic genetic = new Genetic(locations, NB_VEHICLES, MAX_VEHICLES_CAPACITY, NB_GENERATIONS, NB_INDIVIDUALS, P_CROSS);
        genetic.exec();
        String descTabu = "Algorithme génétique (" + genetic.getInlineDescription() + ")";
        routesList.put(descTabu, genetic.getBestVehicles());
        costsHistories.put(descTabu, genetic.getCostsHistory());
        
        /*String parametersDesc = "Nombre de véhicules : " + NB_VEHICLES + ", Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
        Util.drawGraphs("Graphes CVRP Voisinage", parametersDesc, routesList);
        Util.drawLineCharts("Line charts CVRP Voisinage", parametersDesc, costsHistories);*/
    }

}
