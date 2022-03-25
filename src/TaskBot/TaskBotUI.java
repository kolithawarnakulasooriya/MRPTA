package TaskBot;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import test.Tutorial3;

import java.awt.*;
import javax.swing.*;

public class TaskBotUI extends GUIState {

	private static final long serialVersionUID = 1;
	
	public Display2D display;
    public JFrame displayFrame;
    
    ContinuousPortrayal2D continuousPortrayal2D = new ContinuousPortrayal2D();
    SparseGridPortrayal2D dronePortrayal = new SparseGridPortrayal2D();
	
	public TaskBotUI(SimState state) {
		super(state);
		// TODO Auto-generated constructor stub
	}
	
	
	public void init(Controller c) {
	    super.init(c);
	    
	    // Make the Display2D.  We'll have it display stuff later.
	    display = new Display2D(400,400,this); // at 400x400, we've got 4x4 per array position
	    displayFrame = display.createFrame();
	    c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
	    displayFrame.setVisible(true);
	    displayFrame.setTitle(getName());
	
	    // specify the backdrop color  -- what gets painted behind the displays
	    display.setBackdrop(Color.white);
	    
	    display.attach(continuousPortrayal2D, "Targets");
	    display.attach(dronePortrayal, "Drones");
	
    }
	
	public void quit(){
	    super.quit();
	    
	    if (displayFrame!=null) displayFrame.dispose();
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
    	continuousPortrayal2D.setPortrayalForAll(new sim.portrayal.simple.OvalPortrayal2D(Color.blue, 2.0));
    	
    	dronePortrayal.setField(((TaskPlane)state).drones);
    	dronePortrayal.setPortrayalForAll(new sim.portrayal.simple.ImagePortrayal2D(this.getClass(), "drone.png", 4));

               
	    // reschedule the displayer
	    display.reset();
            
	    // redraw the display
	    display.repaint();
    }
	
	public static void main(String []args) {
		new TaskBotUI(new TaskPlane(System.currentTimeMillis())).createController();
	}
	
}

  
  
  
  
  
