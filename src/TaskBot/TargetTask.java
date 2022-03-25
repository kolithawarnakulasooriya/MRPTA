package TaskBot;

import java.text.DecimalFormat;

import sim.util.Double2D;

public class TargetTask {
	public int x;
	public int y;
	public TargetTypes f;
	public double priority=1;
	
	public double getPriority() {
		return priority;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}
	
	public Double2D getLocationDouble2D() {
		return new Double2D(this.x, this.y);
	}

	public TargetTask(int x, int y, TargetTypes f, int p) {
		this.x=x;
		this.y=y;
		this.f= f;
		this.priority = p;
	}
	
	public String toString() {
		return "x: "+this.x +"| y: "+this.y+"| type: "+this.f.name()+"| priority: "+ new DecimalFormat("###.##").format(this.priority);
	}
	
}
