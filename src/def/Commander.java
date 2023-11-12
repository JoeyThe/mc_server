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
import java.io.FileWriter;
import java.io.IOException;
import java.lang.SecurityException;
import java.io.FileNotFoundException;

/**
 * This is the Commander class that does the heavy lifting for commands
 * @author josep
 *
 */

public class Commander {
    // Attributes	
	public RconClient rc;			// RCON client object
	public CommandRefs cmdRefs;    	// Command refs for generating commands
	public String currCmd = ""; 	// Current executing command
	private static final String NOT_A_COMMAND  	= "NOT_A_COMMAND";
	private static final String FAILED_COMMAND 	= "FAILED_COMMAND";
	private static final int MAX_NOTE_LENGTH	= 50; 
	private static final String[] VALID_CUSTOM_COMMANDS = new String[] {
		"testMsg",
		"testCmd",
		"coords",
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
	private static final String PLAYERDATA_DIR	 = WORKING_CMDR_DIR+"/../serverdata/playerdata/";
	private static final String SPECIAL_COORDS 	 = "special_coords"; 
	
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
			currCmd = parsedCmd;
			switch (parsedCmd) {
				case "coords":
					return cmdCoords(parsedArguments);
				case "testCmd":
					return cmdTestCmd(parsedArguments);
				case "end":
					return cmdEnd(parsedArguments);
				default:
					return "default";
			}
		}
		else {
			Logger.log("ERROR: "+parsedCmd+" is not a valid custom comand.");
			rconSendDirMsg(parsedCmd+" is not a valid command! Send '%%cmdListCmds' for a list of valid commands.");
			return NOT_A_COMMAND;
		}
	}

	public boolean verifyValidCommand(String potentialCmd) {
		return Arrays.stream(VALID_CUSTOM_COMMANDS).anyMatch(potentialCmd::equals);
	}

	// Custom commands

	/**
	 * Custom command<br>
	 * Send a direct message to the targeted player with a list of valid commands.
	 */
	public String cmdListCmds(String parsedArguments) {
		//TODO: Implement
		return "hehe";
	}
	
	/**
	 * Custom command<br>
	 * Custom command that is read in from a file. Used for testing commands without having to restart server.
	 */
	public String cmdTestCmd(String parsedArguments) {
		// Read command in from file
		Path testCmd = FileSystems.getDefault().getPath(WORKING_CMDR_DIR, "/testCmd.txt");
		String respBody = NOT_A_COMMAND;
		String cmd;
		try {
			cmd = Files.readString(testCmd).replace("\n", "");
			respBody = rconSendAndGet(cmd);
			return respBody;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.log(e.toString());
		}

		return respBody;
	}
	
	/**
	 * Custom command<br>
	 * New implementation of coords commands. coords is the root command, followed by supplemental commands and arguments<br>
	 * based on what the user wants to do.
	 * Possible options are:<br>
	 * log: add coords with note parameter<br>
	 * Example: %%coords log Ice village<br>
	 * rm: remove existing coords based on ID<br>
	 * Example: %%coords rm 1 <remove coords with ID 1><br>
	 */
	public String cmdCoords(String parsedArguments) {
		// Switch statement for determining which command to use
		// Split the parsed arguments up
		String[] coordArgs = parsedArguments.split(" ",2);
		// If the resulting length of the coordArgs array is one, user did not provide an argument for the coords cmd... This may need to change
		if (coordArgs.length == 1) {
			Logger.log("Not enough arguments passed.");
			rconSendFailCmdMsg("Not enough arguments passed for coords command.");
			return FAILED_COMMAND;
		}
		switch (coordArgs[0]) {
			case "log":
				coordsLog(coordArgs[1]);
				break;
			case "rm":
				coordsRm(coordArgs[1]);
				break;
			case "":
				break;
			default:
				Logger.log("Coords command failed: Not a recognized coords command.");
				rconSendFailCmdMsg("Coords command failed. That is not a recognized coords command.");
				return FAILED_COMMAND;
		}
		return "Command passed";
	}
	
	/**
	 * coordsLogs: Command parameter for cmdCoords<br>
	 * Player sends this command to log their current X/Y/Z coordinates to their playerdata/target/special_coords file.<br>
	 * Player must supply a "note" argument that will be saved with the coordinates (e.g. description of<br>
	 * the environment, a warning for other players, something silly). Coordinates are saved in the special_coords<br>
	 * file as: Id, X/Y/Z coordinates, note. All coords are then written to palyer's special coords written book.<br>
	 * If the coord book already exists in the player's inventory, then replace the book with one that has the new coords.
	 */
	public String coordsLog(String note) {
		// Make sure that the notes are not over X characters long
		if (note.length() > MAX_NOTE_LENGTH) {
			Logger.log("Note for coords is too long.");
			rconSendFailCmdMsg("Note for coord is too long, please limit notes to "+MAX_NOTE_LENGTH+" chars.");
			return FAILED_COMMAND;
		}
		Logger.log("Logging "+getTarget()+"'s position data.");
		// Send rcon command to get player's position data
		String serverCmdText = "data get entity "+getTarget()+" Pos";
		String respBody = rconSendAndGet(serverCmdText);
		// TODO: If response comes back with failed command, do something
		// Create directory for target specific data dir
		String targetDataDirStr = PLAYERDATA_DIR + getTarget();
		File targetDataDirFile 	= new File(targetDataDirStr);
		Path specialCoords 	= FileSystems.getDefault().getPath(targetDataDirStr, SPECIAL_COORDS);
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
				rconSendFailCmdMsg("Player data dir could not be created. Please contact admin.");
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
				rconSendFailCmdMsg("Special coord file could not be created. Please contact admin.");
				return FAILED_COMMAND;
			}
		}
		// Get latest coord id from file and increment
		// get contents of file as string for generating coord book
		String contents = "";
		if (!firstEntry) {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(specialCoords.toString()));
			}
			catch (FileNotFoundException e) {
				Logger.log("Special coord file failed to be found. Error:\n"+stackTraceToString(e));
				rconSendFailCmdMsg("Special coord file could not be found. Please contact admin.");
				return FAILED_COMMAND;
			}
			String tempLine = "";
			String line = "";
			try {
				while((tempLine = reader.readLine()) != null) {
					line = tempLine;
				}			
				reader.close();
				// Check for case where the file has no entries (e.g. the player deleted all of them)
				// If first line is empty, don't do anything (id stays as 0)
				if (!line.trim().equals("")) {
					id = Integer.parseInt(line.split(",",2)[0])+1;
				}
			} catch (IOException e) {
				Logger.log("Failed to read line or close reader. Error:\n"+stackTraceToString(e));
				rconSendFailCmdMsg("Failed to read line or close player special coords files. Please contact admin.");
				return FAILED_COMMAND;
			}
		}
		// Get coordinates from body string, only keep tens decimal place, add id and notes 
		String coordStr = "";
		for (String s : String.join(" ", respBody.split("\\[",2)[1].split("\\]",2)[0]).split(",",3)) {
			coordStr += String.format("%.1f, ", (float) Float.parseFloat(s.substring(0, s.length()-1)));
		}
		coordStr = id + ", " + coordStr + "Note: " + note;
		// Write coordinates to file
		try {
			Files.write(specialCoords, (coordStr+"\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			rconSendDirMsg("Coords logged to player's unique file!");
			contents = Files.readString(specialCoords);
		}
		catch (IOException e){
			// Catch IOException and return failed command
			Logger.log("Error writing coords to file. Error:\n"+stackTraceToString(e));
			rconSendFailCmdMsg("Failed to write coords to player special coords file. Please contact admin.");
			return FAILED_COMMAND;
		}
		
		// Write coords to written book for the player
		// Check if the player already has the book in their inventory
		respBody = rconSendAndGet(CommandRefs.generateGetItemSlot("{id:\"minecraft:written_book\",tag:{Owner:\""+getTarget()+"\"}}", getTarget()));
		// If they don't have it, give it to them
		if (respBody.contains("Found no elements matching")) {
			respBody = rconSendAndGet(CommandRefs.generateGiveCoordBook(contents.replace("\n", "\\\\n"), getTarget()));
			rconSendDirMsg("Coords book generated!");
		}
		// Player has the book, replace their current one this a modified one
		else if (respBody.contains("has the following entity data: ")) {
			String slotStr  = respBody.split("has the following entity data: ",2)[1].replace("b",""); 
			respBody = rconSendAndGet(CommandRefs.generateModifyCoordBook(contents.replace("\n", "\\\\n"), getTarget(), slotStr));
			rconSendDirMsg("Coords book updated!");
		}
		// Player probably has multiple copies of the book (fucker)
		else {
			rconSendDirMsg("Coordinates were logged but the server cannot update the book because the player probably"+
						   " has too many copies of the book in their inventory. Get rid of extras please.");
		}
		return respBody;
	}
	
	/**
	 * coordsRm: Command parameter for cmdCoords<br>
	 * Player sends this command to delete a coordinate in their coords book. Player sends this parameter along with an<br>
	 * ID for the coordinate that they wish to remove. The coordinate is deleted from the coord book and a new book is generated<br>
	 * for the player.
	 */
	public String coordsRm(String id) {
		// Variables for player data file path
		String targetDataDirStr = PLAYERDATA_DIR + getTarget();
		Path specialCoords 	= FileSystems.getDefault().getPath(targetDataDirStr, SPECIAL_COORDS);
		// If special coords file does not exist for user, return fail
		if (!Files.exists(specialCoords)) {
			Logger.log("Special coord file does not exist for "+getTarget());
			rconSendFailCmdMsg("You do not have a special coords file, probably because you have not logged any coords."+
							   " If you think this is incorrect, please contact admin.");
			return FAILED_COMMAND;
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(specialCoords.toString()));
		}
		catch (FileNotFoundException e) {
			Logger.log("Special coord file failed to be found. Error:\n"+stackTraceToString(e));
			rconSendFailCmdMsg("Special coord file could not be found. Please contact admin.");
			return FAILED_COMMAND;
		}
		String line = "";
		String replaceContents = "";
		boolean idFound = false;
		// Read each line of file looking for the id that is to be deleted
		try {
			while((line = reader.readLine()) != null) {
				// If line does not contain id, add it to the replace string
				if (!line.split(",")[0].equals(id)) {
					replaceContents += line+"\n";
				}
				// If line does contain id, don't add it to replace string and set id found flag to true
				else {
					Logger.log("FOUND THE ID!");
					idFound = true;
				}
			}			
			reader.close();
		} catch (IOException e) {
			Logger.log("Failed to read line or close reader. Error:\n"+stackTraceToString(e));
			rconSendFailCmdMsg("Failed to read line or close player special coords files. Please contact admin.");
			return FAILED_COMMAND;
		}
		// If id was found, replace the special coords file contents with the new contents and send new book to player
		if (idFound) {
			FileWriter writer;
			try {
				Logger.log("Overwriting special coords file with new coords list.");
				rconSendDirMsg("Deleting coordinate with ID "+id+" from special coords file.");
				writer = new FileWriter(specialCoords.toString(), false);
				writer.write(replaceContents);
				writer.close();
			}
			catch (IOException e) {
				Logger.log("Special coord file failed to be found. Error:\n"+stackTraceToString(e));
				rconSendFailCmdMsg("Special coord file could not be found. Please contact admin.");
				return FAILED_COMMAND;
			}
			// Write coords to written book for the player
			// Check if the player already has the book in their inventory
			String respBody = rconSendAndGet(CommandRefs.generateGetItemSlot("{id:\"minecraft:written_book\",tag:{Owner:\""+getTarget()+"\"}}", getTarget()));
			// If they don't have it, give it to them
			if (respBody.contains("Found no elements matching")) {
				respBody = rconSendAndGet(CommandRefs.generateGiveCoordBook(replaceContents.replace("\n", "\\\\n"), getTarget()));
				rconSendDirMsg("Coords book generated!");
			}
			// Player has the book, replace their current one this a modified one
			else if (respBody.contains("has the following entity data: ")) {
				String slotStr = respBody.split("has the following entity data: ",2)[1].replace("b",""); 
				respBody = rconSendAndGet(CommandRefs.generateModifyCoordBook(replaceContents.replace("\n", "\\\\n"), getTarget(), slotStr));
				rconSendDirMsg("Coords book updated!");
			}
			// Player probably has multiple copies of the book (fucker)
			else {
				rconSendDirMsg("Desired coordinate was deleted but the server cannot update the book because the player probably"+
							   " has too many copies of the book in their inventory. Get rid of extras please.");
			}
			return respBody;
		}
		else {
			// Id was not found, let player know
			Logger.log("ID was not found in player's special coords file.");
			rconSendDirMsg("Coord ID "+id+" was not found in special coord file.");
			return "id_not_found";
		}
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
		// Checks to see if command worked
		return rc.readPacket();
	}
	
	// RCON send message to target
	public void rconSendDirMsg(String msg) {
		rconSendAndGet(CommandRefs.generateTellRawMsg(msg, getTarget()));
	}
	
	// Send failed command message to target
	public void rconSendFailCmdMsg(String failMsg) {
		rconSendDirMsg(currCmd+" FAILED: "+failMsg);
	}	
	
	// Target gets and sets
	public String getTarget() {
		return target;
	}

	// Set target name
	public void setTarget(String target) {
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

