package TaskBot;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import core.Assessment;
import core.DroneRobot;
import core.Location;
import core.TargetTask;
import core.TargetTaskList;
import enums.SharingMethods;
import enums.TargetTypes;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import utils.ChartEva;
import utils.Common;
import utils.FileLogger;
import utils.SortTask;

public class TaskPlane extends SimState {
	
	private static final long serialVersionUID = 1;
	
	private static final SharingMethods METHOD = SharingMethods.GÜREL;
	
	/*
	 * Configurations
	 */
	private static final boolean SHOULD_LOG_IN_FILE = true;
	private static final boolean SHOW_NUMBER_OF_ITERATION_CHART = false;
	
	
	private static final double MAXIMUM_TARGET_FIELD_COVERAGE_PERCENTAGE = 15.0;
	private static final boolean VISUALIZE_ROBOTS = false;
	
	public static final boolean ARE_ROBOTS_HAVE_SAME_SPEED = true;
	public static final boolean SHOW_COMPARISON_CHART = false;
	public static final boolean TRAILS_OFF = true;
	public static final boolean KEEP_PAST_TRAILS_ON_THE_SIMULATION_WINDOW = false;
	
	// Exposed
	public static final int TASK_PLANE_AREA_WIDTH = 400;
	public static final int TASK_PLANE_AREA_HEIGHT = 400;
	private static final int TASK_PLANE_MARGINAL_WIDTH = 10;
	private static final int TASK_PLANE_MARGINAL_HEIGHT = 30;
	private static final int PRIORITY_BOUNDARY = 100;
	private static final boolean SET_RANDOM_TARGET_TYPE = false;
	
	private static final int MAXIMUM_DRONE_LIMIT = 50;
	private static final int MAXIMUM_TARGET_LIMIT = 100;
	private static final int INCREMENT_TARGET_COUNT = 10;
	private static final int INCREMENT_DRONE_COUNT = 10;
	
	public static int actualTaskPlaneAreaWidth = TASK_PLANE_AREA_WIDTH-TASK_PLANE_MARGINAL_WIDTH;
	public static int actualTaskPlaneAreaHeight = TASK_PLANE_AREA_HEIGHT-TASK_PLANE_MARGINAL_HEIGHT;

	public int numberOfTargetsTasks = 0;
	public int numberOfDroneRobots = 0;
	
	public static int[] targetRange = {10, 20};
	public static int[] droneRange = {5, 10};
	
	public static int minTargetLimit = targetRange[0];
	public static int maxTargetLimit = targetRange[1];
	public static int minDroneLimit = droneRange[0];
	public static int maxDroneLimit = droneRange[1];
	
	private double SAFE_RANGE = 2.0;
	
	// settings
	boolean isPermittedToRunSimulationContineously = true;
	boolean isPermittedToContineouslyRunSimulationsForAStory = true;
	
	public DoubleGrid2D trails;
	public Continuous2D targetTasks;
	public SparseGrid2D targetDrones;
	
	private List<DroneRobot> drones;
	
	public List<Integer> priorityArray = new ArrayList<>();
	public List<Integer> completedArray = new ArrayList<>();
	public List<Integer> alreadyFoundTargetsList = new ArrayList<>();
	
	long simulationStartTime = 0, simulationEndTime = 0, programmeStartTimeStamp =0;
	
	public boolean [][] taskPlaneTaskAssignmentMatrix;
	int totalNumberOfCompletedTaskCount = 0;
	int totalNumberOfPossibleTasks =0;
	int trialNumber = 0;
	
	double sumNonModifiedKT =0;
	double sumKLengthListKT =0;
	double sumPearsonssCorrelations =0;
	double sumSimulationDurations =0;
	double sumStdValues =0;
	
	PrintStream stream = null;
	
	public static double targetFieldCoverPercentage = 0.0;

	public TaskPlane(long seed) {
		super(seed);
		
		FileLogger.setPrintStreamAsFile(FileLogger.getLogFileName(this.METHOD.name()), SHOULD_LOG_IN_FILE);
		FileLogger.summaryOnly = false;
		
		// initialize program start time
		programmeStartTimeStamp = System.nanoTime();
	}
	
	public void start() {
		super.start();
		
		trails = new DoubleGrid2D(TASK_PLANE_AREA_WIDTH, TASK_PLANE_AREA_HEIGHT);
		targetTasks = new Continuous2D(1.0, TASK_PLANE_AREA_WIDTH, TASK_PLANE_AREA_HEIGHT);
		targetDrones = new SparseGrid2D(TASK_PLANE_AREA_WIDTH, TASK_PLANE_AREA_HEIGHT);
		
		taskPlaneTaskAssignmentMatrix = new boolean[actualTaskPlaneAreaWidth][actualTaskPlaneAreaHeight];
		totalNumberOfPossibleTasks = (actualTaskPlaneAreaWidth)*(actualTaskPlaneAreaHeight);
		
		this.startTaskSharing();
	}
	
	public void startTaskSharing() {
		
		trialNumber += 1;
		
		FileLogger.println("I="+(trialNumber)+" DR ["+minDroneLimit+"-"+maxDroneLimit+"] TR ["+minTargetLimit+"-"+maxTargetLimit+"] ");
		
		try {
			// initial phase
			this.generateTasks();
			this.generateDroneRobots();
			
			// first and second stages
			this.calculateStatusAssessments();
			
			// third stage
			this.announceEstimates();
			
			// fourth stage
			this.signInContracts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void evaluateAssessmentErrors(int id) {
		if(this.checkAllCompleted() == 0 && isAllDroneReachedToEndLocations(id)) {
			
			simulationEndTime = System.nanoTime();
			
			double allDistances = 0;
			int []regressionAnalysis = new int[priorityArray.size()];
			for(int i=0;i<priorityArray.size();i++) {
				regressionAnalysis[i] = (int)completedArray.indexOf(priorityArray.get(i));
				double euclidianDistance = Math.pow((((double)(i))-regressionAnalysis[i]), 2.0);
				allDistances += euclidianDistance;
			}
			double meanIndexError = Math.sqrt(allDistances)/priorityArray.size();
			
			if(!isPermittedToRunSimulationContineously && SHOW_COMPARISON_CHART)
				ChartEva.showChart(regressionAnalysis, numberOfDroneRobots+" Robots Over "+numberOfTargetsTasks+" Tasks ");
			
			double[] tmpDoublePriorityArray = priorityArray.stream().mapToDouble(value -> (double)value).toArray();
			double[] tmpDoubleCompletedArray = completedArray.stream().mapToDouble(value -> (double)value).toArray();

			double KT_1 = Math.abs(new KendallsCorrelation().correlation(tmpDoublePriorityArray, tmpDoubleCompletedArray));
			
			double topK = maxTargetLimit * 1 ;
			//double KT_2 = Common.calculateKendallsCorrelationTopK(priorityArray, completedArray, topK);
			double KT_2 = Common.calculateKendallsCorrelation(priorityArray, completedArray);
			double PC_1 = Math.abs(new PearsonsCorrelation().correlation(tmpDoublePriorityArray, tmpDoubleCompletedArray));
			double simulationDuration = schedule.getTime();// (simulationEndTime - simulationStartTime)/1000000;
			
			sumNonModifiedKT += KT_1;
			sumKLengthListKT += KT_2;
			sumPearsonssCorrelations += PC_1;
			sumSimulationDurations += simulationDuration;
			
			targetFieldCoverPercentage  = ((double)totalNumberOfCompletedTaskCount * 100.0)/(double)totalNumberOfPossibleTasks;
			
			FileLogger.printlnSum("----------- Trial ("+trialNumber+") ------------");
			
			printList(priorityArray, "Expected Order Of Tasks: ");
			printList(completedArray, "Completed Order Of Tasks: ");
			
			FileLogger.println("Simulation Duration= "+ simulationDuration + " ms ");
			FileLogger.println("L2 Distance= "+ meanIndexError);
			
			FileLogger.println("KenDalls Tau = "+ KT_1);
			FileLogger.println("KenDalls Tau Error (Top K length lists)= "+ KT_2);
			FileLogger.println("Pearson's Correlation= "+ PC_1);
			FileLogger.println("Target Field Cover Percentage= "+targetFieldCoverPercentage+"%");
			
			if (targetFieldCoverPercentage > MAXIMUM_TARGET_FIELD_COVERAGE_PERCENTAGE) {
				
				StringBuilder summeryMessage = new StringBuilder("\n");
				summeryMessage.append("-------------------------- Summary ----------------------\n");
				summeryMessage.append("Method"+ METHOD.name());
				summeryMessage.append("Total Number of Drones: ");
				summeryMessage.append(" ["+minDroneLimit+"-"+maxDroneLimit+"]");
				summeryMessage.append('\n');
				summeryMessage.append("Target Tasks Allocation Range For One Simulation: ");
				summeryMessage.append("["+minTargetLimit+"-"+maxTargetLimit+"]");
				summeryMessage.append('\n');
				summeryMessage.append("Total Number of Target Tasks: ");
				summeryMessage.append(totalNumberOfCompletedTaskCount);
				summeryMessage.append('\n');
				summeryMessage.append("Total Number of Trial Simulations: ");
				summeryMessage.append(trialNumber);
				summeryMessage.append('\n');
				
				summeryMessage.append("Mean KenDalls Tau for "+ trialNumber +" Trials : ");
				summeryMessage.append(sumNonModifiedKT/(double)trialNumber);
				summeryMessage.append('\n');
				
				summeryMessage.append("Mean KenDalls Tau Error (Top K length lists) for "+ trialNumber +" Trials : ");
				summeryMessage.append(sumKLengthListKT/(double)trialNumber);
				summeryMessage.append('\n');
				
				summeryMessage.append("Mean Pearson's correlation for "+ trialNumber +" Trials : ");
				summeryMessage.append(sumPearsonssCorrelations/(double)trialNumber);
				summeryMessage.append('\n');
				
				summeryMessage.append("Mean Simulation Execution Time for "+ trialNumber +" Trials : ");
				summeryMessage.append(sumSimulationDurations/trialNumber);
				summeryMessage.append('\n');
				
				summeryMessage.append("Mean STD for "+ trialNumber +" Trials : ");
				summeryMessage.append(sumStdValues/(double)trialNumber);
				summeryMessage.append('\n');
				
				FileLogger.printlnSum(summeryMessage.toString());
				
				if(SHOW_NUMBER_OF_ITERATION_CHART) {
					ChartEva.showPercentChart(alreadyFoundTargetsList.stream().mapToInt(i->i).toArray(), "Search iterations for finding unassigned locations");
				}
				
				isPermittedToRunSimulationContineously = false;
				
				if(isPermittedToContineouslyRunSimulationsForAStory) {
					reArrangeSimulationField();
				}
				
			}
			
			System.gc();
			
			if(isPermittedToRunSimulationContineously) {
				this.targetDrones.clear();
				if(!KEEP_PAST_TRAILS_ON_THE_SIMULATION_WINDOW) {
					this.targetTasks.clear();
					this.trails.setTo(new DoubleGrid2D(TASK_PLANE_AREA_WIDTH, TASK_PLANE_AREA_HEIGHT));
				}
				schedule.clear();
				schedule.reset();
				this.startTaskSharing();
			}else {
				this.kill();
			}
		}
	}
	
	private int checkAllCompleted() {
		int incompleted = 0;
		for (Object obj : this.targetTasks.allObjects.toArray()) {
			if(!((TargetTask)obj).isCompleted) {
				incompleted++;
			}
		}
		return incompleted;
	}
	
	private boolean isAllDroneReachedToEndLocations(int id) {
		for(DroneRobot dr : drones) {
			if(dr.getId() != id && dr.getTaskList().size() > 0) {
				return false;
			}
		}
		return true;
	}
	
	private boolean[] initializeBooleanArray(boolean [] inputArray,int size){
		
		if(inputArray != null) {
			boolean unAssignedRobotFound = false;
			for (boolean b : inputArray) {
				if(b == false) {
					unAssignedRobotFound = true;
					break;
				}
			}
			
			if(unAssignedRobotFound) {
				return inputArray;
			}
		}
		
		boolean [] tmpArray = new boolean[size];
		Arrays.fill(tmpArray, false);
		return tmpArray;
	}

	private void reArrangeSimulationField() {
		if(maxDroneLimit < MAXIMUM_DRONE_LIMIT || maxTargetLimit < MAXIMUM_TARGET_LIMIT) {
			if(maxTargetLimit < MAXIMUM_TARGET_LIMIT) {
				maxTargetLimit += INCREMENT_TARGET_COUNT;
				minTargetLimit += INCREMENT_TARGET_COUNT;
			}else {
				maxDroneLimit += INCREMENT_DRONE_COUNT;
				minDroneLimit += INCREMENT_DRONE_COUNT;
				maxTargetLimit = maxDroneLimit + INCREMENT_TARGET_COUNT;
				minTargetLimit = maxDroneLimit;
				
			}
			isPermittedToRunSimulationContineously = true;
		    totalNumberOfCompletedTaskCount = 0;
			totalNumberOfPossibleTasks =0;
			trialNumber = 0;
			
			sumNonModifiedKT =0;
			sumKLengthListKT =0;
			sumSimulationDurations =0;
			sumPearsonssCorrelations =0;
			targetFieldCoverPercentage = 0.0;
			sumStdValues=0.0;
			taskPlaneTaskAssignmentMatrix = new boolean[TASK_PLANE_AREA_WIDTH-10][TASK_PLANE_AREA_HEIGHT-30];
			totalNumberOfPossibleTasks = (TASK_PLANE_AREA_WIDTH-10)*(TASK_PLANE_AREA_HEIGHT-30);
			
			FileLogger.setPrintStreamAsFile(FileLogger.getLogFileName(this.METHOD.name()), SHOULD_LOG_IN_FILE);
			
			this.start();
		}else {
			FileLogger.println("Simulation Completed");
			System.out.print("Total duration: "+ ((simulationEndTime = System.nanoTime() - programmeStartTimeStamp)/1000000000) + " S ");
			this.kill();
		}
		
	}
	
	private void signInContracts() {
		// Award robots
		drones.forEach(drone -> {
			drone.addTaskToTargetTaskList(new TargetTask(10001, drone.getLocation().x, drone.getLocation().y, TargetTypes.END, 0));
			drone.addTaskToTargetTaskList(new TargetTask(10001, drone.getLocation().x, drone.getLocation().y, TargetTypes.END, 0));
			targetDrones.setObjectLocation(drone, drone.getLocation());
			schedule.scheduleRepeating(drone);
		});
		showDroneAssignments();
		simulationStartTime = System.nanoTime();
	}
	
	private void announceEstimates() throws CloneNotSupportedException{
		if(METHOD == SharingMethods.GÜREL) {
			announceEstimatesGurelsMethod();
		} 
		else if(METHOD == SharingMethods.OUR_NP) {
			announceEstimatesOurMethodWithoutIdle();
		} 
		else {
			announceEstimatesOurMethod();
		}
	}

	private void announceEstimatesOurMethod() throws CloneNotSupportedException {
		
		Bag tmpTargetTasks = (Bag) this.targetTasks.getAllObjects().clone();
		List<DroneRobot> tmpDrones = new ArrayList<>();
		tmpDrones.addAll(this.drones);
		
		boolean []idleRobotMetrix = null;
	
		FileLogger.println("Assigning Tasks To Robots ----");
		while(!tmpTargetTasks.isEmpty()) {
			
			idleRobotMetrix = initializeBooleanArray(idleRobotMetrix, tmpDrones.size());
			
			TargetTask nextTask = (TargetTask)tmpTargetTasks.pop();
			FileLogger.println(nextTask.toString());
			
			if(nextTask.isCompleted) {
				continue;
			}
			
			DroneRobot selectedDroneRobot = this.assignRobot(nextTask, tmpDrones, idleRobotMetrix);
			
			if(selectedDroneRobot != null) {
				tmpDrones.get(selectedDroneRobot.getId() -1).addTaskToTargetTaskList(nextTask);
				idleRobotMetrix[selectedDroneRobot.getId() -1] = true;
			}
		}
		
	}
	
	private void announceEstimatesOurMethodWithoutIdle() throws CloneNotSupportedException {
		
		Bag tmpTargetTasks = (Bag) this.targetTasks.getAllObjects().clone();
		List<DroneRobot> tmpDrones = new ArrayList<>();
		tmpDrones.addAll(this.drones);
		
		FileLogger.println("Assigning Tasks To Robots ----");
		while(!tmpTargetTasks.isEmpty()) {
			
			TargetTask nextTask = (TargetTask)tmpTargetTasks.pop();
			FileLogger.println(nextTask.toString());
			
			if(nextTask.isCompleted) {
				continue;
			}
			
			Assessment minimumModifiedAssessment = null;
			DroneRobot selectedDrone = null;
			
			for(int i=0;i< tmpDrones.size(); i++) {
				DroneRobot tempSelectedDroneRobot = tmpDrones.get(i);
				Assessment tmpAssessment = tempSelectedDroneRobot.getTotalAssessmentToNextTarget(nextTask);
				
				// get lowest DSP value with assigned robot
				if(minimumModifiedAssessment == null && tmpAssessment.isWinBid() ) {
					minimumModifiedAssessment = tmpAssessment;
					selectedDrone = tempSelectedDroneRobot;
				} else if(tmpAssessment.isWinBid() && tempSelectedDroneRobot.getTotalAssessmentToNextTarget(nextTask).compare(tmpAssessment)){
					minimumModifiedAssessment = tmpAssessment;
					selectedDrone = tempSelectedDroneRobot;
				}else {
					continue;
				}
			}
			
			if(selectedDrone != null) {
				tmpDrones.get(selectedDrone.getId() -1).addTaskToTargetTaskList(nextTask);
			}
		}
		
	}
	
	private void announceEstimatesGurelsMethod() throws CloneNotSupportedException {
		
		Bag tmpTargetTasks = (Bag) this.targetTasks.getAllObjects().clone();
		List<DroneRobot> tmpDrones = new ArrayList<>();
		tmpDrones.addAll(this.drones);
		
		FileLogger.println("Assigning Tasks To Robots ----");

		// step 1
		// Set mobile robot current node as the starting node
		// current starting node for all drones are set as initial nodes

		while(!tmpTargetTasks.isEmpty()) {
			
			// select lowest modified Modified DSPLength for each drone
			double minimumModifiedDSPLength = Double.MAX_VALUE;
			TargetTask nextTask = (TargetTask)tmpTargetTasks.pop();
			DroneRobot selectedDrone = null;
			
			for(int i=0;i< tmpDrones.size(); i++) {
				DroneRobot selectedTempDrone = tmpDrones.get(i);
				
				// here we do not check the availability of the drones, we just share tasks. because mobile robots are mostly contains enough power
				TargetTask startTask = null;
				if(selectedTempDrone.getTaskList().size() >0 ) {
					startTask= selectedTempDrone.getTaskList().get(0);
				}else {
					startTask = new TargetTask(0, selectedTempDrone.getLocation().getX(), selectedTempDrone.getLocation().getY(), TargetTypes.INIT, 0);
				}
				
				Assessment as = selectedTempDrone.getAssessment(startTask, nextTask);
				
				// get lowest DSP value with assigned robot
				if(as.getStraightLineDistance() < minimumModifiedDSPLength) {
					minimumModifiedDSPLength = as.getStraightLineDistance();
					selectedDrone = selectedTempDrone;
				}
			}
			
			if(selectedDrone != null) {
				tmpDrones.get(selectedDrone.getId() -1).addTaskToTargetTaskList(nextTask);
			}
		}
		
	}
	
	
	private DroneRobot assignRobot(TargetTask inputTargetTask, List<DroneRobot> inputDronesList, boolean[] inputIdleRobotMetrix) {
		
		DroneRobot tempSelectedDroneRobot = null;
		
		List<DroneRobot> tmpDroneList= new ArrayList<>();
		
		// get Idle robots
		for (DroneRobot droneRobot : inputDronesList) {
			if(droneRobot.isIdle()) {
				tmpDroneList.add(droneRobot);
			}
		}
		
		if(tmpDroneList.isEmpty()) {
			tmpDroneList = inputDronesList;
		}
		
		for (DroneRobot tempDroneRobot : inputDronesList) {
			Assessment tmpAssessment = tempDroneRobot.getTotalAssessmentWithoutReturnTrip(inputTargetTask);
			
			if(inputIdleRobotMetrix[tempDroneRobot.getId() -1] == true) {
				continue;
			}else if(tempSelectedDroneRobot == null && tmpAssessment.isWinBid()) {
				tempSelectedDroneRobot = tempDroneRobot;
			} else if(tmpAssessment.isWinBid() && tempSelectedDroneRobot.getTotalAssessmentWithoutReturnTrip(inputTargetTask).compare(tmpAssessment)) {
				tempSelectedDroneRobot = tempDroneRobot;
			}else {
				continue;
			}
		}
		
		return tempSelectedDroneRobot;
	}

	// Helper functions

	private void calculateStatusAssessments() {
		
		ArrayList<TargetTask> targetTaskObjectArray = getTargetTaskArrayFromContinuous2D(this.targetTasks);

		drones.forEach(obj -> {
			DroneRobot tempDrone = (DroneRobot)obj;
			tempDrone.calculateStatusAssessments(targetTaskObjectArray, METHOD);
		});
	}

	
	private void generateDroneRobots() {
		this.drones = new ArrayList<>();
		numberOfDroneRobots = Common.generateRandom(maxDroneLimit, minDroneLimit);

		FileLogger.println("Drone Robots ----");
		for(int index=0;index<numberOfDroneRobots;index++) {
			this.drones.add(new DroneRobot(
					index + 1, 
					new Location(Common.getPositionValue(index, TASK_PLANE_AREA_WIDTH, numberOfDroneRobots, VISUALIZE_ROBOTS ? 10 : 0),
					TASK_PLANE_AREA_HEIGHT-TASK_PLANE_MARGINAL_HEIGHT), 
					TargetTypes.getTargetType(SET_RANDOM_TARGET_TYPE), 
					0, 
					DroneRobot.getRandomSpeed(1,5))
			);
			FileLogger.println(this.drones.get(index).toString());
		}
	}
	
	private void generateTasks() {
		
		numberOfTargetsTasks = Common.generateRandom(maxTargetLimit, minTargetLimit);
		TargetTaskList targetTaskList = generatePossibleTaskList();
		
		if(SHOW_NUMBER_OF_ITERATION_CHART) {
			int maxSelectedTargetIndex = (int)this.targetFieldCoverPercentage;
			if(alreadyFoundTargetsList.size() != maxSelectedTargetIndex+1) {
				alreadyFoundTargetsList.add(targetTaskList.getNumberOfTaskLocationsAlreadyInTheTaskPlane());
			}else {
				alreadyFoundTargetsList.set(
						maxSelectedTargetIndex, 
						( alreadyFoundTargetsList.get(maxSelectedTargetIndex) + targetTaskList.getNumberOfTaskLocationsAlreadyInTheTaskPlane() )
					);
			}
		}
		
		targetTaskList = this.fixPrioritoes(targetTaskList);
		Collections.sort(targetTaskList.getListOfTasks(), new SortTask());
		
		// reset task set arrays
		this.priorityArray = new ArrayList<>();
		this.completedArray = new ArrayList<>();
		
		for(int targetTaskIndex=targetTaskList.getListOfTasks().size()-1;targetTaskIndex>=0;targetTaskIndex--) {
			TargetTask targetTask = targetTaskList.getListOfTasks().get(targetTaskIndex);
			targetTasks.setObjectLocation(targetTask, targetTask.getLocationDouble2D());
			priorityArray.add(targetTask.id);
		}
		Collections.reverse(priorityArray);
	}
	
	// Helper functions
	
	
	private TargetTaskList generatePossibleTaskList() {
		
		int targetTaskIndex= 0;
		int totalPrioryValue = 0;
		int numberOfTaskLocationsAlreadyInTheTaskPlane = 0;
		List<TargetTask> tempListOfTasks = new ArrayList<TargetTask>();
		
		while(targetTaskIndex< numberOfTargetsTasks) {
					
				// local x location and y location
				Location selectedLocation = new Location(random.nextInt(actualTaskPlaneAreaWidth), random.nextInt(actualTaskPlaneAreaHeight));
				boolean isSelectedLocationAlreadyInSelectedInTheTaskPlane = this.isLocationAlreadyAssigned(tempListOfTasks, selectedLocation);
				int taskPriorityValue = 0;
				TargetTask task = null;
				
				if(!isSelectedLocationAlreadyInSelectedInTheTaskPlane) {
					++totalNumberOfCompletedTaskCount;
					taskPlaneTaskAssignmentMatrix[selectedLocation.getX_location()][selectedLocation.getY_location()] = true;
				}
				
				if(isSelectedLocationAlreadyInSelectedInTheTaskPlane) {
					if(SHOW_NUMBER_OF_ITERATION_CHART) {
						numberOfTaskLocationsAlreadyInTheTaskPlane++;
					}
					continue;
				}
				
				taskPriorityValue = Common.generatePriority(PRIORITY_BOUNDARY, METHOD);
				totalPrioryValue += taskPriorityValue;
				
				task = new TargetTask(
						(targetTaskIndex + 1),
						selectedLocation,
						TargetTypes.getTargetType(SET_RANDOM_TARGET_TYPE),
						taskPriorityValue
				);
				targetTaskIndex++;
				tempListOfTasks.add(task);
				
		}
		
		return new TargetTaskList(tempListOfTasks, totalPrioryValue, numberOfTaskLocationsAlreadyInTheTaskPlane);
		
	}

	private boolean isLocationAlreadyAssigned(List<TargetTask> tempListOfTasks, Location l) {
		// check the location l is within the safe range of another location
		
		if(SAFE_RANGE > 0) {
			for(int targetTaskIndex=0;targetTaskIndex<tempListOfTasks.size();targetTaskIndex++) {
				if(tempListOfTasks.get(targetTaskIndex).calculateDistance(l.getX_location(), l.getY_location())<SAFE_RANGE) {
					return true;
				}
			}
		}
		
		if(taskPlaneTaskAssignmentMatrix[l.getX_location()][l.getY_location()] == true) {
			return true;
		}
		
		return false;
	}

	
	private TargetTaskList fixPrioritoes(TargetTaskList unfixedTaskList) {
		
		TargetTaskList fixedTaskList = new TargetTaskList(new ArrayList<>(), unfixedTaskList.getTotalPrioryValue(), unfixedTaskList.getNumberOfTaskLocationsAlreadyInTheTaskPlane());
		fixedTaskList.getListOfTasks().removeAll(fixedTaskList.getListOfTasks());
		
		for(int i=0;i<unfixedTaskList.getListOfTasks().size();i++) {
			TargetTask targetTask = unfixedTaskList.getListOfTasks().get(i);
			
			if(METHOD == SharingMethods.GÜREL) {
				targetTask.setPriority((targetTask.getPriority()));
			}else {
				targetTask.setPriority((targetTask.getPriority()/unfixedTaskList.getTotalPrioryValue()));
			}
			fixedTaskList.getListOfTasks().add(targetTask);
		}
		
		return fixedTaskList;
		
	}
	
	private ArrayList<TargetTask> getTargetTaskArrayFromContinuous2D(Continuous2D continuous2DTaskObject) {
		ArrayList<TargetTask> tempTargetTaskArray = new ArrayList<>();
		for (Object obj : this.targetTasks.allObjects.toArray()) {
			TargetTask tempTargetTask = (TargetTask)obj;
			if(!tempTargetTask.isCompleted) {
				tempTargetTaskArray.add(tempTargetTask);
			}
		}
		return tempTargetTaskArray;
	}

	// end helper functions
	
	// Visualization functions
	
	private void showDroneAssignments() {
		for (DroneRobot d : this.drones) {
			FileLogger.println(d.getParthPlanAsString());
		}
		// task distribution should be uniform distribution
		
		double[] distributionData = new double[this.drones.size()];
		
		for(int i=0;i< this.drones.size() ;i++) {
			distributionData[i] = this.drones.get(i).getTaskList().size();
		}
		double std = new StandardDeviation().evaluate(distributionData);
		FileLogger.println("Task Distibutiorn [x= robot id][y = task count] : "+Arrays.toString(distributionData));
		FileLogger.println("Task Distribution SDT = "+ std);
		sumStdValues += std;
	}
	
	private void printList(List<Integer> priorityArray2, String title) {
		FileLogger.println(title +" ["+Arrays.toString(priorityArray2.toArray())+"]");
	}
	
	// end VF
	
	public static void main(String[] args) {
		
		doLoop(TaskPlane.class, args);
		System.exit(0);
	}

}
