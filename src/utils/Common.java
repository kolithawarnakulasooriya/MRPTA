package utils;

import java.util.List;
import java.util.Random;

import enums.SharingMethods;

public class Common {
	
	private static final double GÜREL_BOUNDARY = 11;

	public static int generatePriority(int bound, SharingMethods m) {
		if(m == SharingMethods.GÜREL) {
			// random priorities over 0 - 10
			new Random().nextDouble(GÜREL_BOUNDARY);
		}
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

	public static double calculateKendallsCorrelationTopK(List<Integer> arr1, List<Integer> arr2, double k) {
		
		int cc=0;
		
		k = k > (double)arr1.size() ? (double)arr1.size() : k;
		
		for(int indexOfArr1=0; (double)indexOfArr1< k; indexOfArr1++) {
			int indexOfArr2 = arr2.indexOf(arr1.get(indexOfArr1));
			
			// check the index2 is ahead of index 1
			if(indexOfArr2 != -1 && indexOfArr2 <= indexOfArr1) {
				cc += 0;
			}else {
				cc+= 1;
			}
		}
		
		double kt = Math.abs((double)(cc)/k);
		return kt;
	}
	
	public static double calculateKendallsCorrelation(List<Integer> arr1, List<Integer> arr2) {
		
		double cc=0, dc =0;
		
		for(int i=0; i< arr1.size(); i++) {
			for(int j= i; j < arr2.size(); j++) {
				if(arr2.get(j) <= arr2.get(i)) {
					
					int a = i - arr1.indexOf(arr2.get(j));
					int b = arr2.indexOf(arr1.get(i)) - j;
					
					if(a * b < 0)
						dc++;
					else 
						cc++;
				}
			}
		}
		return Math.abs((cc - dc)/(cc+dc));
	}
	
	public static double getGurelControlParameter() {
		return 0.2;
	}

}
