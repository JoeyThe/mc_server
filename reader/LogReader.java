package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogReader
{
	// Attributes
	public String path;
	
	// Constructor
	public LogReader(String path) 
	{
		this.path = path;
		System.out.println("Path: "+path);
	}

	public void continuous_read()
	{
		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader(this.path));
			String line = reader.readLine();

			while (true) {
				if (line != null) {
					System.out.println(line);
					if (line.contains("shit")) {
						System.out.println("No cursing on the server");
						break;
					}
				}
				line = reader.readLine();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
