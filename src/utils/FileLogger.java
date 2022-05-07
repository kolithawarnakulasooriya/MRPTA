package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;

public class FileLogger {
	
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
	
	public static void Println(String text) {
		System.out.println(text);
	}
	
	@SuppressWarnings("deprecation")
	public static String getLogFileName() {
		return "log"+new Date().toLocaleString()+".txt";
	}
}
