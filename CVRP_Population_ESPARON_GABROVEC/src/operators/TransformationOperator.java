package operators;

import java.util.ArrayList;

import cvrp_population.GeneticAlgorithm;
import cvrp_population.Location;
import cvrp_population.Util;
import cvrp_population.Vehicle;

public class TransformationOperator {
	
	private GeneticAlgorithm gen;
	
	public TransformationOperator(GeneticAlgorithm gen) {
		this.gen = gen;
	}
	
	public ArrayList<Vehicle> swapRoutes(ArrayList<Vehicle> individual, ArrayList<Location> routeFrom, ArrayList<Location> routeTo, int vFromIdx, int vToIdx, int locFromIdx, int locToIdx) {
		Vehicle newVFrom = new Vehicle(gen.getMaxCapacity());
		Vehicle newVTo = new Vehicle(gen.getMaxCapacity());
		int i;
        for (i = 0; i <= locFromIdx; i++) {
        	newVFrom.routeLocation(routeFrom.get(i));
        }
        for (i = locToIdx + 1; i < routeTo.size(); i++) {
        	if (!newVFrom.routeLocation(routeTo.get(i))) {
        		return null;
        	}
        }
        
        for (i = 0; i <= locToIdx; i++) {
        	newVTo.routeLocation(routeTo.get(i));
        }
        for (i = locFromIdx + 1; i < routeFrom.size(); i++) {
        	if (!newVTo.routeLocation(routeFrom.get(i))) {
        		return null;
        	}
        }
        ArrayList<Vehicle> newVehicles = Util.createDeepCopyVehicles(individual);
        newVehicles.set(vFromIdx, newVFrom);
        newVehicles.set(vToIdx, newVTo);
		return newVehicles;
    }
	
	public ArrayList<Vehicle> swapTwoOpt(ArrayList<Vehicle> individual, int vIdx, int locFromIdx, int locToIdx) {
		Vehicle newV = new Vehicle(gen.getMaxCapacity());
		ArrayList<Location> route = individual.get(vIdx).getRoute();
		
		int i;
        for (i = 0; i <= locFromIdx - 1; i++) {
            newV.routeLocation(route.get(i));
        }
        int dcr = 0;
        for (i = locFromIdx; i <= locToIdx; i++) {
            newV.routeLocation(route.get(locToIdx - dcr));
            dcr++;
        }
        for (i = locToIdx + 1; i < route.size(); i++) {
        	newV.routeLocation(route.get(i));
        }
        ArrayList<Vehicle> newVehicles = Util.createDeepCopyVehicles(individual);
        newVehicles.set(vIdx, newV);
        return newVehicles;
    }
}
