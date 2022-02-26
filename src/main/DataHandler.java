package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DataHandler {
	
	public static String dataDir = "C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\MandelbrotRevamped\\";
	
	public static String pictureDir = "Render\\";
	
	public static String dataFileDir = "data.dat";
	public static String paletteFileDir = "palettes.dat";
	
	public static final String[] DEFAULT_PALETTES = new String[] {"3_Color_(Alt) > 0,255,255 > 255,0,255 > 255,255,0 % a", "3_Color_(Basic) > 255,0,0 > 0,255,0 > 0,0,255 % a", "Sunny > 0,0,255 > 255,255,255 > 255,255,0 > 255,0,0 % a"};
	
	public static void write() throws IOException
	{
		FileWriter fw = new FileWriter(dataDir+dataFileDir);
		fw.write(MyPanel.getRenderOutputPath());
		
		
		fw.close();
	}
	
	public static void read() throws IOException
	{
		File df = new File(dataDir+dataFileDir);
		File pf = new File(dataDir+paletteFileDir);
		
		Scanner sc = new Scanner(df);
		Palette.downloadPalettes(FractalCalculator.palettes, pf);
		
		MyPanel.setRenderOutputPath(sc.nextLine());
		
		sc.close();
	}
	public static void verifyFiles()
	{
		//Data file dir
		File fddir = new File(dataDir);
		//Render storage file dir
		File frdir = new File(dataDir+pictureDir);
		
		//Data file
		File fdata = new File(dataDir+dataFileDir);
		//Palette data file
		File fpalettes = new File(dataDir+paletteFileDir);
		
		try {
			fddir.mkdir();
			frdir.mkdir();
			if (fdata.createNewFile())
			{
				FileWriter fw = new FileWriter(fdata);
				fw.write(dataDir+pictureDir);
				fw.close();
			}
			
			if (fpalettes.createNewFile())
			{
				
				FileWriter fw = new FileWriter(fpalettes);
				fw.write(DEFAULT_PALETTES[0]+"\n");
				fw.write(DEFAULT_PALETTES[1]+"\n");
				fw.write(DEFAULT_PALETTES[2]);
				fw.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
