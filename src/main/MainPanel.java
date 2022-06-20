package main;

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

public class MainPanel extends JPanel implements ActionListener, KeyListener {
	
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
	
	JCheckBox cb_colorInsidePoints;
	JCheckBox cb_colorOutsidePoints;
	
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
	
	MainPanel()
	{
		
		//Initializing fractals/bailouts/etc
		FractalCalculator.initializeAll();
		
		//Initializing preview palette shift windows
		for (int i = 0; i < 5; i++)
			previewPaletteShiftWindows.add(new Color[PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE][PREVIEW_PALETTE_SHIFT_WINDOWS_SIZE]);
		
		
		String x = System.getProperty("os.name");
		System.out.println(x);
		
		DataHandler.initDataDir();
		DataHandler.verifyFiles();
		
		try {
			DataHandler.read();
		} catch (IOException e1) {		
			e1.printStackTrace();
		}
		
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
		
		this.addMouseListener(new MouseAdapter() {
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
			}
			
		});
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		
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
	public static int getRenderImageSize()
	{
		return renderImageSize;
	}
	public static void setRenderImageSize(int r)
	{
		renderImageSize = r;
	}
	//Render small window at top left. Not threaded
	public void renderFinderWindow()
	{
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
	}
	public void renderJuliaWindow()
	{

		IntStream.range(0, JULIA_RENDER_WINDOW_SIZE).parallel().forEach(i -> { 
			FractalCalculator.calcFractalColumn(juliaRenderWindow, i, true, juliaPoint.getR(), juliaPoint.getI(), -1);
		});

	}
	public void renderImage(boolean julia)
	{

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
			
			if (julia)
				IntStream.range(0, renderImageSize).parallel().forEach(i -> { 
					FractalCalculator.calcFractalColumn(buff, i, true, juliaPoint.getR(), juliaPoint.getI(), -1);
				});
			
			else
				IntStream.range(0, renderImageSize).parallel().forEach(i -> { 
					FractalCalculator.calcFractalColumn(buff, i, false, 0, 0, -1);
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

	@Override
	public void keyTyped(KeyEvent e) {

	}
	
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

	@Override
	public void keyReleased(KeyEvent e) {
		
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

		
		cb_colorInsidePoints = new JCheckBox("Inside points");
		cb_colorInsidePoints.setFocusable(false);
		cb_colorInsidePoints.setSelected(FractalCalculator.getColorInsidePixels());
		cb_colorInsidePoints.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setColorInsidePixels(cb_colorInsidePoints.isSelected());
			renderFinderWindow();
		}});
		gbc.gridx = 1;
		gbc.gridy = 0;
		southPanel.add(cb_colorInsidePoints, gbc);
		
		cb_colorOutsidePoints = new JCheckBox("Outside points");
		cb_colorOutsidePoints.setFocusable(false);
		cb_colorOutsidePoints.setSelected(FractalCalculator.getColorOutsidePixels());
		cb_colorOutsidePoints.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setColorOutsidePixels(cb_colorOutsidePoints.isSelected());
			renderFinderWindow();
		}});
		gbc.gridx = 1;
		gbc.gridy = 1;
		southPanel.add(cb_colorOutsidePoints, gbc);
		
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
		
		
		JPanel eastPanelNorth = new JPanel();
		eastPanelNorth.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);
		
		//Fractal dropdown
		JLabel jl_fractalList = new JLabel("Fractals:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		eastPanelNorth.add(jl_fractalList, gbc);
		
		String[] fractalNames = FractalCalculator.getAllFractalNames();
		JComboBox jcb_fractalList = new JComboBox(fractalNames);
		
		jcb_fractalList.setSelectedIndex(0);
		jcb_fractalList.setFocusable(false);
		jcb_fractalList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedFractal(FractalCalculator.getFractalArray().get(jcb_fractalList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		eastPanelNorth.add(jcb_fractalList, gbc);
		
		
		//Palette dropdown
		JLabel jl_paletteList = new JLabel("Palettes:");
		gbc.gridx = 0;
		gbc.gridy = 1;
		eastPanelNorth.add(jl_paletteList, gbc);
		
		String[] paletteNames = FractalCalculator.getAllPaletteNames();
		JComboBox jcb_paletteList = new JComboBox(paletteNames);
		
		jcb_paletteList.setSelectedIndex(0);
		jcb_paletteList.setFocusable(false);
		jcb_paletteList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedPalette(FractalCalculator.getPaletteArray().get(jcb_paletteList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		eastPanelNorth.add(jcb_paletteList, gbc);
		
		//Bailout dropdown
		JLabel jl_bailoutList = new JLabel("Bailouts:");
		gbc.gridx = 0;
		gbc.gridy = 2;
		eastPanelNorth.add(jl_bailoutList, gbc);
		
		String[] bailoutNames = FractalCalculator.getAllBailoutNames();
		JComboBox jcb_bailoutList = new JComboBox(bailoutNames);
		
		jcb_bailoutList.setSelectedIndex(0);
		jcb_bailoutList.setFocusable(false);
		jcb_bailoutList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.setSelectedBailout(FractalCalculator.getBailoutArray().get(jcb_bailoutList.getSelectedIndex()));
			renderFinderWindow();
		}});
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		eastPanelNorth.add(jcb_bailoutList, gbc);
		
		//ISC dropdown
				JLabel jl_ISCList = new JLabel("ISCs:");
				gbc.gridx = 0;
				gbc.gridy = 3;
				eastPanelNorth.add(jl_ISCList, gbc);
				
				String[] ISCNames = FractalCalculator.getAllISCNames();
				JComboBox jcb_ISCList = new JComboBox(ISCNames);
				
				jcb_ISCList.setSelectedIndex(0);
				jcb_ISCList.setFocusable(false);
				jcb_ISCList.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
					FractalCalculator.setSelectedISC(FractalCalculator.getISCArray().get(jcb_ISCList.getSelectedIndex()));
					renderFinderWindow();
				}});
				
				gbc.gridx = 1;
				gbc.gridy = 3;
				eastPanelNorth.add(jcb_ISCList, gbc);

		/*
		btn_incOffset = new JButton("/\\");
		btn_incOffset.setFocusable(false);
		btn_incOffset.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			FractalCalculator.addToColorOffset(1);
			renderFinderWindow();
		}});
		eastPanelNorth.add(btn_incOffset);
		
		btn_decOffset = new JButton("\\/");
		btn_decOffset.setFocusable(false);
		btn_decOffset.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			if (FractalCalculator.getColorOffset() > 0)
			{
				FractalCalculator.addToColorOffset(-1);
				renderFinderWindow();
			}
		}});
		eastPanelNorth.add(btn_decOffset);

		 */
		
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(eastPanelNorth, "North");
		
		return eastPanel;

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
	public static void setPreviewPaletteShifts(boolean x)
	{
		previewPaletteShifts = x;
	}
	public static boolean getPreviewPaletteShifts()
	{
		return previewPaletteShifts;
	}
	public static void setRenderOutputPath(String x)
	{
		renderOutputPath = x;
	}
	public static String getRenderOutputPath()
	{
		return renderOutputPath;
	}
	public static int getRenderSize()
	{
		return renderImageSize;
	}
	
	private static double[] getP1()
	{
		return FractalCalculator.getCameraP1();
	}
	private static double[] getP2()
	{
		return FractalCalculator.getCameraP2();
	}
	private static double[] getJP1()
	{
		return FractalCalculator.getJuliaCameraP1();
	}
	private static double[] getJP2()
	{
		return FractalCalculator.getJuliaCameraP2();
	}
}
