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

public class Processor {
	// Attributes
	private List<String> commands = new ArrayList<String>();
	private String log_file;	
	private ByteArrayOutputStream commandWriter;

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
		ProcessBuilder serverProcessBuild = new ProcessBuilder(commands);
		// Include stderror in output stream
		serverProcessBuild.redirectErrorStream(true);
		// Set home directory for ProcessBuilder
		serverProcessBuild.directory(new File("/home/joey/mc_server"));
		log_file = "/home/joey/mc_server/automation_java/processor/proc_log_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))+".log";
		System.out.println("Writing process logs to: "+log_file);
		File log = new File(log_file);
		// Redirect output of server process to log file
		serverProcessBuild.redirectOutput(Redirect.appendTo(log));
		Process serverProcess = null;
		try {
			serverProcess = serverProcessBuild.start();
			System.out.println("Server started!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStream serverInputStream = serverProcess.getInputStream();
		OutputStream serverOutputStream = serverProcess.getOutputStream();
		try {
			Thread.sleep(30000);
			serverOutputStream.write((byte) 0xff);
			byte[] query = new byte[256];
			serverInputStream.read(query);
			for (byte b : query) {
				System.out.print(String.format("%02x", b));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (serverProcess.isAlive()) {
		}
		serverProcess.destroy();
		System.exit(0);
		//commandWriter = new ByteArrayOutputStream(new OutputStreamWriter(serverInputStream));
	}

	public String getLogFileName() {
		return log_file;
	}
}

