package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
public class RenderProgressJPanel extends JPanel implements ActionListener {
	
	
	/*
	 * This class effectively runs the small window that displays information whenever a render is in progress. This calculates all the diagnostic values. 
	 */
	
	private static final long serialVersionUID = 1L;
	
	//Max number of column representations. (Trust me, you don't need to render an image more than 50,000 x 50,000)
	private static final int PROGRESS_STATES_SIZE = 50000;
	
	//Draw cords
	private static final int[] PROGRESS_DISPLAY_POSTITION = new int[] {10,30};
	
	static byte[] progressStates = new byte[PROGRESS_STATES_SIZE];
	// 0 = No job
	// 1 = Unstarted job
	// 2 = Unfinished job
	// 3 = Finished job

	static float progressPercentage = 0;
	static long ic = 0; //Iterations calculated
	static long pc = 0; //Pixels calculated
	static double atpi = 0.0; //Average time per iteration
	static double ips = 0.0; //Iterations per second
	static boolean boolUpdateDiagnostics = false; //Controls whether or not ATPI is being updated.
	
	//Time vars.
	static long startNano; 
	static long startMillis, endMillis, elapsedMillis;
	
	private static Timer frameTimer;
	
	public RenderProgressJPanel()
	{		
		
		
		frameTimer = new Timer(16, this);
		frameTimer.start();
		
		//Initialize progressStates as "no job"
		for (int i = 0; i < progressStates.length; i++)
			progressStates[i] = 0;

	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		
		if (boolUpdateDiagnostics)
		{
			updateATPI();
			updateIPS();
			updateProgressPercent();
		}
		this.setBackground(Color.BLACK);
		
		

		if (progressPercentage < 100)
			elapsedMillis = System.currentTimeMillis() - startMillis;
		else
			elapsedMillis = endMillis - startMillis;
		
		Graphics2D g2D = (Graphics2D) g;
		
		g2D.setColor(Color.RED);
		g2D.drawString("RENDER PROGRESS:", 10, 20);
		
		g2D.setColor(new Color(255 - (int)Math.round(255.0f * (progressPercentage / 100.0f)), (int)Math.round(255.0f * (progressPercentage / 100.0f)), 0));
		g2D.drawString(String.format("%.2f%%",progressPercentage), 150, 20);
		
		g2D.setColor(Color.RED);
		g2D.drawString(String.format("ELAPSED TIME: %s", millisToStringTime(elapsedMillis)), 250, 20);
		
		g2D.drawString(String.format("IC:  %,d", ic), 10, 140);
		
		g2D.drawString(String.format("PC: %,d", pc), 10, 155);
		
		
		g2D.drawString(String.format("ATPI: %.2f", atpi), 160, 140);
		
		g2D.drawString(String.format("IPS: %.2fM", ips/1000000.0), 160, 155);
		
		//Fractal info
		g2D.drawLine(0, 160, this.getWidth(), 160);
		
		g2D.drawString(String.format("FRACTAL    : %s", FractalCalculator.getSelectedFractal().getName()), 10, 175);
		g2D.drawString(String.format("BAILOUT    : todo"), 10, 190);
		g2D.drawString(String.format("PALETTE    : %s", FractalCalculator.getSelectedPalette().getName()), 10, 205);
		g2D.drawString(String.format("ITERATIONS : %d", FractalCalculator.getMaxIterations()), 10, 220);
		g2D.drawString(String.format("IMSIZE     : %d", MainPanel.getRenderImageSize()), 10, 235);
		
		int renderDetailRepresentation = (int) Math.round(Math.pow(2, FractalCalculator.getRenderDetail()) * Math.pow(2, FractalCalculator.getRenderDetail()));
		g2D.drawString(String.format("DETAIL LVL : %dx", renderDetailRepresentation), 10, 250);
		
		for (int ix = 0, i = 0; ix < 500; ix++)
		{
			for (int iy = 0; iy < 100; iy++, i++)
			{
				
				g2D.setColor(getStateColor(progressStates[i]));
				g2D.drawLine(ix+PROGRESS_DISPLAY_POSTITION[0], iy+PROGRESS_DISPLAY_POSTITION[1], ix+PROGRESS_DISPLAY_POSTITION[0], iy+PROGRESS_DISPLAY_POSTITION[1]);
			}
		}
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
		this.repaint();
	}
	
	public static void setJob(int pos, byte state)
	{
		progressStates[pos] = state;
	}
	public static void presetJobs(int amount)
	{
		ic = 0; pc = 0;
		
		for (int i = 0; i < PROGRESS_STATES_SIZE; i++)
			if (i < amount)
				progressStates[i] = (byte) 1;
			else
				progressStates[i] = (byte) 0;
	}
	private static Color getStateColor(byte b)
	{
		switch(b)
		{
		case 0:
			return Color.DARK_GRAY;
		case 1:
			return new Color(100,0,0);
		case 2:
			return Color.WHITE;
		case 3:
			return new Color(0,100,0);
		}
		return Color.MAGENTA;
	}
	/**
	 Updates the percent completion
	 */
	private static void updateProgressPercent()
	{
		float complete = 0, total = 0;
		
		for(byte i : progressStates)
			if (i == 3)
			{
				complete += 1.0f;
				total += 1.0f;
			}
			else if (i == 1 || i == 2)
				total += 1.0f;
		
		progressPercentage = complete / total * 100.0f;
		
	}
	private static void updateATPI()
	{
		atpi = (double) (System.nanoTime() - startNano) / (double) ic;
	}
	private static void updateIPS()
	{
		ips = (double) ic / (((double) System.currentTimeMillis() - (double) startMillis) / 1000.0);
	}
	
	public static void startStopwatch()
	{
		boolUpdateDiagnostics = true;
		startNano = System.nanoTime();
		startMillis = System.currentTimeMillis();
	}
	public static void endStopwatch()
	{
		boolUpdateDiagnostics = false;
		endMillis = System.currentTimeMillis();
		
		//This is called here to make sure the window displays 100% when finished. 
		updateProgressPercent();

	}
	public static void incIC()
	{
		ic++;
	}
	public static void incPC()
	{
		pc++;
	}
	private static String millisToStringTime(long millis)
	{
		String SS = String.format("%02d",(millis/1000l) % 60);
		String MM = String.format("%02d",(millis/60000l) % 60);
		String HH = String.format("%02d",(millis/3600000l));
		
		return String.format("%s:%s:%s", HH, MM, SS);
	}
}
