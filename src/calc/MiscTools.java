package calc;

public class MiscTools {
	public static String millisToStringTime(long millis)
	{
		String SS = String.format("%02d",(millis/1000l) % 60);
		String MM = String.format("%02d",(millis/60000l) % 60);
		String HH = String.format("%02d",(millis/3600000l));
		
		return String.format("%s:%s:%s", HH, MM, SS);
	}
}
