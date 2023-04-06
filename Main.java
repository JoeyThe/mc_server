import reader.LogReader;
import commander.Commander;

public class Main
{
	public static void main(String[] args)
	{
		LogReader logReader = new LogReader("/home/joey/mc_server/logs/latest.log");
		Commander Commander = new Commander();
		logReader.print_logs();

		logReader.continuous_read();
	}
}
