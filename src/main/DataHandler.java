package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import calc.FractalCalculator;
import calc.Palette;
import ui.MainFrame;
import calc.Palette;

public class DataHandler {
	
	private static String dataDir = "C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\MandelbrotRevamped\\";
	
	private static String pictureDir = "Render\\";
	
	private static String dataFileDir = "data.dat";
	private static String paletteFileDir = "palettes.dat";
	
	private static final String[] DEFAULT_PALETTES = new String[] {"3_Color_(Alt) > 0,255,255 > 255,0,255 > 255,255,0 % a", "3_Color_(Basic) > 255,0,0 > 0,255,0 > 0,0,255 % a", "Sunny > 0,0,255 > 255,255,255 > 255,255,0 > 255,0,0 % a"};
	
	private MainFrame mf;
	
	public DataHandler(MainFrame mp)
	{
		this.mf = mp;
	}
	
	/*
	 *  Function that initializes where the data will be stored based on the current OS.
	 */
	public void initDataDir()
	{
		String os = System.getProperty("os.name");
		
		switch (os)
		{
		case "Windows 10":
			dataDir = "C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\MandelbrotRevamped\\";
			pictureDir = "Render\\";
			break;
		case "Linux":
			dataDir = "/home/"+System.getProperty("user.name")+"/.config/MandelbrotRevamped/";
			pictureDir = "Render/";
			break;
		}
		
	}
	
	public void write() throws IOException
	{
		FileWriter fw = new FileWriter(dataDir+dataFileDir);
		fw.write(mf.getMainPanel().getRenderOutputPath());
		
		
		fw.close();
	}
	
	public String read() throws IOException
	{
		File df = new File(dataDir+dataFileDir);
		File pf = new File(dataDir+paletteFileDir);
		
		Scanner sc = new Scanner(df);
		Palette.downloadPalettes(FractalCalculator.getPaletteArray(), pf);
		
		Palette.saveRandomPalette(pf);
		
		String ret = sc.nextLine();
		
		sc.close();
		
		return ret;
	}
	public void verifyFiles()
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
	
	public String getDataDir()
	{
		return dataDir;
	}
	public String getPictureDir()
	{
		return pictureDir;
	}
	public String getDataFileDir()
	{
		return dataFileDir;
	}
	public String getPaletteFileDir()
	{
		return paletteFileDir;
	}
	
	public void setDataDir(String s)
	{
		dataDir = s;
	}
	public void setPictureDir(String s)
	{
		pictureDir = s;
	}
	public void setDataFileDir(String s)
	{
		dataFileDir = s;
	}
	public void setPaletteFileDir(String s)
	{
		paletteFileDir = s;
	}
}
