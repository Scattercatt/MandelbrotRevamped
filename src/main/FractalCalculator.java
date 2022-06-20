package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FractalCalculator {
	
	
	/**
	 * The FractalCalculator class is a mostly static class that handles all of the math besides coloring. It is designed in such a way that 
	 * rendered images are tunable and customaizable. This class includes many of these tuning knobs such as modulusColorDivisons, colorOffset,
	 * colorWrapping, and many more. 
	 * 
	 * 
	 * 
	 * @version 5 May 2022
	 * @author Gavin Green
	 * 
	 */
	
	//This is the list of palettes available to the user. This is filled on startup by the palettes.dat file.
	private static ArrayList<Palette> palettes = new ArrayList<Palette>();
	
	//Points to the palette currently being used.
	private static int selectedPalette = 0;
	
	private static ArrayList<Fractal> fractals = new ArrayList<Fractal>();
	
	//Points to the fractal being used. 
	private static int selectedFractal = 0;
	//Names of fractals based on value.
	private static String[] fractalNames = new String[] {"Mandelbrot Set", "Burning Ship", "Scattercattt", "Butterflies", "Tailing", 
			"Psudobrot 1", "Warbled", "Lips", "Claws", "Mandelbrot_D", "J-Star", "Grid", "12", "13", "14", "15", "16", "17", "18", "J-Web", "Manta", "Prism", "22", "23", "24", "25", "26", "27J"};
	
	//Points to the bailout method being used.
	private static int selectedBailout = 0;
	//Names of bailout methods.
	private static String[] bailoutNames = new String[] {"Basic", "Follicle", "Jungle", "Amazon"};
	
	
	//The maximum number of iterations the program will calculate. 
	private static int maxIterations = 300;
	
	/*
	 * Modulus color divisions represents how often the palette of colors will loop. For instance:
	 * If I have a palette that is 0,0,0 > 255,255,255 (black to white), and modulus color divisions is say, 5, then the program will create a gradient of 5 colors in between this palette.
	 * So the colors used would be:
	 * 0,0,0 
	 * 51,51,51
	 * 102,102,102
	 * 153,153,153
	 * 204,204,204
	 */
	private static int modulusColorDivisions = 30;
	
	//colorWrapping just determines if the program will use modulus color divisions.
	private static boolean colorWrapping = true;
	
	//colorOffset is the value that the gradient will be shifted by. If this were 5, iterations 0 will instead be colored as if it were iteration 5
	private static int colorOffset = 0;
	
	//paletteShiftMode determines which paletteShiftMode the program is using.
	//Palette shifts are different modes where the RGB values of colors are swapped around. For instance, one of the options is BRG, which, if its not self explainatory, does this:
	// R becomes B
	// G becomes R
	// B becomes G
	//Thats just an example of one palette shift mode.
	private static int paletteShiftMode = 0;
	
	//This is the view window of the main fractal. P1 represents the top left, and P2 represents the bottom right. These are their mathematical values. It is CRITICAL that these are doubles, and not floats. 
	private static double cameraP1[] = new double[] {-2, -2};
	private static double cameraP2[] = new double[] {2, 2};
	
	//Ditto as last comment, but for the julia window. 
	private static double juliaCameraP1[] = new double[] {-2, -2};
	private static double juliaCameraP2[] = new double[] {2, 2};
	
	//Render detail represents how detailed of a render the program generates. At 0, the render is the basic render method: rendering points for every given position of an array
	//At 1 and above things get complicated. For 1, the program first *doubles* the size of the array; 100x100 becomes 200x200. each point is calculated in this new large array, and is then averaged back down to 100x100 for finer detail. This exponentially increases render time.
	//At 2, the program quadruples. (100x100 -> 400x400). this makes render time take 16x longer. Rarely used.
	//Very sensitive variable. 0 = Standard render. 1 = Double resolution compressed. 2 = Quadruple, etc.
	private static int renderDetail = 0;

	//Simple constant zero to be used.
	static final Complex C_ZERO = new Complex(0,0);
	
	
	//////////////////////////////////////////////////
	
	//Determines whether pixels calculated to be within the set are colored.
	private static boolean colorInsidePixels = false;
	
	//Determines whether pixels calculated to be outside of the set are colored.
	private static boolean colorOutsidePixels = true;
	
	
	
	


	private static InSetCalculator selectedInSetCalculator = null;

	private static boolean calcBailout(Complex Z)
	{
		switch (selectedBailout)
		{
		     case 0:
		       if (Z.mod() > 4)
		         return true;
		       break;
		     case 1:
		       if (Z.getR() * Z.getI() > 1000)
		         return true;
		       break;
		     case 2:
		       if (Z.getR() * Z.getI() > Z.getI() * Z.getI())
		    	 return true;
		     case 3:
		       if (Z.getR() > 0.3 && Z.getI() > 0.3)
			     return true;
		     default:
		    	 return false;
		}
		return false;
		
		 
	}
	
	/*
	 * The main function driving fractal rendering. 
	 * This is called "calcFractalColumn" because this program renders each of it's images one column at a time. Each column is offloaded to different threads. All this function can do is render one sliver of pixels in an entire image "id".
	 */
	public static synchronized void calcFractalColumn(Color[][] id, Integer column, boolean julia, double jpx, double jpy, int param_paletteShiftMode)
	{	
		if (param_paletteShiftMode == -1)
			param_paletteShiftMode = paletteShiftMode;
		
		RenderProgressJPanel.setJob(column, (byte) 2);
		Complex c, z;
		int jy = 0;
		
		double p1x, p1y, p2x, p2y;
		
		//If julia, use the julia camera. If not, use the main camera.
		if (julia)
		{
			p1x = juliaCameraP1[0];
			p1y = juliaCameraP1[1];
			p2x = juliaCameraP2[0];
			p2y = juliaCameraP2[1];
		}
		else
		{
			p1x = cameraP1[0];
			p1y = cameraP1[1];
			p2x = cameraP2[0];
			p2y = cameraP2[1];
		}
		
		
		//xval represents what column of the render is being worked on in this function. Remember this function is used on multiple threads at a time. This is effectively converting the Integer "column" value to a usable value in terms of render
		//For example, lets say the camera's position is (-0.5,0.2),(0,-0.3), and we're rendering column number 312 of an image of 5000x5000 size.
		//In this example, the distance *mathematically* between the start of the camera and the end of the camera is 0.5 since if we take x2-x1 (0 - -0.5 = 0.5). But in an image of 5000x5000 size, we need to represent the 312th column in mathematical terms.
		//We do this by calculating 312/5000 which gives us 0.0624. We multiply this number by the camera length to get the mathematical value 0.0312
		//This is still not complete. The line after this one adds the x1 camera position as an anchor point. 
		double xval = (double) column / (double) id.length * (p2x - p1x);
		xval = xval + p1x;
		
		int iterations;
		
		//Here, iy represents the vertical position of the current renderer thread. This goes from the top to the bottom of the column, rendering each pixel as it goes. In the case of higher quality settings, each pixel is subdivided and averaged. 
		for (double iy = p1y; jy < id.length; iy = iy + ((p2y - p1y) / id.length), jy++)
		{
			//renderDetail is by default 0, indicating that no subdividing & averaging of pixels dont happen
			// However, if renderDetail is non zero, subdivision happens.
			if (renderDetail != 0)
			{
				ArrayList<Color> colors = new ArrayList<Color>();
				
				//This for loop in summary:
				//Take the size of the current column. 
				//Divide it into smaller pieces based on renderDetail		
				//(this is definitely the most complex nested for loop ive ever written)
				for (double hdx = xval; 
						hdx < xval + (p2x - p1x) / (double) id.length; 
						hdx += (p2x - p1x) / (double) id.length / Math.pow(2, renderDetail))
				{
					for (double hdy = iy; 
							hdy < iy + ((p2y - p1y) / (double) id.length); 
							hdy += (p2y - p1y) / (double) id.length / Math.pow(2, renderDetail))
					{
						ArrayList<Complex> points = (colorInsidePixels) ? new ArrayList<Complex>() : null;

						iterations = 0;
						
						if (julia)
						{
							c = new Complex(jpx, jpy);
							
							z = new Complex(hdx, hdy);
						}
						else
						{
							c = new Complex(hdx, hdy);
							
							z = C_ZERO;
						}
						
						if (colorInsidePixels)
							points.add(c);
			
						while (!calcBailout(z) && iterations < maxIterations) 
						{
							
							//z = ZIterative(z, c);
							z = fractals.get(selectedFractal).ZIterative(z, c);
							
							if (colorInsidePixels)
								points.add(z);
							
							iterations++;
							RenderProgressJPanel.incIC();
						}
						//System.out.println(column+","+iy+". "+iterations+". ["+column+","+jy+"]");	
						
						if (iterations == maxIterations)
							if (colorInsidePixels)
							{
								int variableNum = selectedInSetCalculator.calculateVariable(points);
								int maxNum = selectedInSetCalculator.calculateMax(points);
								colors.add(palettes.get(selectedPalette).calculate(variableNum, maxNum, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode));
								
							}
							else
								colors.add(Color.BLACK);
						else
							if (colorOutsidePixels)
								colors.add(palettes.get(selectedPalette).calculate(iterations, maxIterations, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode));
							else
								colors.add(Color.BLACK);
						
					}
				}

				id[column][jy] = averageColorArray(colors);
				RenderProgressJPanel.incPC();
				
			}
			else
			{
				ArrayList<Complex> points = (colorInsidePixels) ? new ArrayList<Complex>() : null;
				
				iterations = 0;
				
				if (julia)
				{
					c = new Complex(jpx, jpy);
					
					z = new Complex(xval, iy);
				}
				else
				{
					c = new Complex(xval, iy);
					
					z = C_ZERO;
				}
				
				if (colorInsidePixels)
					points.add(c);
				
				while (!calcBailout(z) && iterations < maxIterations) 
				{
					//z = ZIterative(z, c);
					z = fractals.get(selectedFractal).ZIterative(z, c);
					
					if (colorInsidePixels)
						points.add(z);
					
					iterations++;
					RenderProgressJPanel.incIC();
					//totalIterationsCalculated++;
				}
				
				if (iterations == maxIterations)
					if (colorInsidePixels)
					{
						int variableNum = selectedInSetCalculator.calculateVariable(points);
						int maxNum = selectedInSetCalculator.calculateMax(points);
						id[column][jy] = palettes.get(selectedPalette).calculate(variableNum, maxNum, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode);
						
					}	
					else
						id[column][jy] = Color.BLACK;
				else
					if (colorOutsidePixels)
						id[column][jy] = palettes.get(selectedPalette).calculate(iterations, maxIterations, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode);
					else
						id[column][jy] = Color.BLACK;

				RenderProgressJPanel.incPC();

			}
		}
		RenderProgressJPanel.setJob(column, (byte) 3);
	}
	
	//This function is the same as the one above, but uses a BufferedImage instead of Color[][]. See previous function for comments. 
	public static synchronized void calcFractalColumn(BufferedImage id, Integer column, boolean julia, double jpx, double jpy, int param_paletteShiftMode)
	{	
		if (param_paletteShiftMode == -1)
			param_paletteShiftMode = paletteShiftMode;
		
		RenderProgressJPanel.setJob(column, (byte) 2);
		Complex c, z;
		int jy = 0;
		
		double p1x, p1y, p2x, p2y;
		
		if (julia)
		{
			p1x = juliaCameraP1[0];
			p1y = juliaCameraP1[1];
			p2x = juliaCameraP2[0];
			p2y = juliaCameraP2[1];
		}
		else
		{
			p1x = cameraP1[0];
			p1y = cameraP1[1];
			p2x = cameraP2[0];
			p2y = cameraP2[1];
		}
		
		
		double xval = (p2x - p1x) / id.getWidth() * column;
		xval = xval + p1x;
		
		int iterations;

		for (double iy = p1y; jy < id.getWidth(); iy = iy + ((p2y - p1y) / id.getWidth()), jy++)
		{
			if (renderDetail != 0)
			{
				Color col;
				ArrayList<Color> colors = new ArrayList<Color>();
			
				for (double hdx = xval; 
						hdx < xval + (p2x - p1x) / (double) id.getWidth(); 
						hdx += (p2x - p1x) / (double) id.getWidth()/ Math.pow(2, renderDetail))
				{
					for (double hdy = iy; 
							hdy < iy + ((p2y - p1y) / (double) id.getWidth()); 
							hdy += (p2y - p1y) / (double) id.getWidth() / Math.pow(2, renderDetail))
					{
						ArrayList<Complex> points = (colorInsidePixels) ? new ArrayList<Complex>() : null;
						
						iterations = 0;
						
						if (julia)
						{
							c = new Complex(jpx, jpy);
							
							z = new Complex(hdx, hdy);
						}
						else
						{
							c = new Complex(hdx, hdy);
							
							z = C_ZERO;
						}
						
						if (colorInsidePixels)
							points.add(c);
			
						while (!calcBailout(z) && iterations < maxIterations) // x*x + y*y < 4
						{
							
							//z = ZIterative(z, c);
							z = fractals.get(selectedFractal).ZIterative(z, c);

							if (colorInsidePixels)
								points.add(z);
							
							iterations++;
							RenderProgressJPanel.incIC();
							//totalIterationsCalculated++;
						}
						//System.out.println(column+","+iy+". "+iterations+". ["+column+","+jy+"]");	
						
						if (iterations == maxIterations)
							if (colorInsidePixels)
							{
								int variableNum = selectedInSetCalculator.calculateVariable(points);
								int maxNum = selectedInSetCalculator.calculateMax(points);
								colors.add(palettes.get(selectedPalette).calculate(variableNum, maxNum, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode));
							}	
							else
								colors.add(Color.BLACK);
						else
							if (colorOutsidePixels)
								colors.add(palettes.get(selectedPalette).calculate(iterations, maxIterations, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode));
							else
								colors.add(Color.BLACK);
						
					}
				}

				col = averageColorArray(colors);
				id.setRGB(column, jy, col.getRGB());
				
				RenderProgressJPanel.incPC();
				
			}
			else
			{
				Color col;
				
				ArrayList<Complex> points = (colorInsidePixels) ? new ArrayList<Complex>() : null;
				
				iterations = 0;
				
				if (julia)
				{
					c = new Complex(jpx, jpy);
					
					z = new Complex(xval, iy);
				}
				else
				{
					c = new Complex(xval, iy);
					
					z = C_ZERO;
				}
				
				if (colorInsidePixels)
					points.add(c);
				
				while (!calcBailout(z) && iterations < maxIterations) 
				{
					
					//z = ZIterative(z, c);
					z = fractals.get(selectedFractal).ZIterative(z, c);
					
					if (colorInsidePixels)
						points.add(z);
					
					iterations++;
					RenderProgressJPanel.incIC();
					//totalIterationsCalculated++;
				}
				if (iterations == maxIterations)
					if (colorInsidePixels)
					{
						int variableNum = selectedInSetCalculator.calculateVariable(points);
						int maxNum = selectedInSetCalculator.calculateMax(points);
						col = palettes.get(selectedPalette).calculate(variableNum, maxNum, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode);
						
					}	
					else
						col = Color.BLACK;
				else
					if (colorOutsidePixels)
						col = palettes.get(selectedPalette).calculate(iterations, maxIterations, colorWrapping, modulusColorDivisions, colorOffset, param_paletteShiftMode);
					else
						col = Color.BLACK;
				id.setRGB(column, jy, col.getRGB());
				RenderProgressJPanel.incPC();

			}
		}
		RenderProgressJPanel.setJob(column, (byte) 3);
	}
	public static Color getColorFromIterations(int i) {
		return new Color(i * 10 % 255, 0, 0);
	}
	
	static BigDecimal intToBD(Integer x)
	{
		String t = x.toString();
		return new BigDecimal(t);
	}
	@SuppressWarnings("unused")
	
	private static double getDecimal(double x)
	{
		return x % 1;
	}
	public static double warbleDecimal(double x)
	{
		return x * (x % 1 + 1);
	}
	//Gets the average of 2 colors.
	public static Color averageColors(Color a, Color b)
	{
		return new Color(
				(int) Math.round(Math.sqrt((a.getRed()*a.getRed()+b.getRed()*b.getRed())/2)),
				(int) Math.round(Math.sqrt((a.getGreen()*a.getGreen()+b.getGreen()*b.getGreen())/2)),
				(int) Math.round(Math.sqrt((a.getBlue()*a.getBlue()+b.getBlue()*b.getBlue())/2))
				);
		
		
	}
	
	//Gets the average of all the colors in an array. Primarily used in subdivision->averaging
	public static Color averageColorArray(ArrayList<Color> colors)
	{
		int sumReds = 0, sumGreens = 0, sumBlues = 0; 
		for (int i = 0; i < colors.size(); i++)
		{
			sumReds += Math.pow(colors.get(i).getRed(), 2);
			sumGreens += Math.pow(colors.get(i).getGreen(), 2);
			sumBlues += Math.pow(colors.get(i).getBlue(), 2);		
		}

		return new Color(
					(int) Math.round(Math.sqrt((double)sumReds / (double)colors.size())),
					(int) Math.round(Math.sqrt((double)sumGreens / (double)colors.size())),
					(int) Math.round(Math.sqrt((double)sumBlues / (double)colors.size()))
				);
				
	}
	
	//These are all the currently usable fractals
	public static void initializeFractals()
	{
		fractals.add(new Fractal("Mandelbrot Set") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = Z.mult(Z).add(C);
				return Z;
			}
		});
		fractals.add(new Fractal("Burning Ship") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = new Complex(Math.abs(Z.getR()), Math.abs(Z.getI()));
		        Z = Z.mult(Z).add(C);
		        return Z;
			}
		});
		fractals.add(new Fractal("Scattercatt") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = Z.mult(C.log());
		        Z = Z.mult(Z.tan());
		        Z = Z.add(C);
		        return Z;
			}
		});
		fractals.add(new Fractal("Butterflies") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = Z.mult(Z);
		        Z = Z.tan();
		        Z = Z.sin();
		        Z = Z.add(C);
				return Z;
			}
		});
		fractals.add(new Fractal("Tailing Set") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = Z.cosh();
		        Z = Z.add(C);
		        Z = Z.tan();
		        Z = Z.div(C);
		        Z = Z.log();
				return Z;
			}
		});
		fractals.add(new Fractal("Psuedobrot 1") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = Z.mult(Z.div(C.log())).add(new Complex(C.getR(), C.getI()));
				return Z;
			}
		});
		fractals.add(new Fractal("Warbled") {
			@Override
			public Complex ZIterative(Complex Z, Complex C)
			{
				 Z = new Complex(warbleDecimal(Z.getR()), warbleDecimal(Z.getI()));
			     Z = Z.mult(Z).add(C);
			     return Z;
			}
		});
		
		fractals.add(new Fractal("Lips") {
			public Complex ZIterative(Complex Z, Complex C)
			{
				Complex t = C.cosh();
			     Z = new Complex(Z.getR() % t.getR(), Z.getI() % t.getI());
			     Z = Z.mult(t.add(Z)).add(C);
			     return Z;
			}
			
		});
		fractals.add(new Fractal("Claws") {
			public Complex ZIterative(Complex Z, Complex C)
			{
				Complex c8 = C.log();
			    Z = new Complex(Z.getR() % c8.getR(), Z.getI() % c8.getI());
			    Z = Z.mult(c8.add(Z)).add(C);
			    return Z;
			}
			
		});
		fractals.add(new Fractal("Prism") {
			public Complex ZIterative(Complex Z, Complex C)
			{
				Z = new Complex(Z.getR() * -1, Z.getI() * -1);
			     Z = new Complex(Z.getR() + Math.abs(Z.getI()), Z.getI() + Math.abs(Z.getR())).add(C);
			     return Z;
			}
			
		});

		
	}
	
	public static boolean verifyCurrentPalette()
	{
		return (selectedPalette < palettes.size()) ? true : false;
	}
	public static String getCurrentFractalName()
	{
		return fractals.get(selectedFractal).getName();
	}
	public static String getCurrentBailoutName()
	{
		return bailoutNames[selectedBailout];
	}
	public static String getCurrentPaletteName()
	{
		return palettes.get(selectedPalette).getName();
	}
	public static int getPaletteArraySize()
	{
		return palettes.size();
	}
	public static Palette getCurrentPalette()
	{
		return palettes.get(selectedPalette);
	}
	public static int getFractalArraySize()
	{
		return fractals.size();
	}

	

	
	public static void setInSetCalculator(InSetCalculator isc)
	{
		selectedInSetCalculator = isc;
	}
	
	public static Palette getPaletteAt(int n)
	{
		return palettes.get(n);
	}
	public static int getSelectedPalette()
	{
		return selectedPalette;
	}
	public static int getSelectedFractal()
	{
		return selectedFractal;
	}
	public static String getFractalName(int pos)
	{
		return fractals.get(pos).getName();
	}
	public static int getSelectedBailout()
	{
		return selectedBailout;
	}
	public static String getBailoutlName(int pos)
	{
		return bailoutNames[pos];
	}
	public static String[] getBailoutNameArray()
	{
		return bailoutNames;
	}
	public static int getMaxIterations()
	{
		return maxIterations;
	}
	public static int getModulusColorDivision()
	{
		return modulusColorDivisions;
	}
	public static boolean getColorWrapping()
	{
		return colorWrapping;
	}
	public static int getColorOffset()
	{
		return colorOffset;
	}
	public static int getPaletteShiftMode()
	{
		return paletteShiftMode;
	}
	public static double[] getCameraP1()
	{
		return cameraP1;
	}
	public static double[] getCameraP2()
	{
		return cameraP2;
	}
	public static double[] getJuliaCameraP1()
	{
		return juliaCameraP1;
	}
	public static double[] getJuliaCameraP2()
	{
		return juliaCameraP2;
	}
	public static int getRenderDetail()
	{
		return renderDetail;
	}
	public static boolean getColorInsidePixels()
	{
		return colorInsidePixels;
	}
	public static boolean getColorOutsidePixels()
	{
		return colorOutsidePixels;
	}
	public static ArrayList<Palette> getPaletteArray() 
	{
		return palettes;
	}
	
	public static void setSelectedPalette(int n)
	{
		selectedPalette = n;
	}
	public static void setSelectedFractal(int n)
	{
		 selectedFractal = n;
	}
	public static void setFractalName(int pos, String s)
	{
		 fractalNames[pos] = s;
	}
	public static void setSelectedBailout(int n)
	{
		 selectedBailout = n;
	}
	public static void setBailoutlName(int pos, String s)
	{
		 bailoutNames[pos] = s;
	}
	public static void setMaxIterations(int n)
	{
		 maxIterations = n;
	}
	public static void setModulusColorDivision(int n)
	{
		 modulusColorDivisions = n;
	}
	public static void setColorWrapping(boolean b)
	{
		 colorWrapping = b;
	}
	public static void setColorOffset(int n)
	{
		 colorOffset = n;
	}
	public static void setPaletteShiftMode(int n)
	{
		 paletteShiftMode = n;
	}
	public static void setCameraP1(double[] pos)
	{
		 cameraP1[0] = pos[0];
		 cameraP1[1] = pos[1];
	}
	public static void setCameraP2(double[] pos)
	{
		 cameraP2[0] = pos[0];
		 cameraP2[1] = pos[1];
	}
	public static void setJuliaCameraP1(double[] pos)
	{
		 juliaCameraP1[0] = pos[0];
		 juliaCameraP1[1] = pos[1];
	}
	public static void setJuliaCameraP2(double[] pos)
	{
		 juliaCameraP2[0] = pos[0];
		 juliaCameraP2[1] = pos[1];
	}
	public static void setRenderDetail(int n)
	{
		 renderDetail = n;
	}
	public static void setColorInsidePixels(boolean b)
	{
		 colorInsidePixels = b;
	}
	public static void setColorOutsidePixels(boolean b)
	{
		 colorOutsidePixels = b;
	}
	
	public static void addToSelectedPalette(int n)
	{
		selectedPalette += n;
	}
	public static void addToSelectedFractal(int n)
	{
		selectedFractal += n;
	}
	public static void addToSelectedBailout(int n)
	{
		selectedBailout += n;
	}
	public static void addToMaxIterations(int n)
	{
		maxIterations += n;
	}
	public static void addToModulusColorDivisions(int n)
	{
		modulusColorDivisions += n;
	}
	public static void addToColorOffset(int n)
	{
		colorOffset += n;
	}

	

	
}
