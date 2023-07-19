import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.Float;
import java.io.BufferedReader;
import java.io.FileReader;
//import org.json.simple.JSONObject;
//import javax.json.Json;
//import javax.json.JsonObject;
//import java.util.HashMap;

// Exceptions
import java.io.IOException;
import java.lang.SecurityException;
import java.io.FileNotFoundException;
import java.lang.NumberFormatException;

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
		"showCoords",
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
	
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);

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
	
	/**
	 * Hehe
	 */
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
				case "logCoords":
					return cmdLogCoords(parsedArguments);
				case "showCoords":
					return cmdShowCoords(parsedArguments);
				case "end":
					return cmdEnd(parsedArguments);
				default:
					return "default";
			}
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
	
	/**
	 * Custom command<br>
	 * Player sends this command to log their current X/Y/Z coordinates to their playerdata/\<target\>/special_coords file.<br>
	 * Player can optionally supply a "note" argument that will be saved with the coordinates (e.g. description of<br>
	 * the environment, a warning for other players, something silly). Coordinates are saved in the special_coords<br>
	 * file as: Id, X/Y/Z coordinates, note. 
	 */
	public String cmdLogCoords(String parsedArguments) {
		Logger.log("Logging "+getTarget()+"'s position data.");
		// Send rcon command to get player's position data
		String serverCmdText = "data get entity "+getTarget()+" Pos";
		String respBody = rconSendAndGet(serverCmdText);
		// TODO: If response comes back with failed command, do something
		// Create directory for target specific data dir
		String targetDataDirStr = WORKING_CMDR_DIR+"/../serverdata/playerdata/"+getTarget();
		File targetDataDirFile 	= new File(targetDataDirStr);
		Path specialCoords 	= FileSystems.getDefault().getPath(targetDataDirStr, "special_coords");
		// ID and bool for first entry
		int id = 0;
		boolean firstEntry = false;
		// Check if target data dir exists; if it doesn't, create it
		if (!targetDataDirFile.exists()) {
			try {
				targetDataDirFile.mkdirs();
				Logger.log(targetDataDirStr+" dir successfully created.");
			}
			// Catch security expection and return failed command
			catch (SecurityException se) {
				Logger.log("Player data dir failed to be created. Error:\n"+stackTraceToString(se));
				return FAILED_COMMAND;
			}		
		}
		// If special coords file does not exist for user, create it
		if (!Files.exists(specialCoords)) {
			firstEntry = true;
			Logger.log("Creating special coords file for "+getTarget());
			try {
				Files.createFile(specialCoords);
			}
			// Throws too many exceptions, just catch them all
			catch (Exception e) {
				Logger.log("Special coord file failed to be created. Error:\n"+stackTraceToString(e));
				return FAILED_COMMAND;
			}
		}
		// Get latest coord id from file and increment
		if (!firstEntry) {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(specialCoords.toString()));
			}
			catch (FileNotFoundException e) {
				Logger.log("Special coord file failed to be found. Error:\n"+stackTraceToString(e));
				return FAILED_COMMAND;
			}
			String tempLine = "";
			String line = "";
			try {
				while(tempLine != null) {
					line = tempLine;
					tempLine = reader.readLine();
				}			
				reader.close();
				// Get last id and increment 
				try {
					id = Integer.parseInt(line.split(",",2)[0])+1;
				} 
				catch (NumberFormatException e){
					Logger.log("Failed to read number from line. Probably written wrong. Error:\n"+stackTraceToString(e));
					return FAILED_COMMAND;
				}
			} catch (IOException e) {
				Logger.log("Failed to read line or close reader. Error:\n"+stackTraceToString(e));
				return FAILED_COMMAND;
			}
		}
		// Get coordinates from body string, only keep tens decimal place, add id and notes 
		String coordStr = "";
		for (String s : String.join(" ", respBody.split("\\[",2)[1].split("\\]",2)[0]).split(",",3)) {
			coordStr += String.format("%.1f, ", (float) Float.parseFloat(s.substring(0, s.length()-1)));
		}
		coordStr = id + ", " + coordStr + "Note: " + parsedArguments;
		// Write coordinates to file
		try {
			Files.write(specialCoords, (coordStr+"\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			rconSendDirMsg("Coords successfully logged!");
		}
		catch (IOException e){
			// Catch IOException and return failed command
			Logger.log("Error writing coords to file. Error:\n"+stackTraceToString(e));
			return FAILED_COMMAND;
		}
		return respBody;
	}
	
	/**
	 * Custom command<br>
	 * Shows the player the last ten locations of their special coords file as a personal hover event message<br>
	 */
	public String cmdShowCoords(String parsedArguments) {
		Logger.log("Displaying coords for "+getTarget());
		// Access targeted player's special coords file
		String targetDataDirStr = WORKING_CMDR_DIR+"/../serverdata/playerdata/"+getTarget();
		Path specialCoords = FileSystems.getDefault().getPath(targetDataDirStr, "special_coords");
		// Check to see if the file exists
		String respBody = "";
		if (!Files.exists(specialCoords)) {
			Logger.log("Special coords file for "+getTarget()+" does not exist");
			rconSendDirMsg("You do not have a special coords file. Run the command 'logCoords' to generate one.");
			return respBody;
		}
		// If it exists, display contents in message with hover event
		try {
			String coords = Files.readString(specialCoords).replace("\n","\\n");
			// Send rcon command to send raw json hover message to specified player
			String serverCmdText = "tellraw "+getTarget()+" {\"text\":\"Hover for coords\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""+coords+"\"}}";
			respBody = rconSendAndGet(serverCmdText);
		} catch (IOException e) {
			Logger.log("Special coord file failed to be read. Error:\n"+stackTraceToString(e));
			return FAILED_COMMAND;
		}
		return respBody;
	}

	/**
	 * Custom command<br>
	 * Terminates the server
	 */
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
	
	// RCON send message to target
	public void rconSendDirMsg(String msg) {
		String serverCmdText = "tellraw "+getTarget()+" {\"text\":\""+msg+"\"}";
		rconSendAndGet(serverCmdText);
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
	
	public String stackTraceToString(Exception e) {
		e.printStackTrace(pw);
		return sw.toString();
	}
		

	public void parseTargetAndSet(String line) {
		// Get target name from the line that is passed
		String[] targetStrArray = line.split("<",2)[1].split(">",2);
		setTarget(targetStrArray[0]);
	}
}

