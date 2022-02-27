package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FractalCalculator {
	
	public static ArrayList<Palette> palettes = new ArrayList<Palette>();
	
	public static int selectedPalette = 0;
	
	public static int selectedFractal = 0;
	public static String[] fractalNames = new String[] {"Mandelbrot Set", "Burning Ship", "Scattercattt", "Butterflies", "Tailing", 
			"Psudobrot 1", "Warbled", "Lips", "Claws", "Mandelbrot_D", "J-Star", "Grid", "12", "13", "14", "15", "16", "17", "18", "J-Web", "Manta", "Prism", "22", "23", "24", "25", "26", "27J"};
	
	public static int selectedBailout = 0;
	public static String[] bailoutNames = new String[] {"Basic", "Follicle", "Jungle", "Amazon"};
	
	public static int maxIterations = 300;
	
	public static int modulusColorDivisions = 30;
	public static boolean colorWrapping = true;
	public static int colorOffset = 0;
	public static int paletteShiftMode = 0;
	
	public static double cameraP1[] = new double[] {-2, -2};
	public static double cameraP2[] = new double[] {2, 2};
	
	public static double juliaCameraP1[] = new double[] {-2, -2};
	public static double juliaCameraP2[] = new double[] {2, 2};
	
	//Very sensitive variable. 0 = Standard render. 1 = Double resolution compressed. 2 = Quadruple, etc.
	public static int renderDetail = 0;

	static final Complex C_ZERO = new Complex(0,0);
	
	
	//////////////////////////////////////////////////
	
	public static boolean colorInsidePixels = false;
	public static boolean colorOutsidePixels = true;
	


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
	
	public static void calcFractalColumn(Color[][] id, Integer column, boolean julia, double jpx, double jpy, int param_paletteShiftMode)
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
		
		
		double xval = (p2x - p1x) / id.length * column;
		xval = xval + p1x;
		
		int iterations;

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
							
							z = ZIterative(z, c);
							
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
					z = ZIterative(z, c);
					
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
	public static void calcFractalColumn(BufferedImage id, Integer column, boolean julia, double jpx, double jpy, int param_paletteShiftMode)
	{	
		if (param_paletteShiftMode == -1)
			param_paletteShiftMode = paletteShiftMode;
		
		RenderProgressJPanel.setJob(column, (byte) 2);
		Complex c, z;
		int jy = 0;
		
		double p1x = cameraP1[0];
		double p1y = cameraP1[1];
		double p2x = cameraP2[0];
		double p2y = cameraP2[1];
		
		
		double xval = (p2x - p1x) / id.getWidth() * column;
		xval = xval + p1x;
		
		int iterations;

		for (double iy = p1y; jy < id.getWidth(); iy = iy + ((p2y - p1y) / id.getWidth()), jy++)
		{
			if (renderDetail != 0)
			{
				Color col;
				ArrayList<Color> colors = new ArrayList<Color>();
				
				//This for loop in summary:
				//Take the size of the current column. 
				//Divide it into smaller pieces based on renderDetail				
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
							
							z = ZIterative(z, c);

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
					
					z = ZIterative(z, c);
					
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
	private static Complex ZIterative(Complex Z, Complex C)
	{
		switch(selectedFractal)
		{
		//Mandelbrot set
		case 0:
			Z = Z.mult(Z).add(C);
			return Z;
		//Burning ship
		case 1:
			Z = new Complex(Math.abs(Z.getR()), Math.abs(Z.getI()));
	        Z = Z.mult(Z).add(C);
	        return Z;
	    //Scattercatt
		case 2:
			Z = Z.mult(C.log());
	        Z = Z.mult(Z.tan());
	        Z = Z.add(C);
	        return Z;
	    //Butterflies
		case 3:
			Z = Z.mult(Z);
	        Z = Z.tan();
	        Z = Z.sin();
	        Z = Z.add(C);
			return Z;
		case 4:
			Z = Z.cosh();
	        Z = Z.add(C);
	        Z = Z.tan();
	        Z = Z.div(C);
	        Z = Z.log();
			return Z;
		case 5:
         Z = Z.mult(Z.div(C.log())).add(new Complex(C.getR(), C.getI()));
         break;
		case 6:
	     Z = new Complex(warbleDecimal(Z.getR()), warbleDecimal(Z.getI()));
	     Z = Z.mult(Z).add(C);
	     return Z;
		case 7:
	     Complex t = C.cosh();
	     Z = new Complex(Z.getR() % t.getR(), Z.getI() % t.getI());
	     Z = Z.mult(t.add(Z)).add(C);
	     return Z;
		case 8:
	     Complex c8 = C.log();
	     Z = new Complex(Z.getR() % c8.getR(), Z.getI() % c8.getI());
	     Z = Z.mult(c8.add(Z)).add(C);
	     return Z;
		case 9:
	     Z = new Complex(Z.getR() + getDecimal(Z.getR()), Z.getI() + getDecimal(Z.getI()));
	     Z = Z.mult(Z).add(C);
	     return Z;
		case 10:
	     double c10 = getDecimal(Z.getR()) % getDecimal(Z.getI());
	     Z = new Complex(Z.getR() + c10, Z.getI() + c10);
	     Z = Z.mult(Z).add(C);
	     return Z;
		case 11:
	     Z = Z.add(new Complex(getDecimal(Z.getR()), getDecimal(Z.getI())));
	     Z = Z.add(C);
	     return Z;
		case 12:
	     Z = Z.add(C.add(Z.mult(C)));
	     return Z;
		case 13:
	     double c13 = Z.getI()+Z.getR();
	     Z = Z.mult(new Complex((c13+Z.getR())/2,(c13+Z.getI())/2)).add(C);
	     return Z;
		case 14:
	     Z = Z.add(C.add(Z.mult(C.mult(C))));
	     return Z;
		case 15:
	     Z = Z.add(Z.mult(new Complex(Z.getR(), Z.getI()*Z.getR())));
	     Z = Z.add(C);
	     return Z;
		case 16:
	     Z = Z.add(Z.mult(new Complex(Z.getR() * Z.getI(), Z.getI()*Z.getR())));
	     Z = Z.add(C);
	     return Z;
		case 17:
	     Z = Z.add(Z.mult(new Complex(Z.getR() * Z.getI(), Z.getI() + Z.getR())));
	     Z = Z.add(C);
	     return Z;
		case 18:
	     Z = Z.mult(Z.sub(new Complex(Z.getR() * Z.getI(), Z.getI() + Z.getR())));
	     Z = Z.add(C);
	     return Z;
		case 19:
	     Z = Z.mult(Z).add(C.div(Z));
	     return Z;
		case 20:
	     Z = new Complex(Z.getR() * Z.getR() + Z.getI(), Z.getI() * Z.getI() + Z.getR());
	     Z = Z.add(C);
	     return Z;
		case 21:
	     Z = new Complex(Z.getR() * -1, Z.getI() * -1);
	     Z = new Complex(Z.getR() + Math.abs(Z.getI()), Z.getI() + Math.abs(Z.getR())).add(C);
	     return Z;
		case 22:
	     Z = new Complex( Z.getR()+Math.abs(Z.getI()) + C.getR(), Z.getI() - C.getI() + Z.getR() );
	     return Z;
		case 23:
	     Z = Z.add(C);
	     Z = new Complex(Z.getI(), Z.getR());
	     return Z;
		case 24:
	     Complex c24 = new Complex(Math.sin(Z.getR()), Math.sin(Z.getI()));
	     Z = Z.mult(c24);
	     Z = Z.add(C);
	     return Z;
		case 25:
	     Z = Z.mult(new Complex(Math.log(C.getR()), Math.log(C.getI())));
	     Z = Z.mult(new Complex(Math.tan(Z.getR()), Math.cos(Z.getI())));
	     Z = Z.add(C);
	     return Z;
		case 26:
	      Z = new Complex(Z.getR() * -1, Z.getI() * -1);
	      Z = Z.tan();
	     Z = Z.div(C);
	      Z = Z.add(Z.mult(new Complex(Z.getR() * Z.getI(), Z.getI() + Z.getR())));
	      Z = new Complex(Z.getR() + Math.abs(Z.getI()), Z.getI() + Math.abs(Z.getR())).add(C);
	     Z = Z.add(C);
	     return Z;
		case 27:
		 Z = Z.tan();
		 Z = Z.div(C);
	     return Z;
		}
		//Virtually unreachable
		return C_ZERO;
	}
	public static Color getColorFromIterations(int i) {
		return new Color(i * 10 % 255, 0, 0);
	}
	
	static BigDecimal intToBD(Integer x)
	{
		String t = x.toString();
		return new BigDecimal(t);
	}
	private static double getDecimal(double x)
	{
		return x % 1;
	}
	public static double warbleDecimal(double x)
	{
		return x * (x % 1 + 1);
	}
	public static Color averageColors(Color a, Color b)
	{
		return new Color(
				(int) Math.round(Math.sqrt((a.getRed()*a.getRed()+b.getRed()*b.getRed())/2)),
				(int) Math.round(Math.sqrt((a.getGreen()*a.getGreen()+b.getGreen()*b.getGreen())/2)),
				(int) Math.round(Math.sqrt((a.getBlue()*a.getBlue()+b.getBlue()*b.getBlue())/2))
				);
		
		
	}
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
	public static boolean verifyCurrentPalette()
	{
		return (selectedPalette < palettes.size()) ? true : false;
	}
	public static String getCurrentFractalName()
	{
		return fractalNames[selectedFractal];
	}

	public static void setPaletteShiftMode(int x)
	{
		paletteShiftMode = x;
	}
	public static int getPaletteShiftMode()
	{
		return paletteShiftMode;
	}
	
	public static void setInSetCalculator(InSetCalculator isc)
	{
		selectedInSetCalculator = isc;
	}
	
}
