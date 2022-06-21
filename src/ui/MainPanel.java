package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javax.swing.*;

import calc.Complex;
import calc.FractalCalculator;
import calc.MiscTools;
import calc.Palette;
import main.DataHandler;

public class MainPanel extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private static int userMonitorWidth = gd.getDisplayMode().getWidth();
	private static int userMonitorHeight = gd.getDisplayMode().getHeight();
	
	final int WINDOW_WIDTH = 1600;
	final int WINDOW_HEIGHT = 900;
	
	private static int renderImageSize = 1000;
	
	final int PREVIEW_RENDER_WINDOW_SIZE = 40;
	final int[] PREVIEW_RENDER_WINDOW_POSITION = new int[] {10, 10};
    Color[][] previewRenderWindow = new Color[PREVIEW_RENDER_WINDOW_SIZE][PREVIEW_RENDER_WINDOW_SIZE];
	
	final int LARGE_RENDER_WINDOW_SIZE = 400;
	final int[] LARGE_RENDER_WINDOW_POSITION = new int[] {60, 10};
	Color[][] largeRenderWindow = new Color[LARGE_RENDER_WINDOW_SIZE][LARGE_RENDER_WINDOW_SIZE];
	
	final int JULIA_RENDER_WINDOW_SIZE = 400;
	final int[] JULIA_RENDER_WINDOW_POSITION = new int[] {60, LARGE_RENDER_WINDOW_SIZE+20};
	Color[][] juliaRenderWindow = new Color[JULIA_RENDER_WINDOW_SIZE][JULIA_RENDER_WINDOW_SIZE];
	
	final int PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE = 50;
	final int[] PREVIEW_PALETTE_SHIFT_WINDOWS_POSITION = new int[] {480, 360};
	ArrayList<Color[][]> previewPaletteShiftWindows = new ArrayList<Color[][]>();
	
	private static boolean controlJuliaWindow = false;
	
	
	private static String renderOutputPath = "";
	
	//Positions of UI ///////////////////////////////////////////////
	final int[] POSITION_CALCTEXT = new int[] {480, 40};
	final int[] POSITION_COLORTEXT = new int[] {640, 40};
	
	
	/////////////////////////////////////////////////////////////////
	
	private Complex juliaPoint = new Complex(0, 0);
	
	private static boolean previewPaletteShifts = false;
	
	JButton btn_incPalette;
	JButton btn_decPalette;
	JButton btn_incSelectedFractal;
	JButton btn_decSelectedFractal;
	JButton btn_incBailout;
    JButton btn_decBailout;
    JButton btn_incOffset;
    JButton btn_decOffset;
	JButton btn_renderPreview;
	JButton btn_renderImage;
	JButton btn_renderJuliaPreview;
	JButton btn_confirmRenderImage;
	JButton btn_confirmJuliaRenderImage;
	
	JRadioButton rbtn_1xQuality;
	JRadioButton rbtn_2xQuality;
	JRadioButton rbtn_4xQuality;
	ButtonGroup bg_quality;

	JRadioButton rbtn_controlMain;
	JRadioButton rbtn_controlJulia;
	ButtonGroup bg_control;
	
	
	JTextField tf_imageRenderSize;
	JPanel southPanel;
	
	
	Timer frameTimer;
	
	JMenu fileMenu;
	
	JFrame errorFrame;
	JLabel errorLabel;
	
	RenderProgressThread rpThreadClass;
	
	JuliaWindowRenderer jwr;
	LargeWindowRenderer lwr;
	ImageOutputRenderer ior;
	
	JTextField l_iterationTracker;
	JTextField l_estimatedRenderTime;
	JTextField l_averageTimePerIteration;
	JTextField l_iterationsPerSecond;
	JTextField l_predictedRenderIterations;
	
	ArrayList<JTextField> textFieldArrayList = new ArrayList<JTextField>();
	
	//DEBUG
	final static boolean debugShowFillerBorders = true;
	
	private final int WAOFC = JComponent.WHEN_IN_FOCUSED_WINDOW; 
	
	MainPanel(String renderOutputPathArg)
	{
		renderOutputPath = renderOutputPathArg;
		//Initializing fractals/bailouts/etc
		FractalCalculator.initializeAll();
		
		//Initializing preview palette shift windows
		for (int i = 0; i < 5; i++)
			previewPaletteShiftWindows.add(new Color[PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE][PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE]);
		
		
		String x = System.getProperty("os.name");
		System.out.println(x);
		
		
		
		FractalCalculator.setSelectedPalette(FractalCalculator.getPaletteArray().get(0));
		
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		
		setLayout(new BorderLayout());
		
		jwr = new JuliaWindowRenderer();
		lwr = new LargeWindowRenderer();
		ior = new ImageOutputRenderer();
		
		frameTimer = new Timer(16, this);
		frameTimer.start();
		
		
		//South panel 
		JPanel southPanel = create_southPanel();
		add(southPanel, "South");
		
		JPanel eastPanel = create_eastPanel();
		add(eastPanel,BorderLayout.EAST);
		
		
		errorFrame = new JFrame("Error!");
		errorFrame.setLayout(new FlowLayout());
		errorFrame.setBounds(userMonitorWidth/2-200, userMonitorHeight/2-40, 400, 80);
		errorFrame.setResizable(false);
		errorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		errorLabel = new JLabel();
		errorFrame.add(errorLabel);
		
		
		rpThreadClass = new RenderProgressThread();
		Thread rpThread = new Thread(rpThreadClass);
		rpThread.start();
		


		//Initializing windows. 
		for (int ix = 0; ix < PREVIEW_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < PREVIEW_RENDER_WINDOW_SIZE; iy++)
			{
				previewRenderWindow[ix][iy] = Color.GRAY;
			}	
		for (int ix = 0; ix < LARGE_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < LARGE_RENDER_WINDOW_SIZE; iy++)
			{
				largeRenderWindow[ix][iy] = Color.GRAY;
			}	
		for (int ix = 0; ix < JULIA_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < JULIA_RENDER_WINDOW_SIZE; iy++)
			{
				juliaRenderWindow[ix][iy] = Color.GRAY;
			}	
		for (int j = 0; j < 5; j++)
			for (int ix = 0; ix < PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE; ix++)
				for (int iy = 0; iy < PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE; iy++)
				{
					previewPaletteShiftWindows.get(j)[ix][iy] = Color.GRAY;
				}	
		
		this.addMouseListener(new MouseAdapterModified(this) {
			@Override 
			public void mousePressed(MouseEvent e) {
				
				if (e.getButton() == MouseEvent.BUTTON1 && 
						e.getX() > LARGE_RENDER_WINDOW_POSITION[0] && 
						e.getX() < LARGE_RENDER_WINDOW_POSITION[0] + LARGE_RENDER_WINDOW_SIZE &&
						e.getY() > LARGE_RENDER_WINDOW_POSITION[1] &&
						e.getY() < LARGE_RENDER_WINDOW_POSITION[1] + LARGE_RENDER_WINDOW_SIZE
						)
				{
					double[] p1 = FractalCalculator.getCameraP1();
					double[] p2 = FractalCalculator.getCameraP2();
					double x = (p2[0] - p1[0]) * ((double)e.getX()-LARGE_RENDER_WINDOW_POSITION[0]) / (double)LARGE_RENDER_WINDOW_SIZE + p1[0];
					double y = (p2[1] - p1[1]) * ((double)e.getY()-LARGE_RENDER_WINDOW_POSITION[1]) / (double)LARGE_RENDER_WINDOW_SIZE + p1[1];
					juliaPoint = new Complex(x, y);
					
					jwr.start();
				}
				
				mp.requestFocusInWindow();
			}
			
		});
		
		final String[] MS = new String[] {"m_u", "m_d", "m_l", "m_r", "z_p", "z_n", "i_p", "i_n"};
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("UP"), MS[0]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("DOWN"), MS[1]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("LEFT"), MS[2]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("RIGHT"), MS[3]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("X"), MS[4]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("Z"), MS[5]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("W"), MS[6]);
		getInputMap(WAOFC).put(KeyStroke.getKeyStroke("Q"), MS[7]);
		
		getActionMap().put(MS[0], new MovementAction(MovementAction.UP));
		getActionMap().put(MS[1], new MovementAction(MovementAction.DOWN));
		getActionMap().put(MS[2], new MovementAction(MovementAction.LEFT));
		getActionMap().put(MS[3], new MovementAction(MovementAction.RIGHT));
		getActionMap().put(MS[4], new MovementAction(MovementAction.ZIN));
		getActionMap().put(MS[5], new MovementAction(MovementAction.ZOUT));
		getActionMap().put(MS[6], new MovementAction(MovementAction.IINC));
		getActionMap().put(MS[7], new MovementAction(MovementAction.IDEC));
		
		
		
		setVisible(true);
		
	}
	
	private class MovementAction extends AbstractAction
	{
		static final byte UP = 1;
		static final byte DOWN = 2;
		static final byte LEFT = 3;
		static final byte RIGHT = 4;
		static final byte ZIN = 5;
		static final byte ZOUT = 6;
		static final byte IINC = 7;
		static final byte IDEC = 8;
		
		private byte direction;
		
		MovementAction(byte direction)
		{
			this.direction = direction;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			double  movementDistance, zoomDistance;
			final double MOVEMENT_PERCENTAGE = 0.05, ZOOM_PERCENTAGE = 0.05;			
			double[] p1, p2;
			
			if (controlJuliaWindow)
			{
				p1 = getJP1();
				p2 = getJP2();
			}
			else
			{
				p1 = getP1();
				p2 = getP2();
			}
			
			switch(direction)
			{
			case UP:
				movementDistance = (p2[1] - p1[1]) * MOVEMENT_PERCENTAGE;
				p1[1] -= movementDistance;
				p2[1] -= movementDistance;
				renderFinderWindow();	
				return;
			case DOWN:
				movementDistance = (p2[1] - p1[1]) * MOVEMENT_PERCENTAGE;
				p1[1] += movementDistance;
				p2[1] += movementDistance;
				renderFinderWindow();	
				return;
			case LEFT:
				movementDistance = (p2[0] - p1[0]) * MOVEMENT_PERCENTAGE;
				p1[0] -= movementDistance;
				p2[0] -= movementDistance;
				renderFinderWindow();
				return;
			case RIGHT:
				movementDistance = (p2[0] - p1[0]) * MOVEMENT_PERCENTAGE;
				p1[0] += movementDistance;
				p2[0] += movementDistance;
				renderFinderWindow();	
				return;
			case ZIN:
				zoomDistance = (p2[1] - p1[1]) * ZOOM_PERCENTAGE;
				p1[0] += zoomDistance;
				p1[1] += zoomDistance;
				
				p2[0] -= zoomDistance;
				p2[1] -= zoomDistance;
				renderFinderWindow();
				return;
			case ZOUT:
				zoomDistance = (p2[1] - p1[1]) * ZOOM_PERCENTAGE;
				p1[0] -= zoomDistance;
				p1[1] -= zoomDistance;
				
				p2[0] += zoomDistance;
				p2[1] += zoomDistance;
				renderFinderWindow();
				return;
			case IINC:
				if (FractalCalculator.getMaxIterations() > 2000)
					FractalCalculator.addToMaxIterations(100);
				else if (FractalCalculator.getMaxIterations() > 30)
					FractalCalculator.addToMaxIterations(10);
				else
					FractalCalculator.addToMaxIterations(1);
				return;
			case IDEC:
				if (FractalCalculator.getMaxIterations() > 2000)
					FractalCalculator.addToMaxIterations(-100);
				else if (FractalCalculator.getMaxIterations() > 30)
					FractalCalculator.addToMaxIterations(-10);
				else if (FractalCalculator.getMaxIterations() > 0)
					FractalCalculator.addToMaxIterations(-1);
				renderFinderWindow();
				return;
				
			default:
				
				return;
			}
			
		}
		
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		
		colorFocusedJTextFields();
		updateDiagnosticLabels();
		
		this.setBackground(Color.DARK_GRAY);
		
		Graphics2D g2D = (Graphics2D) g;
		
		
		for (int ix = 0; ix < PREVIEW_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < PREVIEW_RENDER_WINDOW_SIZE; iy++)
			{
				g2D.setColor(previewRenderWindow[ix][iy]);
				g2D.drawLine(ix+PREVIEW_RENDER_WINDOW_POSITION[0], iy+PREVIEW_RENDER_WINDOW_POSITION[1], ix+PREVIEW_RENDER_WINDOW_POSITION[0], iy+PREVIEW_RENDER_WINDOW_POSITION[1]);
			}
		
		for (int ix = 0; ix < LARGE_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < LARGE_RENDER_WINDOW_SIZE; iy++)
			{
				g2D.setColor(largeRenderWindow[ix][iy]);
				g2D.drawLine(ix+LARGE_RENDER_WINDOW_POSITION[0], iy+LARGE_RENDER_WINDOW_POSITION[1], ix+LARGE_RENDER_WINDOW_POSITION[0], iy+LARGE_RENDER_WINDOW_POSITION[1]);
			}
		for (int ix = 0; ix < JULIA_RENDER_WINDOW_SIZE; ix++)
			for (int iy = 0; iy < JULIA_RENDER_WINDOW_SIZE; iy++)
			{
				g2D.setColor(juliaRenderWindow[ix][iy]);
				g2D.drawLine(ix+JULIA_RENDER_WINDOW_POSITION[0], iy+JULIA_RENDER_WINDOW_POSITION[1], ix+JULIA_RENDER_WINDOW_POSITION[0], iy+JULIA_RENDER_WINDOW_POSITION[1]);
			}
		if (previewPaletteShifts)
			for (int j = 0; j < 5; j++)
			{
				int positionX = PREVIEW_PALETTE_SHIFT_WINDOWS_POSITION[0] + j * (PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE + 10);
				int positionY = PREVIEW_PALETTE_SHIFT_WINDOWS_POSITION[1];
				g2D.setColor(Color.WHITE);
				g2D.drawString(Palette.getPaletteShiftModeString(j), positionX, positionY-1);
				
				for (int ix = 0; ix < PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE; ix++) 
					for (int iy = 0; iy < PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE; iy++)
					{
						g2D.setColor(previewPaletteShiftWindows.get(j)[ix][iy]);
						g2D.drawLine(ix+positionX, iy+positionY, ix+positionX, iy+positionY);
					}
			}
		
		/////////////////////////////////////////////////
		//CALC TEXT/////////////////////////////////////
		
		double[] p1 = getP1();
		double[] p2 = getP2();
		
		
		g2D.setColor(Color.WHITE);
		
		g2D.drawLine(POSITION_CALCTEXT[0], POSITION_CALCTEXT[1]-16, POSITION_CALCTEXT[0], POSITION_CALCTEXT[1]+100);
		
		g2D.drawString("Calc Info", POSITION_CALCTEXT[0], POSITION_CALCTEXT[1]-20);
		g2D.drawString("==================", POSITION_CALCTEXT[0], POSITION_CALCTEXT[1]-10);
		
		g2D.drawString("Fractal:", POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]);
		g2D.drawString(String.format("%s",FractalCalculator.getCurrentFractalName()), POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]+10);
		g2D.setColor(Color.WHITE);
		
		g2D.drawString("Zoom:", POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]+30);
		g2D.drawString(String.format("%6.1e",p2[0] - p1[0]), POSITION_CALCTEXT[0]+80, POSITION_CALCTEXT[1]+30);
		
		g2D.drawString("Iterations:", POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]+50);
		g2D.drawString(String.format("%d",FractalCalculator.getMaxIterations()), POSITION_CALCTEXT[0]+80, POSITION_CALCTEXT[1]+50);
		
		g2D.drawString("Bailout:", POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]+70);
		g2D.setColor(Color.GREEN);
		g2D.drawString(String.format("%s",FractalCalculator.getSelectedBailout().getName()), POSITION_CALCTEXT[0]+2, POSITION_CALCTEXT[1]+80);
		g2D.setColor(Color.WHITE);
		
		g2D.drawLine(POSITION_CALCTEXT[0]+125, POSITION_CALCTEXT[1]-16, POSITION_CALCTEXT[0]+125, POSITION_CALCTEXT[1]+100);
		/////////////////////////////////////////////////
		//COLOR TEXT/////////////////////////////////////
		
		g2D.setColor(Color.YELLOW);
		g2D.drawLine(POSITION_COLORTEXT[0], POSITION_COLORTEXT[1]-16, POSITION_COLORTEXT[0], POSITION_COLORTEXT[1]+100);
		
		g2D.drawString("Color Info", POSITION_COLORTEXT[0], POSITION_COLORTEXT[1]-20);
		g2D.drawString("==================", POSITION_COLORTEXT[0], POSITION_COLORTEXT[1]-10);
		
		g2D.drawString("Palette:", POSITION_COLORTEXT[0]+2, POSITION_COLORTEXT[1]);
		g2D.drawString(String.format("%s", FractalCalculator.getSelectedPalette().getName()), POSITION_COLORTEXT[0]+80, POSITION_COLORTEXT[1]);
		
		
		g2D.drawString("Color divs:", POSITION_COLORTEXT[0]+2, POSITION_COLORTEXT[1]+30);
		g2D.drawString(String.format("%d",FractalCalculator.getModulusColorDivision()), POSITION_COLORTEXT[0]+80, POSITION_COLORTEXT[1]+30);
		
		g2D.drawString("Color offset:", POSITION_COLORTEXT[0]+2, POSITION_COLORTEXT[1]+60);
		g2D.drawString(String.format("%d",FractalCalculator.getColorOffset()), POSITION_COLORTEXT[0]+80, POSITION_COLORTEXT[1]+60);
		
		g2D.setColor(Color.RED);
		//g2D.drawString("WARNING: This program is made to use 100% of your CPU. Responsibility for heat damage lies on the user, so do not use if you have poor CPU cooling!", 5, this.getHeight()-30);
		g2D.setColor(Color.WHITE);
		
		
		
	}
	public int getRenderImageSize()
	{
		return renderImageSize;
	}
	public void setRenderImageSize(int r)
	{
		renderImageSize = r;
	}
	//Render small window at top left. Not threaded
	public void renderFinderWindow()
	{
		FractalCalculator.resetTrackers();
		
		btn_confirmRenderImage.setEnabled(false);
		btn_confirmJuliaRenderImage.setEnabled(false);

		if (controlJuliaWindow)
			IntStream.range(0, PREVIEW_RENDER_WINDOW_SIZE).parallel().forEach(i -> { 
				FractalCalculator.calcFractalColumn(previewRenderWindow, i, true, juliaPoint.getR(), juliaPoint.getI(), -1);
			});
		else
			IntStream.range(0, PREVIEW_RENDER_WINDOW_SIZE).parallel().forEach(i -> { 
				FractalCalculator.calcFractalColumn(previewRenderWindow, i, false, 0, 0, -1);
			});
	
	}
	public void renderLargeWindow()
	{
		FractalCalculator.resetTrackers();
		FractalCalculator.startTimeTracker();
		
		IntStream.range(0, LARGE_RENDER_WINDOW_SIZE).parallel().forEach(i -> { 
			FractalCalculator.calcFractalColumn(largeRenderWindow, i, false, 0, 0, -1);
		});

		
		if (previewPaletteShifts)
		{
			for (int j = 0; j < 5; j++)
			{
				int h = j;
				IntStream.range(0, PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE).parallel().forEach(i -> { 
					FractalCalculator.calcFractalColumn(previewPaletteShiftWindows.get(h), i, false, 0, 0, h);
				});
			}
		}
		
		FractalCalculator.endTimeTracker();
	}
	public void renderJuliaWindow()
	{
		FractalCalculator.resetTrackers();
		FractalCalculator.startTimeTracker();
		
		IntStream.range(0, JULIA_RENDER_WINDOW_SIZE).parallel().forEach(i -> { 
			FractalCalculator.calcFractalColumn(juliaRenderWindow, i, true, juliaPoint.getR(), juliaPoint.getI(), -1);
		});
		
		FractalCalculator.endTimeTracker();

	}
	public void renderImage(boolean julia)
	{
		FractalCalculator.resetTrackers();
		
		RenderProgressJPanel.presetJobs(renderImageSize);
		RenderProgressJPanel.startStopwatch();
		
		rpThreadClass.open();
		
		Random rand = new Random();
		
		BufferedImage buff = new BufferedImage(renderImageSize, renderImageSize, BufferedImage.TYPE_INT_RGB);
		
		String outputName = FractalCalculator.getCurrentFractalName()+rand.nextInt()+"_"+rand.nextInt()+".png";  
		File out =  new File(renderOutputPath+outputName);
		
		if (!new File(renderOutputPath).isDirectory())
			callErrorFrame("Invalid output path!");
		else
		{
			
			IntStream.range(0, renderImageSize).parallel().forEach(i -> { 
				FractalCalculator.calcFractalColumn(buff, i, julia, juliaPoint.getR(), juliaPoint.getI(), -1);
			});
			RenderProgressJPanel.endStopwatch();
		    
			try {
				ImageIO.write(buff, "png", out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		rpThreadClass.release();
	}

	
	public void actionPerformed(ActionEvent e) {
		
		
		this.repaint();
	}

	/*
	@Override
	public void keyPressed(KeyEvent e) {
		
		double  movementDistance, zoomDistance;
		final double MOVEMENT_PERCENTAGE = 0.05, ZOOM_PERCENTAGE = 0.05;
		
		int keyCode = e.getKeyCode();
		
		double[] p1, p2;
		
		if (controlJuliaWindow)
		{
			p1 = getJP1();
			p2 = getJP2();
		}
		else
		{
			p1 = getP1();
			p2 = getP2();
		}
		
		switch (keyCode)
		{
		//Basic finder window movement controls
			case KeyEvent.VK_LEFT:
				movementDistance = (p2[0] - p1[0]) * MOVEMENT_PERCENTAGE;
				p1[0] -= movementDistance;
				p2[0] -= movementDistance;
				renderFinderWindow();
				
				break;
			case KeyEvent.VK_RIGHT:
				movementDistance = (p2[0] - p1[0]) * MOVEMENT_PERCENTAGE;
				p1[0] += movementDistance;
				p2[0] += movementDistance;
				renderFinderWindow();	
				break;
			case KeyEvent.VK_UP:
				movementDistance = (p2[1] - p1[1]) * MOVEMENT_PERCENTAGE;
				p1[1] -= movementDistance;
				p2[1] -= movementDistance;
				renderFinderWindow();	
				break;
			case KeyEvent.VK_DOWN:
				movementDistance = (p2[1] - p1[1]) * MOVEMENT_PERCENTAGE;
				p1[1] += movementDistance;
				p2[1] += movementDistance;
				renderFinderWindow();	
				break;
			case KeyEvent.VK_X:
				zoomDistance = (p2[1] - p1[1]) * ZOOM_PERCENTAGE;
				p1[0] += zoomDistance;
				p1[1] += zoomDistance;
				
				p2[0] -= zoomDistance;
				p2[1] -= zoomDistance;
				renderFinderWindow();
				break;
			case KeyEvent.VK_Z:
				zoomDistance = (p2[1] - p1[1]) * ZOOM_PERCENTAGE;
				p1[0] -= zoomDistance;
				p1[1] -= zoomDistance;
				
				p2[0] += zoomDistance;
				p2[1] += zoomDistance;
				renderFinderWindow();
				break;
			case KeyEvent.VK_Q:
				//Scaling values based on what max iterations is currently set to.
				if (FractalCalculator.getMaxIterations() > 2000)
					FractalCalculator.addToMaxIterations(-100);
				else if (FractalCalculator.getMaxIterations() > 30)
					FractalCalculator.addToMaxIterations(-10);
				else if (FractalCalculator.getMaxIterations() > 0)
					FractalCalculator.addToMaxIterations(-1);
				renderFinderWindow();
				break;
			case KeyEvent.VK_W:
				//Scaling values based on what max iterations is currently set to.
				if (FractalCalculator.getMaxIterations() > 2000)
					FractalCalculator.addToMaxIterations(100);
				else if (FractalCalculator.getMaxIterations() > 30)
					FractalCalculator.addToMaxIterations(10);
				else
					FractalCalculator.addToMaxIterations(1);
				renderFinderWindow();
				break;
			case KeyEvent.VK_A:
				if (FractalCalculator.getModulusColorDivision() > 0)
				{
					FractalCalculator.addToModulusColorDivisions(-1);
					renderFinderWindow();
				}
				break;
			case KeyEvent.VK_S:
				FractalCalculator.addToModulusColorDivisions(1);
				renderFinderWindow();
				break;
			
			
		}
	}
	*/
	
	public class LargeWindowRenderer implements Runnable{
		Thread myThread;
		public void run() {
			renderLargeWindow();
		}	
		public void start() {
			if (myThread == null)
			{
				myThread = new Thread(this);
				myThread.start();
			}
			else
			{
				myThread = null;
				myThread = new Thread(this);
				myThread.start();
			}
		}
	}
	public class JuliaWindowRenderer implements Runnable{
		Thread myThread;
		public void run() {
			renderJuliaWindow();
		}	
		public void start() {
			if (myThread == null)
			{
				myThread = new Thread(this);
				myThread.start();
			}
			else
			{
				myThread = null;
				myThread = new Thread(this);
				myThread.start();
			}
		}
	}
	public class ImageOutputRenderer implements Runnable{
		Thread myThread;
		boolean julia;
		public void run() {
			renderImage(julia);
		}	
		public void start(boolean julia) {
			this.julia = julia;
			if (myThread == null)
			{
				myThread = new Thread(this);
				myThread.start();
			}
			else
			{
				myThread = null;
				myThread = new Thread(this);
				myThread.start();
			}
		}
	}

	private void callErrorFrame(String message)
	{
		errorLabel.setText(message);
		errorFrame.setVisible(true);
		Toolkit.getDefaultToolkit().beep();
	}
	
	
	
	private JPanel create_southPanel()
	{
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridBagLayout());
		southPanel.setPreferredSize(new Dimension(100, 70));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);

		btn_renderPreview = create_btn_renderPreview_Button();
		gbc.gridx = 0;
		gbc.gridy = 0;
		southPanel.add(btn_renderPreview, gbc);
		
		
		btn_renderJuliaPreview = new JButton("Julia preview");
		btn_renderJuliaPreview.setMnemonic('J');
		btn_renderJuliaPreview.setFocusable(false);
		btn_renderJuliaPreview.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			jwr.start();
		}});
		gbc.gridx = 0;
		gbc.gridy = 1;
		southPanel.add(btn_renderJuliaPreview, gbc);
		
		JLabel jl_quality = new JLabel("Quality:");
		gbc.gridx = 2;
		gbc.gridy = 0;
		southPanel.add(jl_quality, gbc);
		
		JPanel jp_rbtn_quality = new JPanel();
		jp_rbtn_quality.setLayout(new FlowLayout());
		jp_rbtn_quality.setBorder(BorderFactory.createRaisedBevelBorder());
		rbtn_1xQuality = new JRadioButton("1x");
		rbtn_2xQuality = new JRadioButton("4x");
		rbtn_4xQuality = new JRadioButton("16x");
		bg_quality = new ButtonGroup();
		
		bg_quality.add(rbtn_1xQuality);
		bg_quality.add(rbtn_2xQuality);
		bg_quality.add(rbtn_4xQuality);

		rbtn_1xQuality.setFocusable(false);
		rbtn_1xQuality.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setRenderDetail(0);
			renderFinderWindow();
		}});
		
		rbtn_2xQuality.setFocusable(false);
		rbtn_2xQuality.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setRenderDetail(1);
			renderFinderWindow();
		}});
		
		rbtn_4xQuality.setFocusable(false);
		rbtn_4xQuality.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setRenderDetail(2);;
			renderFinderWindow();
		}});
				
		rbtn_1xQuality.setSelected(true);
		
		jp_rbtn_quality.add(rbtn_1xQuality, gbc);
		jp_rbtn_quality.add(rbtn_2xQuality, gbc);
		jp_rbtn_quality.add(rbtn_4xQuality, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		southPanel.add(jp_rbtn_quality, gbc);
		
		rbtn_controlMain = new JRadioButton("Control main");
		rbtn_controlMain.setToolTipText("Enables the control of the main fractal");
		rbtn_controlMain.setSelected(true);
		rbtn_controlMain.setFocusable(false);
		rbtn_controlMain.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			controlJuliaWindow = false;
		}});
		

		rbtn_controlJulia = new JRadioButton("Control julia");
		rbtn_controlJulia.setToolTipText("Enables the control of the julia fractal");
		rbtn_controlJulia.setSelected(false);
		rbtn_controlJulia.setFocusable(false);
		rbtn_controlJulia.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			controlJuliaWindow = true;
		}});
		
		bg_control = new ButtonGroup();
		bg_control.add(rbtn_controlMain);
		bg_control.add(rbtn_controlJulia);
		
		gbc.gridx = 3;
		gbc.gridy = 0;
		southPanel.add(rbtn_controlMain, gbc);
		gbc.gridx = 3;
		gbc.gridy = 1;
		southPanel.add(rbtn_controlJulia, gbc);
		
		btn_renderImage = new JButton("Generate Image");
		btn_renderImage.setFocusable(false);
		btn_renderImage.setToolTipText("Generate a PNG of the current fractal, julia or main window. Must confirm before starting.");
		btn_renderImage.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			btn_confirmRenderImage.setEnabled(true);
			btn_confirmJuliaRenderImage.setEnabled(true);
		}});
		gbc.gridx = 4;
		gbc.gridy = 0;
		
		southPanel.add(btn_renderImage, gbc);
		
		btn_confirmRenderImage = new JButton("Confirm Main");
		btn_confirmRenderImage.setToolTipText("Render main.");
		btn_confirmRenderImage.setEnabled(false);
		btn_confirmRenderImage.setFocusable(false);
		btn_confirmRenderImage.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			ior.start(false);
			btn_confirmRenderImage.setEnabled(false);
			btn_confirmJuliaRenderImage.setEnabled(false);
		}});
		gbc.gridx = 5;
		gbc.gridy = 0;
		southPanel.add(btn_confirmRenderImage, gbc);
		
		btn_confirmJuliaRenderImage = new JButton("Confirm Julia");
		btn_confirmJuliaRenderImage.setToolTipText("Render julia.");
		btn_confirmJuliaRenderImage.setEnabled(false);
		btn_confirmJuliaRenderImage.setFocusable(false);
		btn_confirmJuliaRenderImage.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			ior.start(true);
			btn_confirmRenderImage.setEnabled(false);
			btn_confirmJuliaRenderImage.setEnabled(false);
		}});
		gbc.gridx = 5;
		gbc.gridy = 1;
		southPanel.add(btn_confirmJuliaRenderImage, gbc);
		
		
		return southPanel;
		
	}
	private JPanel create_eastPanel()
	{
		final int PANEL_WIDTH = 310;
		
		//North panel of east
		JPanel eastPanelNorth = new JPanel();
		eastPanelNorth.setLayout(new GridBagLayout());
		eastPanelNorth.setBorder(BorderFactory.createRaisedBevelBorder());
		eastPanelNorth.setMaximumSize(new Dimension(PANEL_WIDTH, 100));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);
		
		//Fractal dropdown
		JLabel jl_fractalList = new JLabel("Fractals:");
		gbcSet(gbc, 0, 0);
		eastPanelNorth.add(jl_fractalList, gbc);
		
		String[] fractalNames = FractalCalculator.getAllFractalNames();
		JComboBox jcb_fractalList = new JComboBox(fractalNames);
		
		jcb_fractalList.setSelectedIndex(0);
		jcb_fractalList.setFocusable(false);
		jcb_fractalList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedFractal(FractalCalculator.getFractalArray().get(jcb_fractalList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbcSet(gbc, 1, 0);
		eastPanelNorth.add(jcb_fractalList, gbc);
		
		
		//Palette dropdown
		JLabel jl_paletteList = new JLabel("Palettes:");
		gbcSet(gbc, 0, 1);
		eastPanelNorth.add(jl_paletteList, gbc);
		
		String[] paletteNames = FractalCalculator.getAllPaletteNames();
		JComboBox jcb_paletteList = new JComboBox(paletteNames);
		
		jcb_paletteList.setSelectedIndex(0);
		jcb_paletteList.setFocusable(false);
		jcb_paletteList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedPalette(FractalCalculator.getPaletteArray().get(jcb_paletteList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbcSet(gbc, 1, 1);
		eastPanelNorth.add(jcb_paletteList, gbc);
		
		//Bailout dropdown
		JLabel jl_bailoutList = new JLabel("Bailouts:");
		gbcSet(gbc, 0, 2);
		eastPanelNorth.add(jl_bailoutList, gbc);
		
		String[] bailoutNames = FractalCalculator.getAllBailoutNames();
		JComboBox jcb_bailoutList = new JComboBox(bailoutNames);
		
		jcb_bailoutList.setSelectedIndex(0);
		jcb_bailoutList.setFocusable(false);
		jcb_bailoutList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedBailout(FractalCalculator.getBailoutArray().get(jcb_bailoutList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbcSet(gbc, 1, 2);
		eastPanelNorth.add(jcb_bailoutList, gbc);
		
		//ISC dropdown
		JLabel jl_ISCList = new JLabel("ISCs:");
		gbcSet(gbc, 0, 3);
		eastPanelNorth.add(jl_ISCList, gbc);
		
		String[] ISCNames = FractalCalculator.getAllISCNames();
		JComboBox jcb_ISCList = new JComboBox(ISCNames);
		jcb_ISCList.setEnabled(false);
		jcb_ISCList.setSelectedIndex(0);
		jcb_ISCList.setFocusable(false);
		jcb_ISCList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedISC(FractalCalculator.getISCArray().get(jcb_ISCList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbcSet(gbc, 1, 3);
		eastPanelNorth.add(jcb_ISCList, gbc);
		
	
		JCheckBox cb_colorInsidePoints = new JCheckBox("Inside points");
		cb_colorInsidePoints.setFocusable(false);
		cb_colorInsidePoints.setSelected(FractalCalculator.getColorInsidePixels());
		cb_colorInsidePoints.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setColorInsidePixels(cb_colorInsidePoints.isSelected());
			jcb_ISCList.setEnabled(cb_colorInsidePoints.isSelected());
			renderFinderWindow();
		}});
		gbcSet(gbc, 0, 4);
		eastPanelNorth.add(cb_colorInsidePoints, gbc);
		
		JCheckBox cb_colorOutsidePoints = new JCheckBox("Outside points");
		cb_colorOutsidePoints.setFocusable(false);
		cb_colorOutsidePoints.setSelected(FractalCalculator.getColorOutsidePixels());
		cb_colorOutsidePoints.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setColorOutsidePixels(cb_colorOutsidePoints.isSelected());
			renderFinderWindow();
		}});
		gbcSet(gbc, 0, 5);
		eastPanelNorth.add(cb_colorOutsidePoints, gbc);
		
		//Center panel of east
		JPanel eastPanelCenter = new JPanel();
		eastPanelCenter.setLayout(new GridBagLayout());
		eastPanelCenter.setMaximumSize(new Dimension(PANEL_WIDTH, 400));
		eastPanelCenter.setBorder(BorderFactory.createRaisedBevelBorder());
		final int TEXT_FIELD_SIZE = 9;
		final int FILLER_SIZE_X = 72;
		
		
		///////// Iterations text field
		JLabel l_iterations = new JLabel("Iterations:");
		
		gbcSet(gbc, 0, 0);
		eastPanelCenter.add(l_iterations, gbc);
		
		JTextField tf_iterations = new JTextField(TEXT_FIELD_SIZE);
		textFieldArrayList.add(tf_iterations);
		tf_iterations.setText(String.valueOf(FractalCalculator.getMaxIterations()));
		tf_iterations.addActionListener(new TextActionListener(this){@Override public void actionPerformed(ActionEvent e) {
			
			try {
				int t = Integer.parseInt(tf_iterations.getText());
				if (t < 0) throw new Exception();
				FractalCalculator.setMaxIterations(t);
			} catch (Exception ex)
			{
				callErrorFrame("Input must be non-negative integer");
				tf_iterations.setText(String.valueOf(FractalCalculator.getMaxIterations()));
			}
			
			mp.renderFinderWindow();
			mp.requestFocusInWindow();
		}});
		
		gbcSet(gbc, 1, 0);
		eastPanelCenter.add(tf_iterations, gbc);
		
		JPanelFiller rfiller1 = new JPanelFiller(FILLER_SIZE_X, 20);
		gbcSet(gbc, 2, 0);
		eastPanelCenter.add(rfiller1, gbc);
		
		///////// Iterations Till Loop text field
		JLabel l_iterationsTillLoop = new JLabel("ITL:");
		
		gbcSet(gbc, 0, 1);
		eastPanelCenter.add(l_iterationsTillLoop, gbc);
		
		JTextField tf_iterationsTillLoop = new JTextField(TEXT_FIELD_SIZE);
		textFieldArrayList.add(tf_iterationsTillLoop);
		tf_iterationsTillLoop.setText(String.valueOf(FractalCalculator.getModulusColorDivision()));
		tf_iterationsTillLoop.addActionListener(new TextActionListener(this){@Override public void actionPerformed(ActionEvent e) {
			
			try {
				int t = Integer.parseInt(tf_iterationsTillLoop.getText());
				if (t < 0) throw new Exception();
				FractalCalculator.setModulusColorDivision(t);
				
			} catch (Exception ex)
			{
				callErrorFrame("Input must be non-negative be integer");
				tf_iterationsTillLoop.setText(String.valueOf(FractalCalculator.getModulusColorDivision()));
			}
			
			mp.renderFinderWindow();
			mp.requestFocusInWindow();
		}});
		
		gbcSet(gbc, 1, 1);
		eastPanelCenter.add(tf_iterationsTillLoop, gbc);
		
		JPanelFiller rfiller2 = new JPanelFiller(FILLER_SIZE_X, 20);
		gbcSet(gbc, 2, 1);
		eastPanelCenter.add(rfiller2, gbc);
		
		//////////// Offset text field
		JLabel l_offset = new JLabel("Color Offset:");
		
		gbcSet(gbc, 0, 2);
		eastPanelCenter.add(l_offset, gbc);
		
		JTextField tf_offset = new JTextField(TEXT_FIELD_SIZE);
		textFieldArrayList.add(tf_offset);
		tf_offset.setText(String.valueOf(FractalCalculator.getColorOffset()));
		tf_offset.addActionListener(new TextActionListener(this){@Override public void actionPerformed(ActionEvent e) {
			
			try {
				int t = Integer.parseInt(tf_offset.getText());
				if (t < 0) throw new Exception();
				FractalCalculator.setColorOffset(t);
				
			} catch (Exception ex)
			{
				callErrorFrame("Input must be non-negative be integer");
				tf_offset.setText(String.valueOf(FractalCalculator.getColorOffset()));
			}
			
			mp.renderFinderWindow();
			mp.requestFocusInWindow();
		}});
		
		gbcSet(gbc, 1, 2);
		eastPanelCenter.add(tf_offset, gbc);
		
		JPanelFiller rfiller3 = new JPanelFiller(FILLER_SIZE_X, 20);
		gbcSet(gbc, 2, 2);
		eastPanelCenter.add(rfiller3, gbc);
		
		
		JPanel eastPanelSouth = new JPanel();
		eastPanelSouth.setLayout(new GridBagLayout());
		eastPanelSouth.setBorder(BorderFactory.createRaisedBevelBorder());
		eastPanelSouth.setMaximumSize(new Dimension(PANEL_WIDTH, 100));
		
		JLabel l_iterationTrackerLabel = new JLabel("IC:");
		l_iterationTracker = new JTextField(7);
		l_iterationTracker.setEditable(false);
		
		JLabel l_predictedRenderIterationsLabel = new JLabel("PRI:");
		l_predictedRenderIterations = new JTextField(7);
		l_predictedRenderIterations.setEditable(false);
		
		JLabel l_estimatedRenderTimeLabel = new JLabel("ERT:");
		l_estimatedRenderTime = new JTextField(7);
		l_estimatedRenderTime.setEditable(false);
		
		JLabel l_averageTimePerIterationLabel = new JLabel("ATPI:");
		l_averageTimePerIteration = new JTextField(7);
		l_averageTimePerIteration.setEditable(false);
		
		JLabel l_iterationsPerSecondLabel = new JLabel("IPS:");
		l_iterationsPerSecond = new JTextField(7);
		l_iterationsPerSecond.setEditable(false);
		
		
		
		gbcSet(gbc, 0, 0);
		eastPanelSouth.add(l_iterationTrackerLabel, gbc);
		gbcSet(gbc, 1, 0);
		eastPanelSouth.add(l_iterationTracker, gbc);
		gbcSet(gbc, 0, 1);
		eastPanelSouth.add(l_predictedRenderIterationsLabel, gbc);
		gbcSet(gbc, 1, 1);
		eastPanelSouth.add(l_predictedRenderIterations, gbc);
		gbcSet(gbc, 2, 0);
		eastPanelSouth.add(l_averageTimePerIterationLabel, gbc);
		gbcSet(gbc, 3, 0);
		eastPanelSouth.add(l_averageTimePerIteration, gbc);
		gbcSet(gbc, 2, 1);
		eastPanelSouth.add(l_iterationsPerSecondLabel, gbc);
		gbcSet(gbc, 3, 1);
		eastPanelSouth.add(l_iterationsPerSecond, gbc);
		gbcSet(gbc, 0, 2);
		eastPanelSouth.add(l_estimatedRenderTimeLabel, gbc);
		gbcSet(gbc, 1, 2);
		eastPanelSouth.add(l_estimatedRenderTime, gbc);

		JPanelFiller bfiller = new JPanelFiller(10, 610);
		JPanelFiller tfiller = new JPanelFiller(10, 5);
		JPanelFiller mfiller1 = new JPanelFiller(10, 10);
		JPanelFiller mfiller2 = new JPanelFiller(10, 10);
		
		
		
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
		eastPanel.add(tfiller);
		eastPanel.add(eastPanelNorth);
		eastPanel.add(mfiller1);
		eastPanel.add(eastPanelCenter);
		eastPanel.add(mfiller2);
		eastPanel.add(eastPanelSouth);
		eastPanel.add(bfiller);

		return eastPanel;

	}
	public void updateDiagnosticLabels()
	{
		l_iterationTracker.setText(String.format("%.1fM", FractalCalculator.getIterationTracker() / 1000000f));
		double pri = Math.pow(renderImageSize, 2) * ((double) FractalCalculator.getIterationTracker() / Math.pow(LARGE_RENDER_WINDOW_SIZE, 2));
		l_predictedRenderIterations.setText(String.format("%.1fM", pri / 1000000));
		
		if (FractalCalculator.getActiveTracker())
		{	
			double atpi = (System.nanoTime() - FractalCalculator.getTimeStartTrackerNano()) / (double) FractalCalculator.getIterationTracker();
			l_averageTimePerIteration.setText(String.format("%.1fns", atpi));
			double ips = (double) FractalCalculator.getIterationTracker() / ((double)(System.currentTimeMillis() - FractalCalculator.getTimeStartTrackerMillis()) / 1000.0);
			l_iterationsPerSecond.setText(String.format("%.1fM", ips / 1000000.0));
			
			double ert = pri / ips;
			l_estimatedRenderTime.setText(String.format("%s", MiscTools.millisToStringTime((long)(ert * 1000))));
		}
	}
	
	private JButton create_btn_renderPreview_Button() {
		JButton button = new JButton("Render Preview");
		button.setFocusable(false);
		button.setToolTipText("Render the current position in the main preview window.");
		button.setMnemonic(KeyEvent.VK_E);
		
		button.addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						lwr.start();
					}
				}
		);
		
		return button;
		
	}
	
	private void colorFocusedJTextFields()
	{
		for (JTextField tf : textFieldArrayList)
		{
			if (tf.isFocusOwner())
				tf.setBackground(Color.YELLOW);
			else
				tf.setBackground(Color.WHITE);
		}
	}
	
	private void gbcSet(GridBagConstraints gbc, int x, int y)
	{
		gbc.gridx = x;
		gbc.gridy = y;
	}
	public void setPreviewPaletteShifts(boolean x)
	{
		previewPaletteShifts = x;
	}
	public boolean getPreviewPaletteShifts()
	{
		return previewPaletteShifts;
	}
	public void setRenderOutputPath(String x)
	{
		renderOutputPath = x;
	}
	public String getRenderOutputPath()
	{
		return renderOutputPath;
	}
	public int getRenderSize()
	{
		return renderImageSize;
	}
	
	private double[] getP1()
	{
		return FractalCalculator.getCameraP1();
	}
	private double[] getP2()
	{
		return FractalCalculator.getCameraP2();
	}
	private double[] getJP1()
	{
		return FractalCalculator.getJuliaCameraP1();
	}
	private double[] getJP2()
	{
		return FractalCalculator.getJuliaCameraP2();
	}
}
