package cvrp_population;

import java.util.ArrayList;
import java.util.HashMap;

public class Program {
	// A3205, A3305, A3306, ..., A6509, A6909, A8010
	private static final String DATA_FILE = "A3205";
	
	private static final int NB_VEHICLES = 20;
	private static final int MAX_VEHICLES_CAPACITY = 100;
	
	private static final long TABU_MAX_ITERATION = 500;
	private static final int TABU_MAX_LIST_SIZE = 100;
	
	private static final double SA_MU = 0.9;
	private static final int SA_N2 = 800;
	private static final double SA_P_ACCEPTING_COSTING_SOL = 0.8;
	private static final double SA_P_ACCEPTING_SAME_SOL = 0.00000000001;
	
	public static void main(String[] args) {
		HashMap<String, Vehicle[]> routesList = new HashMap<>();
		HashMap<String, ArrayList<Double>> costsHistories = new HashMap<>();
	    ArrayList<Location> locations = Util.readData("data/" + DATA_FILE + ".txt");
	    
	    Tabu tabu = new Tabu(locations, NB_VEHICLES, MAX_VEHICLES_CAPACITY, TABU_MAX_ITERATION, TABU_MAX_LIST_SIZE);
        tabu.exec();
        String descTabu = "Recherche Tabou (" + tabu.getInlineDescription() + ")";
        routesList.put(descTabu, tabu.getBestVehicles());
        costsHistories.put(descTabu, tabu.getCostsHistory());
        
        SimulatedAnnealing sa = new SimulatedAnnealing(locations, NB_VEHICLES, MAX_VEHICLES_CAPACITY, SA_MU, SA_N2,
        		SA_P_ACCEPTING_COSTING_SOL, SA_P_ACCEPTING_SAME_SOL);
        sa.exec();
        String descSA = "Recuit simulé (" + sa.getInlineDescription() + ")";
        routesList.put(descSA, sa.getBestVehicles());
        costsHistories.put(descSA, sa.getCostsHistory());
        
        String parametersDesc = "Nombre de véhicules : " + NB_VEHICLES + ", Capacité maximale des véhicules : " + MAX_VEHICLES_CAPACITY;
        Util.drawGraphs("Graphes CVRP Voisinage", parametersDesc, routesList);
        Util.drawLineCharts("Line charts CVRP Voisinage", parametersDesc, costsHistories);
    }

}
