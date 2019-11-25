package Interface;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.*;

import Core.Engine;

public class MicroFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private MicroCanvas canvas;
	private JMenuBar bar;
	private JMenu menu;
	private HashMap<String, JMenuItem> hash;
	private Engine engine;
	private InputBuffer input;
	
	public MicroCanvas getCanvas()
	{
		return canvas;
	}
	
	public MicroFrame(Engine e)
	{
		super("Micro Trainer");
		input = new InputBuffer();	//canvas and frame share input buffer
		canvas = new MicroCanvas(1000, 800, e, input);
		engine = e;
		
		hash = new HashMap<String, JMenuItem>();
		
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		
		//JButton button = new JButton("Start/Pause");
		//button.addActionListener(this);
		//JMenuItem button2 = new JMenuItem("View Replay");
		//button2.addActionListener(this);
		JButton button3 = new JButton("Restart Round");
		button3.addActionListener(this);
		JMenuItem button4 = new JMenuItem("Reset to Base");
		button4.addActionListener(this);
		
		bar = new JMenuBar();
		menu = new JMenu("Options");
		addMenuRadio("Allied Colour", new String[] {"Red", "Blue", "Green", "Purple", "Black"}, menu);
		addMenuRadio("Enemy Colour", new String[] {"Red", "Blue", "Green", "Purple", "Black"}, menu);
		//addMenuRadio("Speeds", new String[] {"3", "5", "7"}, menu);
		addMenuRadio("Enemy Increment", new String[] {"1", "2", "3"}, menu);
		addMenuRadio("Base Enemy Number", new String[] {"5", "10", "15", "20"}, menu);
		addMenuRadio("Base Ally Number", new String[] {"5", "10", "15", "20"}, menu);
		//addMenuCheckbox("Save Replays", new String[] {}, menu);
		//menu.add(button2);
		menu.add(button4);
		
		bar.add(menu);
		//bar.add(button);
		bar.add(button3);
		getContentPane().add(bar, BorderLayout.NORTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		setResizable(true);
		
		canvas.fix();
	}
	
	public void setMenuItem(String setting)
	{
		if(hash.containsKey(setting))
		{
			hash.get(setting).setSelected(true);
		}
	}
	
	private void addMenuCheckbox(String name, String[] s, JMenu m)
	{
		if(s.length == 0)
		{
			JMenuItem temp = new JCheckBoxMenuItem(name);
			temp.addActionListener(this);
			temp.setActionCommand(name);
			hash.put(temp.getActionCommand(), temp);
			m.add(temp);
			return;
		}
		
		JMenu temp1 = new JMenu(name);
		m.add(temp1);

		for(int i = 0; i < s.length; i++)
		{
			JMenuItem temp = new JCheckBoxMenuItem(s[i]);
			temp.addActionListener(this);
			temp.setActionCommand(name + " " + temp.getActionCommand());
			hash.put(temp.getActionCommand(), temp);
			temp1.add(temp);
		}
	}
	
	private void addMenuRadio(String name, String[] s, JMenu m)
	{
		JMenu temp1 = new JMenu(name);
		m.add(temp1);
		
		ButtonGroup b = new ButtonGroup();
		for(int i = 0; i < s.length; i++)
		{
			JMenuItem temp = new JRadioButtonMenuItem(s[i]);
			b.add(temp);
			temp.addActionListener(this);
			temp.setActionCommand(name + " " + temp.getActionCommand());
			hash.put(temp.getActionCommand(), temp);
			temp1.add(temp);
			temp.setSelected(true);
		}
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		//debuging
		
		if(e.getActionCommand().equals("Start/Pause"))
		{
			input.pause();
			canvas.checkCommands();
			return;
		}
		if(e.getActionCommand().equals("View Replay"))
		{
			input.replay();
			canvas.checkCommands();
			return;
		}
		if(e.getActionCommand().equals("Restart Round"))
		{
			input.restart();
			canvas.checkCommands();
			return;
		}
		if(e.getActionCommand().equals("Reset to Base"))
		{
			input.reset();
			canvas.checkCommands();
			return;
		}
		
		System.out.println("CHANGEING A SETTING");
		
		if(e.getActionCommand() != null)
		{
			System.out.println("ACTUALLY TELLING THE SETTINGS");
			engine.getSettings().setASetting(e.getActionCommand());
		}
	}

}
