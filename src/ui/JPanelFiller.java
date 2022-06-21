package ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class JPanelFiller extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean debugShowBorders = false;
	
	public JPanelFiller(int x, int y)
	{
		if (debugShowBorders)
			this.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
		
		this.setPreferredSize(new Dimension(x, y));
	}

}
