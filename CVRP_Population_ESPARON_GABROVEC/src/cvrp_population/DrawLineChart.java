package cvrp_population;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JPanel;

public class DrawLineChart extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int WIDTH = 1400;
    private final int HEIGHT = 1000;
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color minPointsColor = Color.RED;
    private Color gridColor = new Color(200, 200, 200, 200);
    private Color bgColor = Color.WHITE;
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private ArrayList<Double> scores;

    public DrawLineChart(ArrayList<Double> scores) {
        this.scores = scores;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int scoresSize = scores.size();
        double minScore = Collections.min(scores);
        double maxScore = Collections.max(scores);
        double xScale = ((double)getWidth() - (2 * padding) - labelPadding) / (scoresSize - 1);
        double yScale = ((double)getHeight() - 2 * padding - labelPadding) / (maxScore - minScore);
        
        ArrayList<Point> graphPoints = new ArrayList<>();
        ArrayList<Point> minPoints = new ArrayList<>();
        for (int i = 0; i < scoresSize; i++) {
            int x1 = (int)(i * xScale + padding + labelPadding);
            int y1 = (int)((maxScore - scores.get(i)) * yScale + padding);
            Point p = new Point(x1, y1);
            if (scores.get(i) == minScore) {
            	minPoints.add(p);
            }
            graphPoints.add(p);
        }

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.BLACK);

        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (scoresSize > 0) {
                g2d.setColor(gridColor);
                g2d.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2d.setColor(Color.BLACK);
                String yLabel = ((int) ((minScore + (maxScore - minScore) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2d.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2d.drawLine(x0, y0, x1, y1);
        }

        for (int i = 0; i < scoresSize; i++) {
            if (scoresSize > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scoresSize - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((scoresSize / 20.0)) + 1)) == 0) {
                    g2d.setColor(gridColor);
                    g2d.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2d.setColor(Color.BLACK);
                    String xLabel = i + "";
                    FontMetrics metrics = g2d.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2d.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2d.drawLine(x0, y0, x1, y1);
            }
        }

        g2d.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2d.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(lineColor);
        g2d.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(pointColor);
        for (Point p : graphPoints) {
            int x = p.x - pointWidth / 2;
            int y = p.y - pointWidth / 2;
            if (!minPoints.contains(p)) {
            	g2d.fillOval(x, y, pointWidth, pointWidth);
            } else {
            	g2d.setColor(minPointsColor);
            	g2d.fillOval(x, y - 2, pointWidth + 2, pointWidth + 2);
                g2d.setColor(pointColor);
            }
        }
    }
}