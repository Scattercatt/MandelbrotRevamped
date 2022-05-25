package main;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.awt.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Random;

public class Palette
{

/**
 * The Palette class is a list of colors and positions used to dynamically generate fractal colors based on given
 * calculations values. Palettes are built so that a gradient of colors can be created with ease through the 
 * palettes.dat file. 
 * 
 * Palettes use float positions to indicate how far gradients between colors stretch. They can be used to emphasize
 * one color more than another. However, automatic position assignment is also allowed, where each color is spaced
 * the same distance apart. 
 * 
 * @version 24 Feb 2022
 * @author Gavin Green
 * 
 */
		
	String name;
	ArrayList<Color> Colors = new ArrayList<Color>();
	ArrayList<Float> positions = new ArrayList<Float>();
	
	
	private static Random random = new Random();
	/**
	 * 
	 * @param name Name of palette
	 * @param Colors ArrayList of colors
	 * @param positions ArrayList of float positions
	 */
  public Palette(String name, ArrayList<Color> Colors, ArrayList<Float> positions)
  {
    this.name = name;
    this.Colors = Colors;
    this.positions = positions;
  }
  
  /**
   * Empty Palette. Name required
   * @param name Name of palette
   */
  public Palette(String name)
  {
    this.name = name;
  }
  
  /**
   * Normal constructor, but using normal arrays instead of ArrayLists. 
   * @param name Name of palette
   * @param Colors Array of colors
   * @param positions Array of float positions
   */
  public Palette(String name, Color[] Colors, float[] positions)
  {
    this.name = name;
    for (int i = 0; i < Colors.length; i++)
      this.addColor(Colors[i].getRed(), Colors[i].getGreen(), Colors[i].getBlue(), positions[i]);
  }
  
  /**
   * Color only constructor. Takes color Array. Generates automatic positions
   * @param name Name of palette
   * @param Colors Array of colors
   */
  public Palette(String name, Color[] Colors)
  {
    this.name = name;
    for (int i = 0; i < Colors.length; i++)
      this.addColor(Colors[i].getRed(), Colors[i].getGreen(), Colors[i].getBlue(), (float) i * (1.0f/ (float) Colors.length));
  }
  
  /**
   * Adds a color to the palette
   * @param r int red
   * @param g int green
   * @param b int blue
   * @param position gradient point for this color
   */
  public void addColor(int r, int g, int b, float position)
  {
    Colors.add(new Color(r, g, b));
    positions.add(position);
  }
  public Color getColor(int pos)
  {
    return Colors.get(pos);
  }
  /**
   * Gets name of palette
   * @return name of palette
   */
  public String getName()
  {
     return name; 
  }
  /**
   * Set name of palette
   * @param name name
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Takes the calculations of the iterated point (iterations and maxIterations) and return a color representation. 
   * @param colorWrapping whether the color sequence repeats itself based on modulusColorDivisions
   * @param modulusColorDivisions how often the color sequence repeats itself
   * @param colorOffset adjusts the entire gradient in one direction. Does not affect how often colors are repeated.
   * @param paletteShiftMode rearranges the RGB values based on its value. Used to change up palette colors
   * @return Color based on the provided parameters.
   */
  public Color calculate(int iterations, int maxIterations, boolean colorWrapping, int modulusColorDivisions, int colorOffset, int paletteShiftMode)
  {
    float percentageOfPalette = (colorWrapping) ? ((float)iterations % (modulusColorDivisions * Colors.size()) / (modulusColorDivisions * Colors.size())) : (float)iterations/(float)maxIterations;
    percentageOfPalette = ((float) colorOffset / 100.0f + percentageOfPalette) % 1;

    Color ColorLow = null;
    Color ColorHigh = null;

    float positionLow = 0.0f;
    float positionHigh = 0.0f;
    
    float percentageStack = 0;
    
    boolean ColorFound = false;
    
    for (int i = 0; i < positions.size() - 1; i++)
    {
      percentageStack += positions.get(i+1) - positions.get(i);
      
      if (percentageOfPalette < percentageStack)
      {
        ColorLow = Colors.get(i);
        ColorHigh = Colors.get(i+1);
        
        positionLow = positions.get(i);
        positionHigh = positions.get(i+1);
        
        ColorFound = true;
        i = positions.size();
      }
      
    }
    if (!ColorFound)
    {
       ColorLow = Colors.get(Colors.size() - 1); 
       ColorHigh = Colors.get(0); 
       
       positionLow = positions.get(positions.size() - 1);
       positionHigh = 1;
    }
    
    float gradientMark = (percentageOfPalette - positionLow) / (positionHigh - positionLow);
    
    Color retColor = new Color (
      (int) ((ColorHigh.getRed() - ColorLow.getRed()) * gradientMark + ColorLow.getRed()),
      (int) ((ColorHigh.getGreen() - ColorLow.getGreen()) * gradientMark + ColorLow.getGreen()),
      (int) ((ColorHigh.getBlue() - ColorLow.getBlue()) * gradientMark + ColorLow.getBlue())
      );
    
    switch (paletteShiftMode)
    {
      case 0:
        return new Color(retColor.getRed(), retColor.getGreen(), retColor.getBlue());
      case 1:
        return new Color(retColor.getBlue(), retColor.getRed(), retColor.getGreen());
      case 2:
        return new Color(retColor.getGreen(), retColor.getBlue(), retColor.getRed()); 
      case 3:
        return new Color(retColor.getRed(), retColor.getBlue(), retColor.getGreen());
      case 4:
        return new Color(retColor.getBlue(), retColor.getGreen(), retColor.getRed());
      default:
        return new Color(255, 0, 255);
    }
    
    
  }
  
  /**
   * Prints to console the string representation of a palette
   */
  public void printPalette()
  {
	System.out.println(name+": \n");
	for(int i = 0; i < Colors.size(); i++)
	{
	  System.out.println(Colors.get(i).getRed() +" "+Colors.get(i).getGreen() +" "+Colors.get(i).getBlue() +" "+ positions.get(i)+"\n");
    }
  }
  
  /**
   * Downloads palette data from the palettes.dat file
   */
  public static void downloadPalettes(ArrayList<Palette> palettes, File paletteFile) throws NoSuchElementException
  {
    String currentToken;
    
    String name = null;
    ArrayList<Float[]> colors;
    ArrayList<Float> positions;
    
    String[] splitToken;
    boolean isAuto = false;
    
    try
    {
      Scanner sc = new Scanner(paletteFile);
      
      while (sc.hasNextLine())
      {
        isAuto = false;
        colors = new ArrayList<Float[]>();
        positions = new ArrayList<Float>();
        

        name = sc.next().replace("_"," ");


        while (sc.next().charAt(0) == '>')
        {   
          splitToken = sc.next().split(",");
          
          colors.add(new Float[] {Float.parseFloat(splitToken[0]), Float.parseFloat(splitToken[1]), Float.parseFloat(splitToken[2])});
        }

        currentToken = sc.next();
        
        if (currentToken.charAt(0) == 'a')
        {
          isAuto = true; 
        }
        else
        {
          while (currentToken.charAt(0) != 'X')
          {
              positions.add(Float.parseFloat(currentToken));
              currentToken = sc.next();
          }
        }
        
        if (isAuto)
          palettes.add(new Palette(name, floatArrayALtoColorArray(colors)));
        else
          palettes.add(new Palette(name, floatArrayALtoColorArray(colors), floatALtofloatArray(positions)));
      }
      
      sc.close();
    }

    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Private method used to convert an arraylist to color array. Used for the color component when downloading palettes
   */
  static Color[] floatArrayALtoColorArray(ArrayList<Float[]> al)
  {
    Color[] ret = new Color[al.size()];
    for (int i = 0; i < al.size(); i++)
        ret[i] = new Color(al.get(i)[0]/255.0f, al.get(i)[1]/255.0f, al.get(i)[2]/255.0f);
    return ret;
  }
  /**
   * Private method used to convert an arraylist to float array. Used for the position component when downloading palettes
   */
  static float[] floatALtofloatArray(ArrayList<Float> al)
  {
    float[] ret = new float[al.size()];
    for (int i = 0; i < al.size(); i++)  
      ret[i] = al.get(i);
    return ret;
  }
  /**
   * Creates a random palette in the palettes.dat file
   * @throws IOException 
   * 
   */
  static void saveRandomPalette(File paletteFile) throws IOException
  {
	  /*
	  FileWriter fw = new FileWriter(paletteFile);
	  
	  String palName = "Random_" + random.nextInt(1000000);
	  
	  final int COLOR_COUNT = 100;
	  
	  String colorDataString = "";
	  
	  for (int i = 0; i < COLOR_COUNT; i++)
	  {
		  final String COLOR_DATA_PREFIX = " > ";
		  
		  colorDataString += COLOR_DATA_PREFIX + String.format("%s,%s,%s", random.nextInt(256), random.nextInt(256), random.nextInt(256));
	  }
	  
	  String formattedPaletteData = String.format("%s%s %% a", palName, colorDataString);
	  
	  System.out.println(formattedPaletteData);
	  fw.append(formattedPaletteData);
	  
	  fw.close();
	  */
  }
  
  /**
   * Gets string represnetation of shift mode
   * @param mode Shift mode
   * @return String representation of the shift mode
   */
  public static String getPaletteShiftModeString(int mode)
  {
	  switch(mode)
	  {
	  case 0:
		  return "RGB";
	  case 1:
		  return "BRG";
	  case 2:
		  return "GBR";
	  case 3:
		  return "RBG";
	  case 4:
		  return "BGR";
	  default:
		  return "INVALID!";
	  }
  }
}