import reader.LogReader;
import processor.Processor;
import rconclient.RconClient;
import commander.Commander;

public class Main {
	public static void main(String[] args) {
		// Start the server process
		Processor serverProcessor	= new Processor();
		// Start the RCON client
		RconClient rc 			= new RconClient();
		// Create Commander
		Commander commander		= new Commander(rc); 
		// Start log reader
		LogReader logReader 		= new LogReader(serverProcessor, commander);

		logReader.continuousRead();

		rc.closeConnection();
		// Should not need the .kill because we need to send the "/stop" command in order to even get here
		// Can we add some sort of check though?
//		serverProcessor.kill();
	}
}
