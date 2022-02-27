package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InSetCalculator {
	/**
	 * The InSetCalculator class handles the different operations used to calculated points within a fractal set. calculteVariable and calculateMax are made to be overridden, and guide the rules of the calculations
	 * @author Gavinr1
	 */
	
	//This static list is all of the different methods i've come up with.
	//This list along with the ISCActionListener class is built in a way to allow for any amount of InSetCalculators (ISCs) to be made with ease.
	private static ArrayList<InSetCalculator> iscList = new ArrayList<InSetCalculator>();
	
	//Name of the ISC. Required for the menu options
	String name;
	
	public InSetCalculator(String name)
	{
		this.name = name;
	}
	
	/**
	 * Calculates the dependant variable given a set of points.
	 * 
	 * @param c ArrayList of complex points
	 * @return Integer variable num
	 * @implNote MUST BE OVERIDDEN
	 */
	public int calculateVariable(ArrayList<Complex> c)
	{
		return 1;
	}
	
	/**
	 * Calculates the max number for the ruleset. This is the number that the dependant number will be compared to. This number is usually fixed.
	 * 
	 * @param c ArrayList of complex points
	 * @return Integer max num
	 * @implNote MUST BE OVERIDDEN
	 */
	public int calculateMax(ArrayList<Complex> c)
	{
		return 10;
	}
	
	/**
	 * @return Name of ISC
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * Simple 2 point distance method.
	 * @param c ArrayList of complex points
	 * @param index1 Index of point 1
	 * @param index2 Index of point 2
	 * @return Distance between the point at index 1 and the point at index 2
	 */
	protected double distance(ArrayList<Complex> c, int index1, int index2)
	{
		return Math.sqrt(
				Math.pow(c.get(index1).getR()-c.get(index2).getR(),2) + Math.pow(c.get(index1).getI()-c.get(2).getI(), 2) 
				);
	}
	
	/**
	 * Gets the length of the longest distance between the list of points, going from 0 to size()-1 elemenets of the list
	 * @param c ArrayList of complex points
	 * @return Longest distance between two consecutive points
	 */
	
	protected double getLongestDistance(ArrayList<Complex> c)
	{
		double longest = 0.0;
		
		for (int i = 0; i < c.size()-1; i++)
			longest = (distance(c, i, i+1) > longest) ? distance(c, i, i+1) : longest;
		
		return longest;
	}
	
	/**
	 * Gets the index of the starting point of the longest distance between 2 points, going from 0 to size()-1 elements of the list
	 * @param c ArrayList of complex points
	 * @return Index of the starting point of the longest distance between 2 points
	 */
	protected int getLongestPosition(ArrayList<Complex> c)
	{
		double longest = 0;
		int longestPos = 0;
		
		for (int i = 0; i < c.size()-1; i++)
		{
			double d = distance(c, i, i+1);
			if (d > longest)
			{
				longest = d;
				longestPos = i;
			}
		}
		return longestPos;
	}
	
	/**
	 * 
	 * @return ArrayList of ISCs 
	 */
	public static ArrayList<InSetCalculator> getList()
	{
		return iscList;
	}
	
	/**
	 * Initializes all of the pre-written ISCs. To create more, go here!
	 */
	public static void initializeList()
	{
		iscList.add(new InSetCalculator("Average Distance")
		{
			@Override
			public int calculateVariable(ArrayList<Complex> c)
			{
				double sum = 0.0;
				for (int i = 0; i < c.size() - 1; i++)
				{
					sum += distance(c, i, i+1);
				}
				return (int) Math.round(sum / (double) c.size() * 1000.0);
			}
			@Override
			public int calculateMax(ArrayList<Complex> c)
			{
				return 100;
			}
		
		});
		iscList.add(new InSetCalculator("First Last Distance")		
		{
			@Override
			public int calculateVariable(ArrayList<Complex> c)
			{
				return (int) Math.round(distance(c, 0, c.size()-1) * 1000.0);
			}
			@Override
			public int calculateMax(ArrayList<Complex> c)
			{
				return 100;
			}
		});
		iscList.add(new InSetCalculator("Longest distance")		
		{
			@Override
			public int calculateVariable(ArrayList<Complex> c)
			{
				return getLongestPosition(c);
			}
			@Override
			public int calculateMax(ArrayList<Complex> c)
			{
				return c.size();
			}
		});
		iscList.add(new InSetCalculator("Test")
		{
			@Override
			public int calculateVariable(ArrayList<Complex> c)
			{
				return (int) Math.round(distance(c, 0, c.size()-1) * (1000.0*c.size()));
			}
			@Override
			public int calculateMax(ArrayList<Complex> c)
			{
				return 1000*c.size();
			}
		
		});
	}
}
