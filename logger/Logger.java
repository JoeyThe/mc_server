package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	// Attributes
	

	// Constructor
	public Logger() {
		// Does anything need to happen in here?

	}

	public static void log(String msg) {
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy'T'HH:mm:ss.SSS'Z'")) + "] " + msg);
	}
}

