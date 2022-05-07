package utils;

import org.jfree.chart.ChartPanel;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartEva extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private XYDataset dataset;
	private JFreeChart chart;
	private ChartPanel panel;
	
	private static ChartEva ce = null;

	public ChartEva( String chartTitle, int[] dataArray ) {
		
	}

	private XYDataset createDataset(int[] dataArray) {
		
		XYSeriesCollection dataset = new XYSeriesCollection();
    
		XYSeries series = new XYSeries("");  
        
        for (int i =0; i< dataArray.length; i++) {
			series.add(i, dataArray[i]);
		}
 
        dataset.addSeries(series);
        return dataset;
	}

	public static void showChart(int [] u, String title) {
		  ce = new ChartEva(title,u);
		  ce.dataset = ce.createDataset(u);
	    // Create chart
	    ce.chart = ChartFactory.createScatterPlot(
	    	title,
	        "Expected Order", // X-Axis Label
	        "Actual Order", // Y-Axis Label
	        ce.dataset
	        );
	    

	    ce.panel = new ChartPanel(ce.chart);
	    ce.setContentPane(ce.panel);
	      ce.setAlwaysOnTop(true);
	      ce.pack();
	      ce.setSize(600, 400);
	      ce.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	      ce.setVisible(true);
	}
	
	public static void showPercentChart(int [] u, String title) {
		
		ce = new ChartEva(title,u);
		  ce.dataset = ce.createDataset(u);
	    // Create chart
	    ce.chart = ChartFactory.createScatterPlot(
	    	title,
	        "Filed access percentage", // X-Axis Label
	        "Number of Iterations", // Y-Axis Label
	        ce.dataset
	        );
	    

	    ce.panel = new ChartPanel(ce.chart);
	    ce.setContentPane(ce.panel);
	  
	  XYPlot p = (XYPlot) ce.chart.getPlot();
	  
	  NumberAxis domain = (NumberAxis)p.getDomainAxis();
      domain.setRange(1, 100);
      domain.setTickUnit(new NumberTickUnit(10));
      domain.setVerticalTickLabels(true);
      
      ce.setAlwaysOnTop(true);
      ce.pack();
      ce.setSize(600, 400);
      ce.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      ce.setVisible(true);
	      
	}
}
