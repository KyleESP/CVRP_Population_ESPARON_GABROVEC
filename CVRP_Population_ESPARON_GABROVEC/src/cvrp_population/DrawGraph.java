package cvrp_population;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;

public class DrawGraph extends JPanel {
	
	private static final long serialVersionUID = 1L;
    private final int MARGIN = 30;
    private final int SIZE = 900;
    private Color lineColor = new Color(100, 100, 100, 180);
    private Color pointColor = new Color(0, 0, 153, 180);
    private Color labelColor = Color.RED;
    private Color bgColor = Color.WHITE;
    private int pointWidth = 10;
    private Vehicle[] vehicles;
    
    public DrawGraph(Vehicle[] vehicles) {
    	this.vehicles = vehicles;
        this.setPreferredSize(new Dimension(SIZE, SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(2));
        g2d.setFont(new Font("default", Font.BOLD, 16));
        g.setColor(bgColor);
        g.fillRect(0, 0, SIZE, SIZE);

        
        double graphSize = SIZE - 2 * MARGIN;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Vehicle v : vehicles) {
            for (Location l : v.getRoute()) {
                maxX = Math.max(l.getX(), maxX);
                maxY = Math.max(l.getY(), maxY);
                minX = Math.min(l.getX(), minX);
                minY = Math.min(l.getY(), minY);
            }
        }
        
        ArrayList<Location> route;
        for (Vehicle v : vehicles) {
        	route = v.getRoute();
            for (int j = 1; j < route.size(); j++) {
            	Location l1 = route.get(j);
            	Location l2 = route.get(j - 1);
                int xl1 = (int) (graphSize * (l1.getX() - minX) / (maxX - minX)) + MARGIN;
                int yl1 = (int) (graphSize * (1 - (l1.getY() - minY) / (maxY - minY))) + MARGIN;
                int xl2 = (int) (graphSize * (l2.getX() - minX) / (maxX - minX)) + MARGIN;
                int yl2 = (int) (graphSize * (1 - (l2.getY() - minY) / (maxY - minY))) + MARGIN;
                g2d.setColor(lineColor);
                g2d.drawLine(xl1, yl1, xl2, yl2);
                g2d.setColor(pointColor);
                g2d.fillOval(xl1 - pointWidth / 2, yl1 - pointWidth / 2, pointWidth, pointWidth);
                g2d.setColor(labelColor);
                g2d.drawString(Integer.toString(l1.getId()), xl1 + pointWidth / 2 + 2, yl1 + pointWidth / 2 + 2);
            }
        }
    }
}