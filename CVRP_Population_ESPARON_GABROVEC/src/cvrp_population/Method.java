package cvrp_population;

import java.util.ArrayList;

public abstract class Method {
	
	protected int maxCapacity;
    protected int nbVehicles;
	protected ArrayList<Location> locations;
	protected Vehicle[] vehicles;
	protected double cost;
    protected Vehicle[] bestVehicles;
    protected double bestCost;
    protected ArrayList<Double> costsHistory;
    
    public Method(ArrayList<Location> locations, int nbVehicles, int maxCapacity) {
    	this.nbVehicles = nbVehicles;
    	this.maxCapacity = maxCapacity;
    	this.locations = Util.createDeepCopy(locations);
    	try {
			initRoutes();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
    	cost = objectiveFunction(vehicles);
    	costsHistory = new ArrayList<>();
    	costsHistory.add(cost);
    	bestVehicles = vehicles;
    	bestCost = cost;
    }
    
    public abstract void exec();
    
    public abstract String getInlineDescription();
    
    public abstract void displayDescription();
    
    public Vehicle[] getVehicles() {
    	return vehicles;
    }
    
    public Vehicle[] getBestVehicles() {
    	return bestVehicles;
    }
    
    public double getBestCost() {
    	return bestCost;
    }
    
    public ArrayList<Double> getCostsHistory() {
    	return costsHistory;
    }
    
    protected ArrayList<Vehicle> getActiveVehicles() {
    	ArrayList<Vehicle> activeVehicles = new ArrayList<Vehicle>();
    	for (Vehicle v : vehicles) {
    		if (!v.getRoute().isEmpty()) {
    			activeVehicles.add(v);
    		} else {
    			break;
    		}
    	}
    	return activeVehicles;
    }
    
    private void initRoutes() throws Exception {
    	vehicles = new Vehicle[nbVehicles];
    	for (int i = 0 ; i < nbVehicles; i++) {
            vehicles[i] = new Vehicle(maxCapacity);
        }
    	
        int vIdx = 0;
        Vehicle v;
        Location depot = locations.get(0);
        while (hasAnUnroutedLocation()) {
        	v = vehicles[vIdx];
        	if (v.getRoute().isEmpty()) {
                v.routeLocation(depot);
            }
            Location choseLocation = null;
            double minCost = Double.POSITIVE_INFINITY;
            int currentLocationId = v.getCurrentLocationId();
            double dist;
            for(Location l : locations) {
            	if((currentLocationId != l.getId()) && !l.getIsRouted() && v.fits(l.getNbOrders()) 
            			&& minCost > (dist = Util.getDistances().get(currentLocationId).get(l.getId()))) {
					minCost = dist;
                    choseLocation = l;
            	}
            }
            
            if(choseLocation != null) {
            	v.routeLocation(choseLocation);
            } else if (vIdx++ < vehicles.length && currentLocationId != depot.getId()) {
                v.routeLocation(depot);
            } else {
                throw new Exception("Il n'y a plus assez de véhicules pour le reste des clients.");
            }
        }
        vehicles[vIdx].routeLocation(depot);
    }
    
    private boolean hasAnUnroutedLocation() {
    	boolean hasAnUnroutedLocation = false;
        for(Location l : locations) {
            if (!l.getIsRouted()) {
            	hasAnUnroutedLocation = true;
            	break;
            }
        }
        return hasAnUnroutedLocation;
    }
    
	protected ArrayList<Vehicle[]> getNeighbors() {
		ArrayList<Vehicle[]> neighbors = new ArrayList<>();
		ArrayList<Location> routeFrom, routeTo;
		int routeFromSize, routeToSize;
		boolean isSameRoute;
		int totalLoading;
		ArrayList<Vehicle> activeVehicles = getActiveVehicles();
		for (int vFromIdx = 0; vFromIdx < activeVehicles.size(); vFromIdx++) {
			routeFrom = vehicles[vFromIdx].getRoute();
			routeFromSize = routeFrom.size();
			for (int vToIdx = vFromIdx; vToIdx < activeVehicles.size(); vToIdx++) {
				routeTo = vehicles[vToIdx].getRoute();
				routeToSize = routeTo.size();
				isSameRoute = (vFromIdx == vToIdx);
				if (!isSameRoute) {
					totalLoading = vehicles[vFromIdx].getCurrentLoading() + vehicles[vToIdx].getCurrentLoading();
					if (totalLoading > (maxCapacity * 2)) {
						continue;
					} else if (totalLoading <= maxCapacity) {
						neighbors.add(deleteOneRoute(routeFrom, routeTo, vFromIdx, vToIdx));
					}
				}
				for (int locFromIdx = 1; locFromIdx < routeFromSize - (isSameRoute ? 2 : 1); locFromIdx++) {
					for (int locToIdx = (isSameRoute ? (locFromIdx + 1) : (locFromIdx == routeFromSize - 2 ? 1 : 0)); locToIdx < routeToSize - ((isSameRoute && locFromIdx == 1 || !isSameRoute && locFromIdx == routeFromSize - 2) ? 2 : 1); locToIdx++) {
						Vehicle[] newVehicles = isSameRoute ? swapTwoOpt(vFromIdx, locFromIdx, locToIdx) : swapRoutes(routeFrom, routeTo, vFromIdx, vToIdx, locFromIdx, locToIdx);
						if (newVehicles != null) {
							neighbors.add(newVehicles);
						}
					}
				}
			}
		}
		return neighbors;
	}
    
	private Vehicle[] swapRoutes(ArrayList<Location> routeFrom, ArrayList<Location> routeTo, int vFromIdx, int vToIdx, int locFromIdx, int locToIdx) {
		Vehicle newVFrom = new Vehicle(maxCapacity);
		Vehicle newVTo = new Vehicle(maxCapacity);
		
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
        
        Vehicle[] newVehicles = Util.createDeepCopy(vehicles);
        newVehicles[vFromIdx] = newVFrom;
        newVehicles[vToIdx] = newVTo;
		return newVehicles;
    }
    
    private Vehicle[] deleteOneRoute(ArrayList<Location> routeFrom, ArrayList<Location> routeTo, int vFromIdx, int vToIdx) {
    	Vehicle[] newVehicles = Util.createDeepCopy(vehicles);
    	Vehicle newV = new Vehicle(maxCapacity);
		
		int i;
		for (i = 0; i < routeFrom.size() - 1; i++) {
			newV.routeLocation(routeFrom.get(i));
		}
		for (i = 1; i < routeTo.size(); i++) {
			newV.routeLocation(routeTo.get(i));
		}
		
		newVehicles[vFromIdx] = newV;
		for (i = vToIdx; i < newVehicles.length - 1; i++) {
            newVehicles[i] = new Vehicle(newVehicles[i + 1]);
        }
		newVehicles[newVehicles.length - 1] = new Vehicle(maxCapacity);
		
		return newVehicles;
    }
	
	private Vehicle[] swapTwoOpt(int vIdx, int i, int j) {
		Vehicle newV = new Vehicle(maxCapacity);
		ArrayList<Location> route = vehicles[vIdx].getRoute();
		
		int k;
        for (k = 0; k <= i - 1; k++) {
            newV.routeLocation(route.get(k));
        }
        int dcr = 0;
        for (k = i; k <= j; k++) {
            newV.routeLocation(route.get(j - dcr));
            dcr++;
        }
        for (k = j + 1; k < route.size(); k++) {
        	newV.routeLocation(route.get(k));
        }
        
        Vehicle[] newVehicles = Util.createDeepCopy(vehicles);
        newVehicles[vIdx] = newV;
        return newVehicles;
    }
	
	protected double objectiveFunction(Vehicle[] vehicles) {
    	double sumDist = 0;
    	int sumVehicles = 0;
    	ArrayList<Location> route;
    	for(Vehicle v : vehicles) {
    		route = v.getRoute();
    		if (!route.isEmpty()) {
    			sumVehicles++;
    			for (int i = 0; i < route.size() - 1; i++) {
        			sumDist += Util.getDistances().get(route.get(i).getId()).get(route.get(i + 1).getId());
        		}
    		}
    	}
    	return sumDist + sumVehicles;
    }
    
    public void displayBestSolution() {
    	System.out.println("----------------------------------------------------------------------------------------------------");
        ArrayList<Location> route;
        for (int i = 0 ; i < bestVehicles.length ; i++) {
        	route = bestVehicles[i].getRoute();
            if (!route.isEmpty()) {   
                System.out.println("Véhicule " + i + " : " + getRouteString(route));
            }
        }
        System.out.println("\nCoût de la solution : " + bestCost);
    	System.out.println("----------------------------------------------------------------------------------------------------");
    }
    
    protected String getRouteString(ArrayList<Location> route) {
    	int routeSize = route.size();
    	String routeString = "";
    	for (int i = 0; i < routeSize ; i++) {
        	routeString += "(" + route.get(i).getId() + ")" + ((i != routeSize - 1) ? " == " : "");
        }
    	return routeString;
    }
}
