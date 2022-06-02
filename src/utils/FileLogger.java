package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;

public class FileLogger {
	
	private final static String directoryName = "Logs";
	public static boolean summaryOnly = false;
	
	public static PrintStream setPrintStreamAsFile(String filename, boolean shouldLogInFile) {
		if(!shouldLogInFile)
			return null;
		try {
			PrintStream stream = new PrintStream(new File(filename));
			if(stream != null) {
				System.setOut(stream);
			}
			return stream;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void println(String text) {
		if(!summaryOnly)
			System.out.println(text);
	}
	
	public static void printlnSum(String text) {
		System.out.println(text);
	}
	
	@SuppressWarnings("deprecation")
	public static String getLogFileName(String method) {
		File dir = new File(directoryName);
		if(!dir.exists()) {
			dir.mkdir();
		}
		return directoryName + "/"+method+"-log-"+new Date().toLocaleString()+".txt";
	}
}
