package utils;

import java.util.Comparator;

import core.TargetTask;

public class SortTask implements Comparator<TargetTask> {

	@Override
	public int compare(TargetTask o1, TargetTask o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.priority, o2.priority);
	}
	
}