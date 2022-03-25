package TaskBot;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.Random;

import sim.app.asteroids.Element;
import sim.util.Int2D;

public class DroneRobot extends Element {

	public int x;
	public int y;
	public TargetTypes f;
	public int startAngle;
	public int speed;
	
	public DroneRobot(int x, int y, TargetTypes f, int startAngle, int speed) {
		this.x=x;
		this.y=y;
		this.f=f;
		this.startAngle=startAngle;
		this.speed=speed;
		
		GeneralPath gp = new GeneralPath();
        gp.moveTo(-2,-2);
        gp.lineTo(2,0);
        gp.lineTo(-2,2);
        gp.lineTo(2, 0);
        gp.closePath();
        shape = gp;
	}

	public static int getRandomSpeed(int min, int max) {
		// TODO Auto-generated method stub
		return new Random().nextInt(max-min)+min;
	}
	
	public Int2D getLocation() {
		return new Int2D(this.x, this.y);
	}
	
	public Color getColor() { return Color.green; }
}
