package cvrp_population;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
    	double distance;
    	for (int i = 0; i < nbCustomers - 1; i++) {
            for (int j = i + 1; j < nbCustomers; j++) {
                distance = Math.sqrt(Math.pow(locations.get(i).getX() - locations.get(j).getX(), 2) + Math.pow(locations.get(i).getY() - locations.get(j).getY(), 2));
                addDistance(locations.get(i).getId(), locations.get(j).getId(), distance);
                addDistance(locations.get(j).getId(), locations.get(i).getId(), distance);
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
    	Location l = null;
		for (Location currLoc : locations) {
			if (currLoc.getId() == id) {
				l = currLoc;
				break;
			}
		}
		return l;
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
    
	public static void drawGraph(String title, String parametersDesc, String desc, ArrayList<Vehicle> individual) {
		JFrame frame = new JFrame(title);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(parametersDesc));
    	JPanel graph = new DrawGraph(individual);
    	Border border = BorderFactory.createTitledBorder(desc);
    	graph.setBorder(border);
    	mainPanel.add(graph);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
	}
	
	public static void drawLineChart(String title, String parametersDesc, String desc, TreeMap<Integer, Double> bestCostsHistory) {
		JFrame frame = new JFrame(title);
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(parametersDesc));
    	DrawLineChart lineChart = new DrawLineChart(bestCostsHistory);
    	Border border = BorderFactory.createTitledBorder(desc);
    	lineChart.setBorder(border);
    	mainPanel.add(lineChart);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	public static ArrayList<Location> createDeepCopyLocations(ArrayList<Location> source) {
		ArrayList<Location> copy = new ArrayList<Location>();
		for (Location l : source) {
			copy.add(new Location(l));
		}
        return copy;
    }
	
	public static ArrayList<Vehicle> createDeepCopyIndividual(ArrayList<Vehicle> source) {
    	ArrayList<Vehicle> copy = new ArrayList<>();
        for (Vehicle v : source) {
        	copy.add(new Vehicle(v));
        }
        return copy;
    }

	public static String formatInt(int number) {
		char[] suffixes = {'k', 'm', 'g', 't', 'p', 'e' };
		String string = String.valueOf(number);
	    if(number < 1000) {
	        return string;
	    }
	    int magnitude = (string.length() - 1) / 3;
	    int digits = (string.length() - 1) % 3 + 1;
	    char[] value = new char[4];
	    for(int i = 0; i < digits; i++) {
	        value[i] = string.charAt(i);
	    }
	    int valueLength = digits;
	    if(digits == 1 && string.charAt(1) != '0') {
	        value[valueLength++] = '.';
	        value[valueLength++] = string.charAt(1);
	    }
	    value[valueLength++] = suffixes[magnitude - 1];
	    return new String(value, 0, valueLength);
	}
}
