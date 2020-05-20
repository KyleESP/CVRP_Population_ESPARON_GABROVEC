package cvrp_population;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import drawings.DrawGraph;
import drawings.DrawLineChart;

public abstract class Util {
	
	private static HashMap<Integer, HashMap<Integer, Double>> distances;
	
	public static HashMap<Integer, HashMap<Integer, Double>> getDistances() {
		return distances;
	}
	
	public static ArrayList<Location> readData(String pathFile) {
		ArrayList<Location> locations = new ArrayList<Location>();				
		int[] locationValues = new int[4];
		boolean firstLine = true;
		try {
			List<String> lines = Files.readAllLines(Paths.get(pathFile));
			for (String line : lines) {
				if(firstLine) {
					firstLine = false;
					continue;
				}
				int count = 0;
				for (String data : line.split(";")) {
					locationValues[count] = Integer.valueOf(data);
					count++;
				}
				Location l = new Location(locationValues[0], locationValues[1], locationValues[2], locationValues[3]);
				locations.add(l);
			}
		} catch (NumberFormatException e) {
			System.err.print(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		initDistances(locations);
		return locations;
	}
	
	private static void initDistances(ArrayList<Location> locations) {
    	distances = new HashMap<>();
    	int nbCustomers = locations.size();
    	for (int i = 0; i < nbCustomers - 1; i++) {
            for (int j = i + 1; j < nbCustomers; j++) {
                double distance = Math.sqrt(Math.pow(locations.get(i).getX() - locations.get(j).getX(), 2) + Math.pow(locations.get(i).getY() - locations.get(j).getY(), 2));
                addDistance(locations.get(i).getId(), locations.get(j).getId(), distance);
                addDistance(locations.get(j).getId(),locations.get(i).getId(), distance);
            }
        }
    }
    
    private static void addDistance(int iId, int jId, double distance) {
    	if(distances.containsKey(iId)) {
        	distances.get(iId).put(jId, distance);
        } else {
        	HashMap<Integer, Double> jMap = new HashMap<>();
        	jMap.put(jId, distance);
        	distances.put(iId, jMap);
        }
    }
    
    public static Location getLocationById(int id, ArrayList<Location> locations) {
		for (Location l : locations) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}
    
    public static ArrayList<Location> getLocations(ArrayList<Vehicle> individual) {
		ArrayList<Location> locations = new ArrayList<>();
		for (Vehicle v : individual) {
			for (Location l : v.getRoute()) {
				if (l.getId() != 0) {
					locations.add(l);
				}
			}
		}
		return locations;
	}
    
	public static void drawGraphs(String title, String parametersDesc, HashMap<String, ArrayList<Vehicle>> routesList) {
		JFrame frame = new JFrame(title);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(parametersDesc));
        for (Map.Entry<String, ArrayList<Vehicle>> entry : routesList.entrySet()) {
        	JPanel graph = new DrawGraph(entry.getValue());
        	Border border = BorderFactory.createTitledBorder(entry.getKey());
        	graph.setBorder(border);
        	mainPanel.add(graph);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
	}
	
	public static void drawLineCharts(String title, String parametersDesc, HashMap<String, ArrayList<Double>> bestCostsHistories) {
		JFrame frame = new JFrame(title);
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(parametersDesc));
        for (Map.Entry<String, ArrayList<Double>> entry : bestCostsHistories.entrySet()) {
        	DrawLineChart lineChart = new DrawLineChart(entry.getValue());
        	lineChart.setPreferredSize(new Dimension(900, 650));
        	Border border = BorderFactory.createTitledBorder(entry.getKey());
        	lineChart.setBorder(border);
        	mainPanel.add(lineChart);
        }
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	public static ArrayList<Vehicle> createDeepCopyVehicles(ArrayList<Vehicle> source) {
    	ArrayList<Vehicle> copy = new ArrayList<>();
        for (Vehicle v : source) {
        	copy.add(new Vehicle(v));
        }
        return copy;
    }
	
	public static ArrayList<ArrayList<Vehicle>> createDeepCopyPopulation(ArrayList<ArrayList<Vehicle>> source) {
    	ArrayList<ArrayList<Vehicle>> copy = new ArrayList<>();
        for (ArrayList<Vehicle> vs : source) {
        	copy.add(createDeepCopyVehicles(vs));
        }
        return copy;
    }
	
	public static ArrayList<Location> createDeepCopyLocations(ArrayList<Location> source) {
		ArrayList<Location> copy = new ArrayList<Location>();
		for (Location l : source) {
			copy.add(new Location(l));
		}
        return copy;
    }
}
