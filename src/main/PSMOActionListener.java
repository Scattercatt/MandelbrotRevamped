package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PSMOActionListener implements ActionListener
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		FractalCalculator.setPaletteShiftMode(mode);
		
	}
	
}