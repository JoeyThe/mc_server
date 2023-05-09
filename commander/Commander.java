package commander;

import Java.utils.Arrays;

public class Commander {
        // Attributes
	private static final String NOT_A_COMMAND = "NOT_A_COMMAND";
	private static final String[] validCommands = new String [
		"testMsg"
	];

	public String parseCommand(String line) {
		String parsedCmd = line.split("%",2)[1].split(" ",2)[0];
		return verifyValidCommand(parsedCmd) ? parsedCmd : NOT_A_COMMAND;
	}

	public boolean verifyValidCommand(String potentialCmd) {
		return Arrays.stream(validCommands).anyMatch(potentialCmd::equals);
	}
}

