package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import commander.Commander;

public class LogReader {
	// Attributes
	public Commander commander;	// Commander that we are passing in
	public String path;		// Path to log file
	private String target;		// Target user for commands
	private boolean testRead = false;
	// Color attributes
	public static final String ANSI_RESET 	= "\u001B[0m";
	public static final String ANSI_BLACK 	= "\u001B[30m";
	public static final String ANSI_RED 	= "\u001B[31m";
	public static final String ANSI_GREEN 	= "\u001B[32m";
	public static final String ANSI_YELLOW 	= "\u001B[33m";
	public static final String ANSI_BLUE 	= "\u001B[34m";
	public static final String ANSI_PURPLE 	= "\u001B[35m";
	public static final String ANSI_CYAN 	= "\u001B[36m";
	public static final String ANSI_WHITE 	= "\u001B[37m";
	
	// Constructor
	public LogReader(String path, Commander commander) {
		this.commander 	= commander;
		this.path 	= path;
		this.path = commander.serverProcessor.getLogFileName();
		System.out.println("Path: "+path);
	}

	public void continuousRead() {
		// Start continuous read of the server log file
		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader(this.path));
			String line = reader.readLine();
			while (true) {
				if (testRead == true) {
					line = reader.readLine();
					if (line == null) {
						testRead = false;
					}
					continue;
				}
				if (line != null) {
					System.out.println(line);
					if (line.contains("%quit")) {
						// Some command for grabbing the target name from the log line
						//commander.send_command("/kick "+target)
						System.out.println("Supreme Overlord Josep Birardini has ended the program.");
						commander.sendCommand("%TEST COMMAND");
						break;
					}
					if (line.contains("shit")) {
						getTargetFromLogLine(line);
						commander.sendCommand("%TEST COMMAND");
						System.out.println("KICKING PLAYER "+getTarget()+" FOR SAYING \"shit\"");
					}
				}
				line = reader.readLine();
				try {
						Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getTarget() {
		// Get target name, set target text color
		return ANSI_PURPLE+target+ANSI_RESET;
	}

	public void setTarget(String target) {
		// Set target name
		this.target = target;
	}

	public void getTargetFromLogLine(String line) {
		// Get target name from the line that is passed
		String[] targetStrArray = line.split("<",2)[1].split(">",2);
		setTarget(targetStrArray[0]);
	}
}

