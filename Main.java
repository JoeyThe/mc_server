import reader.LogReader;
import commander.Commander;
import processor.Processor;

public class Main {
	public static void main(String[] args) {
		// Start the server process
		Processor serverProcess = new Processor();
		// Start commander
		Commander commander	= new Commander(serverProcess);
		// Start log reader
		LogReader logReader 	= new LogReader("/home/joey/mc_server/logs/latest.log", commander);

		logReader.continuousRead();
	}
}
