package TaskBot;

import java.util.Comparator;

public class SortTask implements Comparator<TargetTask> {

	@Override
	public int compare(TargetTask o1, TargetTask o2) {
		// TODO Auto-generated method stub
		return Double.compare(o2.priority, o1.priority);
	}
	
}