package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ISCActionListener implements ActionListener {
	
	InSetCalculator me;
	
	public ISCActionListener(InSetCalculator me)
	{
		
		this.me = me;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		FractalCalculator.setInSetCalculator(me);
	}
	
}
