package TaskBot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import sim.app.particles3d.Particle;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;

public class TaskPlane extends SimState {
	
	public int areaWidth = 100;
	public int areaHeight = 100;
	public int numberOfTargets = 0;
	public int numberOfRobots = 0;
	
	public static final int TARGET_LIMIT = 15;
	public static final int DRONES_LIMIT = 5;
	
	public Continuous2D targetTasks;
	public SparseGrid2D drones;

	public TaskPlane(long seed) {
		super(seed);
	}
	
	public void start() {
		super.start();
		
		targetTasks = new Continuous2D(1.0, areaWidth, areaHeight);
		drones = new SparseGrid2D(areaWidth, areaHeight);
		
		this.generateTasks();
		this.generateDroneRobots();
		
		System.out.println("-------------------------------------------------------------");
		
	}

	private void generateDroneRobots() {
		
		numberOfRobots = random.nextInt(DRONES_LIMIT)+1;
		
		for(int i=0;i< numberOfRobots;i++) {
			DroneRobot dr = new DroneRobot(50, 90, TargetTypes.TYPE_1, 0, DroneRobot.getRandomSpeed(10,20));
			drones.setObjectLocation(dr, dr.getLocation());
		}
	}

	private List<TargetTask> fixPrioritoes(List<TargetTask> list, int totalPriorities) {
		
		List<TargetTask> taskList = new ArrayList<TargetTask>();
		
		for(int i=0;i<list.size();i++) {
			TargetTask t = list.get(i);
			t.setPriority(t.getPriority()/totalPriorities);
			taskList.add(t);
		}
		
		return taskList;
		
	}

	private void generateTasks() {
		
		numberOfTargets = random.nextInt(TARGET_LIMIT)+1;
		
		List<TargetTask> taskList = new ArrayList<TargetTask>();
		
		int totalPriorities = 0;
		
		for(int i=0; i< numberOfTargets;i++) {
			int priority = random.nextInt(100);
			totalPriorities += priority;
			TargetTask t = new TargetTask(random.nextInt(areaWidth), random.nextInt(areaHeight-20), TargetTypes.TYPE_1, priority);
			taskList.add(t);
		}
		taskList = fixPrioritoes(taskList, totalPriorities);
		Collections.sort(taskList, new SortTask());
		
		double totalPriorityWeight = 0;
		for(int i=0;i<taskList.size();i++) {
			TargetTask ts = taskList.get(i);
			targetTasks.setObjectLocation(ts, ts.getLocationDouble2D());
			totalPriorityWeight += ts.priority;
			System.out.println(ts.toString());
		}
		
		System.out.println("Total Priority: "+ new DecimalFormat("###.##").format(totalPriorityWeight));
		
	}

	private static final long serialVersionUID = 1;
	
	public static void main(String[] args) {
		doLoop(TaskPlane.class, args);
		System.exit(0);
	}

}
