package TaskBot;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import java.awt.*;
import javax.swing.*;

public class TaskBotUI extends GUIState {

	private static final double DISPLAY_WIDTH = 400;
	private static final double DISPLAY_HEIGHT = 400;
	
	public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D continuousPortrayal2D = new ContinuousPortrayal2D();
    SparseGridPortrayal2D dronePortrayal = new SparseGridPortrayal2D();
    FastValueGridPortrayal2D trailsPortrayal = new FastValueGridPortrayal2D("Trail");
	
	public TaskBotUI(SimState state) {
		super(state);
	}
	
	@Override
	public void finish() {
		super.finish();
	}
	
	public void init(Controller c) {
	    super.init(c);
	    
	    display = new Display2D(DISPLAY_WIDTH,DISPLAY_HEIGHT,this);
	    
	    displayFrame = display.createFrame();
	    c.registerFrame(displayFrame);  
	   // displayFrame.setVisible(true);
	    displayFrame.setTitle(getName());
	    display.setBackdrop(Color.white);
	    display.attach(trailsPortrayal,"Trails");
	    display.attach(continuousPortrayal2D, "Targets");
	    display.attach(dronePortrayal, "Drones");
	    
    }
	
	public void quit(){
	    super.quit();
	    
	    if (displayFrame!=null) 
	    	displayFrame.dispose();
	    
	    displayFrame = null;
	    display = null;
    }
	
    public static String getName() { return "Priority Based Task Allocation"; }
    
    public void start(){
    	super.start();
    	setupPortrayals();
    }

    public void load(SimState state){
    	super.load(state);
    	setupPortrayals();
    }
    
    public void setupPortrayals(){
    
    	continuousPortrayal2D.setField(((TaskPlane)state).targetTasks);
    	trailsPortrayal.setField(((TaskPlane)state).trails);
    	trailsPortrayal.setMap(
                new sim.util.gui.SimpleColorMap(
                    0.0,1.0,Color.white,Color.black));
    	dronePortrayal.setField(((TaskPlane)state).targetDrones);
    	dronePortrayal.setPortrayalForAll(new sim.portrayal.simple.ImagePortrayal2D(this.getClass(), "/imgs/drone.png", 8));

               
	    // reschedule the displayer
	    display.reset();
            
	    // redraw the display
	    display.repaint();
	   
    }
}

  
  
  
  
  
