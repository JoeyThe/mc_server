package commander;

import processor.Processor;

public class Commander {
        // Attributes
	public Processor serverProcessor; // Process for the server        

        // Constructor
        public Commander(Processor serverProcessor) {
                System.out.println("COMMANDER");
		this.serverProcessor = serverProcessor;

        }

	public boolean sendCommand(String command) {
		boolean command_success = false;
		System.out.println("Command: "+command);
		// Code to send the command through some process that the server can interpret
		// If the process works, set command_success to true
		command_success = true;
		return command_success;
	}
 
}

