package utils;

import java.util.List;
import java.util.Random;

public class Common {
	
	public static int generatePriority(int bound) {
		return new Random().nextInt(bound);
	}
	
	public static int generateRandom(int max, int min) {
		if(max == min)
			return max;
		return new Random().nextInt(max - min) + min;
	}
	
	public static int getPositionValue(int steps, int maxArea, int maxNumberOfLocations, int spaceBetweenTwoLocations) {
		int startPosition = (maxArea/2) - (maxNumberOfLocations * spaceBetweenTwoLocations)/2;
		return startPosition + (steps * spaceBetweenTwoLocations);
	}

	public static double calculateKendallsCorrelation(List<Integer> arr1, List<Integer> arr2) {
		
		int cc=0;
		
		for(int indexOfArr1=0; indexOfArr1< arr1.size(); indexOfArr1++) {
			int indexOfArr2 = arr2.indexOf(arr1.get(indexOfArr1));
			
			// check the index2 is ahead of index 1
			if(indexOfArr2 <= indexOfArr1) {
				cc += 0;
			}else {
				cc+= 1;
			}
		}
		
		double kt = Math.abs((double)(cc)/(double)(arr1.size()));
		return kt;
	}

}
