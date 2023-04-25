import reader.LogReader;
import processor.Processor;
import rconclient.RconClient;

public class Main {
	public static void main(String[] args) {
		// Start the server process
		Processor serverProcess = new Processor();
		// Start the RCON client
		RconClient rc 		= new RconClient();
		// Start log reader
		LogReader logReader 	= new LogReader(serverProcessor, rc);

		logReader.continuousRead();

		rc.closeConnection();
		serverProcess.kill();
	}
}
