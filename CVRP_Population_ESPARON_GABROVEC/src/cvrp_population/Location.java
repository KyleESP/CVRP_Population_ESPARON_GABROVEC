package cvrp_population;

public class Location {
	
	private int id;
	private int x;
	private int y;
	private int nbOrders;
	private boolean isRouted;
	
	public Location(int id, int x, int y, int nbOrders) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.nbOrders = nbOrders;
		isRouted = false;
	}
	
	// For deep copy
	public Location(Location l) {
		this.id = l.getId();
		this.x = l.getX();
		this.y = l.getY();
		this.nbOrders = l.getNbOrders();
	}

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getNbOrders() {
		return nbOrders;
	}
	
	public boolean getIsRouted() {
		return isRouted;
	}

	public void setIsRouted(boolean isRouted) {
		this.isRouted = isRouted;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (id != other.id)
			return false;
		if (nbOrders != other.nbOrders)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
