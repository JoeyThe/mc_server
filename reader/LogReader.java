package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import logger.Logger;
import processor.Processor;
import commander.Commander;

public class LogReader {
	// Attributes
	public Processor serverProcessor;	// Server processor 
	public Commander commander;		// Commander
	public String path;			// Path to log file
	private boolean testRead = false;
	
	// Constructor
	public LogReader(Processor serverProcessor, Commander commander) {
		// Initialize commander
		this.commander =  commander;
		// Initialize server processor
		this.serverProcessor = serverProcessor;
		// Set path
		this.path = serverProcessor.getLogFileName();
	}

	public void continuousRead() {
		// Start continuous read of the server log file
		// Leaving this loop means killing the server
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.path));
			String line = reader.readLine();
			boolean server_run = true;
			while (server_run) {
				if (testRead == true) {
					line = reader.readLine();
					if (line == null) {
						testRead = false;
					}
					continue;
				}
				if (line != null) {
					Logger.log(line);
					// Check if the line contains the command key character
					if (line.contains("%%")) {
						// Process command; it will return a string, right now it is only looking for "stop"
						// but I can make it look for and respond to other returns
						if (commander.processCommand(line) == "stop") {
							Logger.log("Supreme Overlord Josep Birardini has ended the program.");
							server_run = false;
						}
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
}

