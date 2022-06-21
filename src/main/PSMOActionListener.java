package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import calc.FractalCalculator;

public class PSMOActionListener implements ActionListener
{
	int mode;
	/**
	 * 
	 * @param mode Palette shift mode for this instance
	 */
	public PSMOActionListener(int mode)
	{
		this.mode = mode;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		FractalCalculator.setPaletteShiftMode(mode);
		
	}
	
}