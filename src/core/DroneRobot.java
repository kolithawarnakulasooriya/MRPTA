package core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import TaskBot.TaskPlane;
import enums.TargetTypes;
import sim.app.asteroids.Element;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class DroneRobot extends Element implements Steppable {

	private static final long serialVersionUID = 1L;
	private static final double SCALE_COEFFICIENT = 0.02 * 1000 ; // 1 pixel is 1m
	private static final double FLYING_COST_FOR_ONE_TIME_UNIT = 0.1;
	private static final double TARGET_TASK_EXECUTION_RANGE = 4.0; 
	private static final double MAXIMIM_FLYING_COST = 1000000.0; 
	
	private int id, x, y, startAngle, speed;

	private TargetTypes f;
	private TargetTask robotStartingLocation;
	
	private Assessment[][] assessmentMetrix;
	private List<TargetTask> taskList;

	private int currentTargetTaskindex = 0;
	private TargetTask nextTask = null;
	
	
	public List<TargetTask> getTaskList() {
		return taskList;
	}
	
	public int getId() {
		return id;
	}
	
	public DroneRobot(int id, int x, int y, TargetTypes f, int startAngle, int speed) {
		this.id=id;
		this.x=x;
		this.y=y;
		this.f=f;
		this.startAngle=startAngle;
		this.speed=speed;
	}
	
	public DroneRobot(int id, Location l, TargetTypes f, int startAngle, int speed) {
		this.id=id;
		this.x=l.x_location;
		this.y=l.y_location;
		this.f=f;
		this.startAngle=startAngle;
		this.speed=speed;
	}
	
	public boolean isIdle() {
		return taskList == null || this.taskList.isEmpty();
	}

	public static int getRandomSpeed(int min, int max) {
		// TODO Auto-generated method stub
		return new Random().nextInt(max-min)+min;
	}
	
	public Int2D getLocation() {
		return new Int2D(this.x, this.y);
	}
	
	public Color getColor() { return Color.green; }
	
	public Assessment [][] calculateStatusAssessments(ArrayList<TargetTask> inputTargetTasksList) {
		
		this.assessmentMetrix = new Assessment[inputTargetTasksList.size()+1][inputTargetTasksList.size()];
		
		// set initial position task
		this.robotStartingLocation = new TargetTask(0, this.x, this.y, TargetTypes.INIT, 0);
		
		// calculate assessments from initial location to other task locations
		for(int j =0;j<inputTargetTasksList.size();j++) {
			TargetTask robotNextTask = inputTargetTasksList.get(j);
			this.assessmentMetrix[0][robotNextTask.id-1] = this.calculateAssessment(robotStartingLocation,robotNextTask);
		}
		
		// rest of the possible starting locations
		for(int i=0;i< inputTargetTasksList.size();i++) {
			TargetTask robotTaskStart = inputTargetTasksList.get(i);
			for(int j =0;j<inputTargetTasksList.size();j++) {
				TargetTask robotTaskEnd = inputTargetTasksList.get(j);
				this.assessmentMetrix[robotTaskStart.id][robotTaskEnd.id-1] = this.calculateAssessment(robotTaskStart,robotTaskEnd);
			}
		}
		
		return this.assessmentMetrix;
	}
	
	private Assessment calculateAssessment(TargetTask t1, TargetTask t2) {
		
		if(this.f != t2.f) {
			return new Assessment(t1, t2, -1, 0, false); 
		}
		
		double straightLineDistance = Math.sqrt(Math.pow(t2.y - t1.y, 2) + Math.pow(t2.x - t1.x, 2)) * SCALE_COEFFICIENT;
		double timeConstraint = straightLineDistance / (double)this.speed;
		double energyConsumption = timeConstraint / FLYING_COST_FOR_ONE_TIME_UNIT;
		return new Assessment(t1, t2, timeConstraint, energyConsumption, true);
	}

	public void addTaskToTargetTaskList(TargetTask inputTask) {
		if(this.taskList == null) {
			this.taskList = new ArrayList<TargetTask>();
		}
		this.taskList.add(inputTask);
	}
	
	public Assessment getTotalAssessmentToNextTarget(TargetTask inputNextTargetTask) {
		
		double sumOfTimeConstraints = 0;
		double sumOfEnergyConsumption = 0;
		
		int nextTargetTaskId = inputNextTargetTask.id;
		
		if(!this.isIdle()) {
			nextTargetTaskId = this.taskList.get(0).id;
		}
		
		
		// initial position to first level location
		sumOfTimeConstraints += this.assessmentMetrix[0][nextTargetTaskId - 1].getTimeConstraint();
		sumOfEnergyConsumption += this.assessmentMetrix[0][nextTargetTaskId - 1].getEnergyconsumption();
		
		if(!this.isIdle()) {
			// calculate the total of assigned next targets
			if(this.taskList.size()>1) {
				for(int index =1; index< taskList.size(); index++) {
					TargetTask tmpTask = taskList.get(index);
					sumOfTimeConstraints += this.assessmentMetrix[nextTargetTaskId][ tmpTask.id - 1].getTimeConstraint();
					sumOfEnergyConsumption += this.assessmentMetrix[nextTargetTaskId][ tmpTask.id - 1].getEnergyconsumption();
					
					nextTargetTaskId = tmpTask.id;
				}
			}
			
			// calculate for new task
			sumOfTimeConstraints += this.assessmentMetrix[nextTargetTaskId][inputNextTargetTask.id - 1].getTimeConstraint();
			sumOfEnergyConsumption += this.assessmentMetrix[nextTargetTaskId][inputNextTargetTask.id - 1].getEnergyconsumption();
			
			nextTargetTaskId = inputNextTargetTask.id;
		}
		
		// return trip calculation
		sumOfTimeConstraints += this.assessmentMetrix[0][nextTargetTaskId - 1].getTimeConstraint();
		sumOfEnergyConsumption += this.assessmentMetrix[0][nextTargetTaskId - 1].getEnergyconsumption();

		return new Assessment(
				robotStartingLocation, 
				inputNextTargetTask, 
				sumOfTimeConstraints, 
				sumOfEnergyConsumption, 
				sumOfEnergyConsumption <= MAXIMIM_FLYING_COST);
		
	}
	
	public String toString() {
		return "id: "+ this.id +
				" x: "+ this.x +
				" y: "+this.y+
				" f: "+this.f+
				" speed: "+ this.speed+
				" angle: "+this.startAngle+
				" c_pos: "+currentTargetTaskindex;
	}
	
	public String getParthPlanAsString() {
		
		StringBuilder visualizer = new StringBuilder("["+this.id+"] : " +this.taskList.size()+" : ");
		this.taskList.forEach((TargetTask t) -> {
			visualizer.append(t.id + "("+t.x+","+t.y+")" + "->");
		});
		visualizer.append("END");
		return visualizer.toString();
	}
	
	public String printAssessmentMetrix() {
		return Arrays.deepToString(assessmentMetrix).replace("], ", "]\n");
	}
	
	public void showStatus() {
		System.out.println("Drone :("+this.id+")");
		System.out.println("--------------------");
		System.out.println(this.toString());
		System.out.println("--------------------");
		System.out.println("Status Assessments");
		System.out.println(this.printAssessmentMetrix());
	}

	public Assessment getAssessment(TargetTask t) {
		return assessmentMetrix[currentTargetTaskindex][t.id-1];
	}
	
	public void step(SimState state) {
		
		 TaskPlane tmpTaskPlane = (TaskPlane)state;
	     Int2D currentLocation = tmpTaskPlane.targetDrones.getObjectLocation(this);
	     
	     if(currentLocation == null) {
	    	 return;
	     }
	     
	     if(taskList.size() > 0) {
	    	 if(nextTask == null) {
	    		 nextTask = taskList.remove(0);
	    	 }
	    	 
	    	 double deltaX = nextTask.x - this.x;
	    	 double deltaY = nextTask.y - this.y;
	    	 
	    	 int itr = !TaskPlane.ARE_ROBOTS_HAVE_SAME_SPEED ? this.speed : 1;
	    	 
	    	 double m = 0.0;
	    	 double c = 0.0;
	    	 int xdir = 0;
	    	 int ydir = 0;
	    	 
	    	 if(Math.abs(deltaY) <= Math.abs(deltaX)) {
	    		 m = deltaY / (deltaX+0.0001);
	    		 c = this.y - m * this.x;
	    		 xdir = currentLocation.x + (deltaX < 0 ? (-(itr)) : itr);
	    		 ydir = (int) (m * xdir + c);
	    	 }else {
	    		 m = deltaX / (deltaY + 0.0001);
	    		 c = this.x - m * this.y;
	    		 ydir = currentLocation.y + (deltaY < 0 ? (-(itr)) : itr);
	    		 xdir = (int) (m * ydir + c);
	    	 }
	    	 
	    	 if(xdir > TaskPlane.TASK_PLANE_AREA_WIDTH) {
	    		 xdir = TaskPlane.TASK_PLANE_AREA_WIDTH;
	    	 }
	    	 if(xdir < 0) {
	    		 xdir = 0;
	    	 }
	    	 if(ydir > TaskPlane.actualTaskPlaneAreaHeight) {
	    		 ydir = TaskPlane.actualTaskPlaneAreaHeight;
	    	 }
	    	 if(ydir < 0) {
	    		 ydir = 0;
	    	 }
	    	 Int2D newloc = new Int2D(xdir,ydir);
	    	 
	    	 if(!TaskPlane.TRAILS_OFF 
	    			 && currentLocation.x < TaskPlane.TASK_PLANE_AREA_WIDTH 
	    			 && currentLocation.y < TaskPlane.actualTaskPlaneAreaHeight) {
	    		 tmpTaskPlane.trails.field[currentLocation.x][currentLocation.y] = 0.5;
	    	 }
	    	 tmpTaskPlane.targetDrones.setObjectLocation(this, newloc);
	    	 
	    	 if( isReachedToTheLocation(currentLocation, TARGET_TASK_EXECUTION_RANGE) ) {
	    		 
	    		 if(nextTask.id != 10001) {
	    			 tmpTaskPlane.completedArray.add(nextTask.id);
	    			 ((TargetTask)tmpTaskPlane.targetTasks.allObjects.get(tmpTaskPlane.targetTasks.getObjectIndex(nextTask))).isCompleted = true;
	    		 }else {
	    			 tmpTaskPlane.evaluateAssessmentErrors();
	    		 }
	    		 
	    		 nextTask = taskList.remove(0);
    		 
	    		 this.x = currentLocation.x;
	    		 this.y = currentLocation.y;
	    	 }
	     }
	}
	
	private boolean isReachedToTheLocation(Int2D currentLocation, double targetTaskExecutionRange) {
		return Math.sqrt(Math.pow(Math.abs(currentLocation.x - nextTask.x), 2.0) + Math.pow(Math.abs(currentLocation.y - nextTask.y), 2.0)) <= targetTaskExecutionRange;
	}
}
