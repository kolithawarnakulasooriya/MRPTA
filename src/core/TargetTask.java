package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import enums.TargetTypes;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

public class TargetTask extends SimplePortrayal2D {
	private static final long serialVersionUID = 1L;
	public int id;
	public int x;
	public int y;
	public TargetTypes f;
	public double priority=1;
	public boolean isCompleted = false;
	
	Font font = new Font("SansSerif", Font.BOLD, 8);
	
	public double getPriority() {
		return priority;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}
	
	public Double2D getLocationDouble2D() {
		return new Double2D(this.x, this.y);
	}
	
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
		graphics.setFont(font);
        graphics.setColor(isCompleted ? Color.red : Color.gray);
     
	    graphics.fillOval((int)(info.draw.x),(int)(info.draw.y),6,6);
	    graphics.setColor( Color.blue );
	    graphics.drawString(String.valueOf(id), (int)(info.draw.x),(int)(info.draw.y) );
    }

	public TargetTask(int id, int x, int y, TargetTypes f, int p) {
		this.id=id;
		this.x=x;
		this.y=y;
		this.f= f;
		this.priority = p;
	}
	
	public TargetTask(int id, Location l, TargetTypes f, int p) {
		this.id=id;
		this.x=l.x_location;
		this.y=l.y_location;
		this.f= f;
		this.priority = p;
	}
	
	public String toString() {
		return "id: " +this.id + "| x: "+this.x +"| y: "+this.y+"| type: "+this.f.name()+"| priority: "+ new DecimalFormat("###.##").format(this.priority);
	}
	
	public double calculateDistance(int x, int y) {
		return Math.sqrt(Math.pow(Math.abs(this.x - x), 2.0) + Math.pow(Math.abs(this.y - y), 2.0));
	}
}
