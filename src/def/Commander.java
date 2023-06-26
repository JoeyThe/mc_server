import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
//import org.json.simple.JSONObject;
//import javax.json.Json;
//import javax.json.JsonObject;
//import java.util.HashMap;

// Exceptions
import java.io.IOException;

/**
 * This is the Commander class that does the heavy lifting for commands
 * @author josep
 *
 */

public class Commander {
        // Attributes	
	public RconClient rc;			// RCON client object
	private static final String NOT_A_COMMAND  = "NOT_A_COMMAND";
	private static final String FAILED_COMMAND = "FAILED_COMMAND";
	private static final String[] VALID_CUSTOM_COMMANDS = new String[] {
		"testMsg",
		"testCmd",
		"logCoords",
		"end"
	};
	private String target;			// Target user for commands
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
	
	private static final String WORKING_CMDR_DIR = System.getProperty("user.dir")+"/commander"; 

	public Commander(RconClient rc) {
		// Initialize RCON client and authenticate with server
		this.rc = rc;
                String pwrd = "SeVeN_mc_890";
		// SERVERDATA_AUTH triggers authentication method in RCON client class
		if (rc.authenticateClient(pwrd)) {
			Logger.log("Authentication successful!");
		}
		else {
			Logger.log("Authentication failed.");
		}
	}

	public String processCommand(String line) {
		// Set Target
		parseTargetAndSet(line);
		// Parse for custom command
		String[] cmdOnly = line.split("%%", 2)[1].split(" ", 2);
		String parsedCmd = cmdOnly[0];
		String parsedArguments = "";
		if (cmdOnly.length > 1) {
			parsedArguments = cmdOnly[1];
		}
		// Check if custom command, if not send an error
		if (verifyValidCommand(parsedCmd)) {
			Logger.log("Player "+getTarget()+" sending valid custom command: "+parsedCmd);
			switch (parsedCmd) {
				// logCoords:  	Player sends this command to log their current X/Y/Z coordinates to their playerdata/<target>/special_coords file.
				//		Player can optionally supply a "note" argument that will be saved with the coordinates (e.g. description of
				//		the environment, a warning for other players, something silly). Coordinates are saved in the special_coords
				//		file as: Id, X/Y/Z coordinates, note. 
				case "logCoords":
					return cmdLogCoords(parsedArguments);
				case "end":
					return cmdEnd(parsedArguments);
				default:
					return "default";
			}
			// Can I just add sending the command packet send and response here? Depends on if all of my
			// custom commands are going to send a command
			// Maybe not because a command like logCoords will need to do stuff with the response, which
			// should be caught in the switch statement
			// Leaving them in for now... Looks wrong though
		}
		else {
			// Rethinking about how I want to handle non-custom commands...
			// I have decided that all server commands can be handled by custom commands
			Logger.log("ERROR: "+parsedCmd+" is not a valid custom comand.");
			return NOT_A_COMMAND;
		}
	}

	public boolean verifyValidCommand(String potentialCmd) {
		return Arrays.stream(VALID_CUSTOM_COMMANDS).anyMatch(potentialCmd::equals);
	}

	// Custom commands
	public String cmdLogCoords(String parsedArguments) {
		Logger.log("Logging "+getTarget()+"'s position data.");
		String serverCmdText = "data get entity "+getTarget()+" Pos";
		String respBody = rconSendAndGet(serverCmdText);
		// To be implemented: if response comes back with failed command, do something
		// Get coordinates from body string, add notes if provided
		String coordStr = String.join(" ", respBody.split("\\[",2)[1].split("\\]",2)[0]);
		if (parsedArguments != "") {
			coordStr = coordStr + ", note: " + parsedArguments;
		}
		// Create directory for target specific data dir
		String targetDataDirStr = WORKING_CMDR_DIR+"/../serverdata/playerdata/"+getTarget();
		File targetDataDirFile 	= new File(targetDataDirStr);
		Path specialCoords 	= FileSystems.getDefault().getPath(targetDataDirStr, "special_coords");
		// Check if target data dir exists; if it doesn't, create it
		if (!targetDataDirFile.exists()) {
			Logger.log("Dir creating resulted in "+targetDataDirFile.mkdirs());
		}
		try {
			Files.write(specialCoords, (coordStr+"\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		catch (IOException e){
			e.printStackTrace();
			return FAILED_COMMAND;
		}
		return respBody;
	}

	public String cmdEnd(String parsedArguments) {
		Logger.log("Stopping server.");
		String serverCmdText = "stop";
		String respBody = rconSendAndGet(serverCmdText);
		Logger.log(respBody);
		// Fix this
		return "stop";
	}

	// RCON send command and get response
	public String rconSendAndGet(String serverCmdText) {
		// Send RCON command
		rc.sendPacket(RconClient.SERVERDATA_EXECCOMMAND, serverCmdText);
		// Get response
		String respBody = rc.readPacket();
		// Checks to see if command worked
		return respBody;
	}
	
	// Target gets and sets
	public String getTarget() {
		// Get target name, set target text color
		// Hmmm not sure if I like this.. It is good for printing but when I want to use the target name in a command,
		// it might not like the ANSI coloring...
		//return ANSI_PURPLE+target+ANSI_RESET;
		return target;
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

