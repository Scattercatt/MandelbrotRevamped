package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Driver {
	
	
	
	public static void main(String[] args) {
		
		System.out.println(String.format("%,d", Runtime.getRuntime().maxMemory()));
		
		
		
		JFrame f = new JFrame("Mandelbrot Revamped 2.0");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MainPanel mp = new MainPanel();
		
		f.add(mp);
		f.addKeyListener(mp);
		f.setSize(1000,600);
		f.setVisible(true);
		f.setJMenuBar(create_menuBar());
		
		
		//FractPoint t1 = new FractPoint(new Apfloat(1, 10), new Apfloat(-2, 10));
		//System.out.println(t1.abs());
	
		
		mp.renderFinderWindow();
		
		
		f.pack();
	}
	private static JMenuBar create_menuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(create_fileMenu());
		menuBar.add(create_editMenu());
		menuBar.add(create_viewMenu());
		return menuBar;
		
	}
	private static JMenu create_viewMenu() {
		JMenu viewMenu = new JMenu("View");
		
		JCheckBoxMenuItem previewPaletteShifts = new JCheckBoxMenuItem("Preview palette shifts");
		previewPaletteShifts.setSelected(false);
		previewPaletteShifts.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			MainPanel.setPreviewPaletteShifts(!MainPanel.getPreviewPaletteShifts());
		}});
		viewMenu.add(previewPaletteShifts);
		
		
		return viewMenu;
		
	}
	
	/*
	 * This function creates the file dropdown menu at the top page. This has to be in Driver.java because JMenusu must be on a JFrame and not a JPanel
	 */
	private static JMenu create_fileMenu() {
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem setOutputPath = new JMenuItem("Set output path");
		setOutputPath.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			JFrame prompt_setOutput = new JFrame("Enter output path");
			
			prompt_setOutput.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextField field = new JTextField();
			field.setText(MainPanel.getRenderOutputPath());
			prompt_setOutput.add(field);
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			prompt_setOutput.setBounds(width/2-200, height/2-30, 400, 60);
			prompt_setOutput.setVisible(true);
			prompt_setOutput.setResizable(false);
			
			field.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
				if (new File(field.getText()).isDirectory())
				{
					MainPanel.setRenderOutputPath(field.getText());
					try {
						DataHandler.write();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					prompt_setOutput.dispose();
				}
			}});
			
		}});
		fileMenu.add(setOutputPath);
		
		
		return fileMenu;
		
	}
	/*
	 * Edit JMenu creation
	 */
	private static JMenu create_editMenu() {
		JMenu editMenu = new JMenu("Edit");
		
		JMenuItem setIterations = new JMenuItem("Iterations");
		setIterations.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			JFrame prompt_setOutput = new JFrame("Enter Iterations");
			
			prompt_setOutput.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextField field = new JTextField();
			field.setText(String.format("%d",FractalCalculator.getMaxIterations()));
			prompt_setOutput.add(field);
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			prompt_setOutput.setBounds(width/2-200, height/2-30, 400, 60);
			prompt_setOutput.setVisible(true);
			prompt_setOutput.setResizable(false);
			
			field.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
				FractalCalculator.setMaxIterations(Integer.parseInt(field.getText()));
				prompt_setOutput.dispose();
			}});
			
		}});
		editMenu.add(setIterations);
		
		JMenuItem setColorDivs = new JMenuItem("Color division mark");
		setColorDivs.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			JFrame prompt_setOutput = new JFrame("Enter color division mark");
			
			prompt_setOutput.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextField field = new JTextField();
			field.setText(String.format("%d",FractalCalculator.getModulusColorDivision()));
			prompt_setOutput.add(field);
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			prompt_setOutput.setBounds(width/2-200, height/2-30, 400, 60);
			prompt_setOutput.setVisible(true);
			prompt_setOutput.setResizable(false);
			
			field.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
				FractalCalculator.setModulusColorDivision(Integer.parseInt(field.getText()));
				prompt_setOutput.dispose();
			}});
			
		}});
		editMenu.add(setColorDivs);
		
		JMenuItem setImageOutSize = new JMenuItem("Image render size");
		setImageOutSize.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			JFrame prompt_setOutput = new JFrame("Enter the resolution of the output image render");
			
			prompt_setOutput.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextField field = new JTextField();
			field.setText(String.format("%d",MainPanel.getRenderImageSize()));
			prompt_setOutput.add(field);
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			prompt_setOutput.setBounds(width/2-200, height/2-30, 400, 60);
			prompt_setOutput.setVisible(true);
			prompt_setOutput.setResizable(false);
			
			field.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
				MainPanel.setRenderImageSize(Integer.parseInt(field.getText()));
				prompt_setOutput.dispose();
			}});
			
		}});
		editMenu.add(setImageOutSize);
		
		JMenuItem setColorOffset = new JMenuItem("Color offset");
		setColorOffset.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
			JFrame prompt_setOutput = new JFrame("Enter color offset");
			
			prompt_setOutput.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextField field = new JTextField();
			field.setText(String.format("%d",FractalCalculator.getColorOffset()));
			prompt_setOutput.add(field);
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			prompt_setOutput.setBounds(width/2-200, height/2-30, 400, 60);
			prompt_setOutput.setVisible(true);
			prompt_setOutput.setResizable(false);
			
			field.addActionListener(new ActionListener(){@Override public void actionPerformed(ActionEvent e) {
				FractalCalculator.setColorOffset(Integer.parseInt(field.getText()));
				prompt_setOutput.dispose();
			}});
			
		}});
		editMenu.add(setColorOffset);
		
		JMenu setPaletteShiftModeMenu = new JMenu("Palette shift");
		//psmo = palette shift menu options
		JRadioButtonMenuItem psmo_RGB = new JRadioButtonMenuItem("RGB");
		JRadioButtonMenuItem psmo_BRG = new JRadioButtonMenuItem("BRG");
		JRadioButtonMenuItem psmo_GBR = new JRadioButtonMenuItem("GBR");
		JRadioButtonMenuItem psmo_RBG = new JRadioButtonMenuItem("RBG");
		JRadioButtonMenuItem psmo_BGR = new JRadioButtonMenuItem("BGR");
		
		psmo_RGB.addActionListener(new PSMOActionListener(0));
		psmo_RGB.setSelected(true);
		psmo_BRG.addActionListener(new PSMOActionListener(1));
		psmo_GBR.addActionListener(new PSMOActionListener(2));
		psmo_RBG.addActionListener(new PSMOActionListener(3));
		psmo_BGR.addActionListener(new PSMOActionListener(4));
		
		ButtonGroup psmoGroup = new ButtonGroup();
		psmoGroup.add(psmo_RGB);
		psmoGroup.add(psmo_BRG);
		psmoGroup.add(psmo_GBR);
		psmoGroup.add(psmo_RBG);
		psmoGroup.add(psmo_BGR);
		
		setPaletteShiftModeMenu.add(psmo_RGB);
		setPaletteShiftModeMenu.add(psmo_BRG);
		setPaletteShiftModeMenu.add(psmo_GBR);
		setPaletteShiftModeMenu.add(psmo_RBG);
		setPaletteShiftModeMenu.add(psmo_BGR);
		editMenu.add(setPaletteShiftModeMenu);
		
		JMenu setInSetCalculator = new JMenu("In-set Calculator");
		ArrayList<JRadioButtonMenuItem> iscRadioButtons = new ArrayList<JRadioButtonMenuItem>();
		ButtonGroup iscGroup = new ButtonGroup();
		
		for (int i = 0; i < InSetCalculator.getList().size(); i++)
		{
			//Stupid variable name. In Set Calculator J Radio Button Menu Item. temp varible
			JRadioButtonMenuItem iscjrbmi = new JRadioButtonMenuItem(InSetCalculator.getList().get(i).getName());
			iscjrbmi.setSelected(i == 0);
			iscjrbmi.addActionListener(new ISCActionListener(InSetCalculator.getList().get(i)));
			iscGroup.add(iscjrbmi);
			setInSetCalculator.add(iscjrbmi);
		}
		
		/*
		JRadioButtonMenuItem isc_averageDistance = new JRadioButtonMenuItem("Average Distance");
		JRadioButtonMenuItem isc_firstLastDistance = new JRadioButtonMenuItem("First-Last Distance");
		JRadioButtonMenuItem isc_longestDistance = new JRadioButtonMenuItem("Longest Distance");
		
		isc_averageDistance.addActionListener(new ISCActionListener("ISC_averageDistance"));
		isc_firstLastDistance.addActionListener(new ISCActionListener("ISC_firstLastDistance"));
		isc_longestDistance.addActionListener(new ISCActionListener("ISC_longestDistance"));
		
		
		
		iscGroup.add(isc_averageDistance);
		iscGroup.add(isc_firstLastDistance);
		iscGroup.add(isc_longestDistance);
		
		setInSetCalculator.add(isc_averageDistance);
		setInSetCalculator.add(isc_firstLastDistance);
		setInSetCalculator.add(isc_longestDistance);
		*/
		editMenu.add(setInSetCalculator);
		
		return editMenu;
	
	}
	


}
