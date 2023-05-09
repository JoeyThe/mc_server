package processor;

import java.lang.ProcessBuilder;
import java.lang.ProcessBuilder.Redirect;
import java.lang.Process;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;

import logger.Logger;

public class Processor {
	// Attributes
	private List<String> commands = new ArrayList<String>();
	private String logFilename;	
	private ByteArrayOutputStream commandWriter;
	private ProcessBuilder serverProcessBuilder;
	private Process serverProcess;

	// Constructor
	public Processor() {
		System.out.println("Starting Minecraft server!");
		// Add commands to List, treated as separate arguments for ProcessBuilder
		commands.add("java");
		commands.add("-Xmx1024M");
		commands.add("-Xms1024M");
		commands.add("-Djava.net.preferIPv4Stack=true");
		commands.add("-jar");
		commands.add("/home/joey/mc_server/server.jar");
		commands.add("nogui");
		// Create ProcessBuilder
		serverProcessBuilder = new ProcessBuilder(commands);
		// Include stderror in output stream
		serverProcessBuilder.redirectErrorStream(true);
		// Set home directory for ProcessBuilder
		serverProcessBuilder.directory(new File("/home/joey/mc_server"));
		logFilename = "/home/joey/mc_server/automation_java/processor/proc_log_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))+".log";
		Logger.log("Writing process logs to: "+logFilename);
		File log = new File(logFilename);
		// Redirect output of server process to log file
		serverProcessBuilder.redirectOutput(Redirect.appendTo(log));
		serverProcess = null;
		try {
			serverProcess = serverProcessBuilder.start();
			System.out.println("Server started!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLogFileName() {
		return logFilename;
	}

	public void kill() {
		Logger.log("Killing the server!");
		serverProcess.destroy();
	}
}

