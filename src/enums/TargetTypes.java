package enums;

import java.util.Random;

public enum TargetTypes {
	INIT, TYPE_1, TYPE_2, END;
	
	public static TargetTypes getTargetType(boolean isRandom) {
		
		if(!isRandom)
			return TYPE_1;
		
		int randSelector = new Random().nextInt(1);
		
		switch(randSelector) {
			case 1: return TYPE_2;
			default: return TYPE_1;
		}
	}
}
