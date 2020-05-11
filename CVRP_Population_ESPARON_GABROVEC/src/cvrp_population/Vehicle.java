package cvrp_population;

import java.util.ArrayList;

public class Vehicle {
	
    private ArrayList<Location> route;
    private int capacity;
    private int currentLocationId;
    private int currentLoading;

    public Vehicle(int capacity) {
        this.route = new ArrayList<>();
        this.capacity = capacity;
        this.currentLocationId = 0;
        this.currentLoading = 0;
    }
    
    // For deep copy
	public Vehicle(Vehicle v) {
		ArrayList<Location> copyRoute = new ArrayList<>();
    	for (Location l : v.getRoute()) {
    		copyRoute.add(new Location(l));
    	}
    	this.route = copyRoute;
    	this.capacity = v.getCapacity();
    	this.currentLocationId = v.getCurrentLocationId();
    	this.currentLoading = v.getCurrentLoading();
    }
    
    public boolean fits(int nbOrders) {
        return capacity >= nbOrders + currentLoading;
    }

	public boolean routeLocation(Location l) {
		if (!fits(l.getNbOrders())) {
			return false;
		}
		route.add(l);
        l.setIsRouted(true);
        currentLocationId = l.getId();
        currentLoading += l.getNbOrders();
        return true;
    }
	
	public int getCapacity() {
		return capacity;
	}

	public int getCurrentLoading() {
		return currentLoading;
	}
	
	public int getCurrentLocationId() {
		return currentLocationId;
	}
	
	public ArrayList<Location> getRoute() {
		return route;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vehicle other = (Vehicle) obj;
		if (capacity != other.capacity)
			return false;
		if (currentLoading != other.currentLoading)
			return false;
		if (currentLocationId != other.currentLocationId)
			return false;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		return true;
	}
	
	
}