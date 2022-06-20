package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

public class RenderProgressThread implements Runnable {
	
	private static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private static int userMonitorWidth = gd.getDisplayMode().getWidth();
	private static int userMonitorHeight = gd.getDisplayMode().getHeight();
	
	JFrame renderProgressFrame;
	
	boolean flagOpen, flagClose, flagRelease;
	
	RenderProgressJPanel rp_mainPanel;
	
	@Override
	public void run() {
		renderProgressFrame = new JFrame("Render Progress...");
		rp_mainPanel = new RenderProgressJPanel();
		renderProgressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		renderProgressFrame.setResizable(false);
		renderProgressFrame.setBounds(userMonitorWidth/2, userMonitorHeight/2, 535, 300);
		renderProgressFrame.add(rp_mainPanel);
		
		while(true)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (flagOpen)
			{
				_open();
				flagOpen = false;
			}
			if (flagClose)
			{
				_close();
				flagClose = false;
			}
			if (flagRelease)
			{
				_release();
				flagRelease = false;
			}
			
		}
	}
	
	private void _open()
	{
		renderProgressFrame.setVisible(true);
		renderProgressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	private void _close()
	{
		renderProgressFrame.setVisible(false);
	}
	private void _release()
	{
		renderProgressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void open()
	{
		flagOpen = true;
	}
	public void close()
	{
		flagClose = true;
	}
	public void release()
	{
		flagRelease = true;
	}
	
	

}
