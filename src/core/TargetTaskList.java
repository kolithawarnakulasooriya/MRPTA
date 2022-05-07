package core;

import java.util.List;

public class TargetTaskList {
	
	private int totalPrioryValue = 0;
	private int numberOfTaskLocationsAlreadyInTheTaskPlane = 0;
	private List<TargetTask> listOfTasks;

	public void setListOfTasks(List<TargetTask> listOfTasks) {
		this.listOfTasks = listOfTasks;
	}

	public int getTotalPrioryValue() {
		return totalPrioryValue;
	}

	public int getNumberOfTaskLocationsAlreadyInTheTaskPlane() {
		return numberOfTaskLocationsAlreadyInTheTaskPlane;
	}

	public List<TargetTask> getListOfTasks() {
		return listOfTasks;
	}
	
	public TargetTaskList(List<TargetTask> taskList, int totalPrioryValue, int allocatedTaskCount) {
		this.listOfTasks = taskList;
		this.totalPrioryValue = totalPrioryValue;
		this.numberOfTaskLocationsAlreadyInTheTaskPlane = allocatedTaskCount;
	}

}
