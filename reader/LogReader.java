package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import logger.Logger;
import processor.Processor;
import rconclient.RconClient;
import commander.Commander;

public class LogReader {
	// Attributes
	public Processor serverProcessor;	// Server processor 
	public RconClient rc;			// RCON client object
	public String path;			// Path to log file
	private String target;			// Target user for commands
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
	public LogReader(Processor serverProcessor, RconClient rc) {
		// Initialize server processor
		this.serverProcessor = serverProcessor;
		// Set path
		this.path = serverProcessor.getLogFileName();
		// Initialize RCON client and authenticate with server
		this.rc = rc;
                String cmd = "SeVeN_mc_890";
                rc.sendPacket(rc.SERVERDATA_AUTH, cmd);
                byte[] resp1 = new byte[rc.MAX_RESP_SIZE];
                resp1 = rc.readPacket(resp1);
	}

	public void continuousRead() {
		// Start continuous read of the server log file
		// Leaving this loop means killing the server
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
					Logger.log(line);
					if (line.contains("%stop")) {
						Logger.log("Supreme Overlord Josep Birardini has ended the program.");
						rc.sendPacket(rc.SERVERDATA_EXECCOMMAND, "stop");	
						break;
					}
					if (line.contains("shit")) {
						parseTargetAndSet(line);
						Logger.log("KICKING PLAYER "+getTarget()+" FOR SAYING \"shit\"");
					}
					// Check if the line contains the command key character
					if (line.contains("%")) {
						Commander.parseCommand(line);
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

	public void parseTargetAndSet(String line) {
		// Get target name from the line that is passed
		String[] targetStrArray = line.split("<",2)[1].split(">",2);
		setTarget(targetStrArray[0]);
	}
}

