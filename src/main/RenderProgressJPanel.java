package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
public class RenderProgressJPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private static final int PROGRESS_STATES_SIZE = 50000;
	private static final int[] PROGRESS_DISPLAY_POSTITION = new int[] {10,30};
	
	static byte[] progressStates = new byte[PROGRESS_STATES_SIZE];
	// 0 = No job
	// 1 = Unstarted job
	// 2 = Unfinished job
	// 3 = Finished job

	static float progressPercentage = 0;
	static long ic = 0; //Iterations calculated
	static long pc = 0;
	static double atpi = 0.0; 
	static boolean boolUpdateATPI = false;
	
	static long startNano;
	static long startMillis, endMillis, elapsedMillis;
	
	private static Timer frameTimer;
	
	public RenderProgressJPanel()
	{		
		
		
		frameTimer = new Timer(16, this);
		frameTimer.start();
		
		for (int i = 0; i < progressStates.length; i++)
			progressStates[i] = 0;

	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		updateProgressPercent();
		if (boolUpdateATPI)
			updateATPI();
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
		if (progressPercentage < 100)
			atpi = (double) (System.nanoTime() - startNano) / (double) ic;
	}
	
	public static void startStopwatch()
	{
		boolUpdateATPI = true;
		startNano = System.nanoTime();
		startMillis = System.currentTimeMillis();
		
	}
	public static void endStopwatch()
	{
		boolUpdateATPI = false;
		endMillis = System.currentTimeMillis();

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
