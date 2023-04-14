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

public class Processor {
	// Attributes
	private List<String> commands = new ArrayList<String>();
	private String log_file;	

	// Constructor
	public Processor() {
		System.out.println("Starting Minecraft server!");
		commands.add("java");
		commands.add("-Xmx1024M");
		commands.add("-Xms1024M");
		commands.add("-Djava.net.preferIPv4Stack=true");
		commands.add("-jar");
		commands.add("/home/joey/mc_server/server.jar");
		commands.add("nogui");
		ProcessBuilder serverProcessBuild = new ProcessBuilder(commands);
		serverProcessBuild.redirectErrorStream(true);
		serverProcessBuild.directory(new File("/home/joey/mc_server"));
		log_file = "/home/joey/mc_server/automation_java/processor/proc_log_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))+".log";
		System.out.println("Writing process logs to: "+log_file);
		File log = new File(log_file);
		serverProcessBuild.redirectOutput(Redirect.appendTo(log));
		try {
			Process serverProcess = serverProcessBuild.start();
			System.out.println("Server started!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLogFileName() {
		return log_file;
	}
}

