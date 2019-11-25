package Core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import Interface.MicroFrame;


public class GameSetting
{
	/*
	 * Game settings are read at the beginning of every round
	 */
	
	private HashMap<String, UnitType> unitTypes;
	
	/*private int speedSetting;
	private int zoomSetting;*/
	private MicroFrame mF;
	
	int roundNumber = 0;
	
	int baseAlly = 5;
	int baseEnemy = 5;
	
	int enemyIncrement = 1;
	
	int currentAlly = 5;
	int currentEnemy = 5;
	
	String alliedColour = "Blue";
	String enemyColour = "Red";
	int speed = 5;
	boolean saveReplay = false;
	
	
	
	public void setupGame(Engine e)
	{
		//this makes the correct units
		//and then spawns the correct number for each faction
	}
	
	public void readUnitTypes()
	{
		
	}
	
	public void nextRound()
	{
		currentEnemy += enemyIncrement;
		roundNumber++;
	}
	
	public void reset()
	{
		currentAlly = baseAlly;
		currentEnemy = baseEnemy;
	}
	
	//this returns the replay name
	public String getRoundString()
	{
		return currentAlly + "Allies" + currentEnemy + "Enemies Speed " + speed;
	}
	
	public String getReplayString()
	{
		//needs to include current number of units of each type as well as game speed
		return "these settings should allow the game to be replayed";
	}
	
	//this is used for general play, retained while game is open and modified
	public GameSetting(File settingsFile, MicroFrame mf)
	{
		mF = mf;
		
		loadSettings(settingsFile);
	}
	
	//this is used for replays
	public GameSetting(String line)
	{
		Scanner sc = new Scanner(line);
		//load the settings
		//currentNumberOfMarines + " " + currentNumberOfBanelings + " " + speed + " " + alliedColour + " " + enemyColour + "\n";
		
		
		
		
	}
	
	public void loadSettings(File f)
	{
		try
		{
			Scanner sc = new Scanner(f);
			
			while(sc.hasNext())
			{
				setASetting(sc.nextLine());
			}
			sc.close();
		} catch (IOException e) {
			System.out.println("setting loading failed");
			setASetting("baseEnemy 5");
			setASetting("enemyIncrement 1");
			setASetting("currentAlly 5");
			setASetting("currentEnemy 5");
			setASetting("alliedColour Blue");
			setASetting("enemyColour Red");
			setASetting("speed 5");
			setASetting("saveReplay false");
		}
		
		currentAlly = baseAlly;
		currentEnemy = baseEnemy;
	}
	
	public void saveSettings(File f)
	{
		String settingString = "";

		settingString += "baseAlly " + baseAlly + "\n";
		settingString += "baseEnemy " + baseEnemy + "\n";
		settingString += "enemyIncrement " + enemyIncrement + "\n";
		settingString += "alliedColour " + alliedColour + "\n";
		settingString += "enemyColour " + enemyColour + "\n";
		settingString += "speed " + speed + "\n";
		settingString += "saveReplay " + saveReplay + "\n";

		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(f));
			out.write(settingString);
			out.close();
		} catch (IOException e) {
			System.out.println("File writing failed for settings");
		}
	}
	
	public void setASetting(String line)
	{
		String[] components = line.split("\\s+");
		String var = "";
		for(int i = 0; i < components.length - 1; i++)
		{
			var += components[i] + ((i == components.length-2) ? "" : " ");
		}
		setASetting(var, components[components.length-1]);
	}
	
	public void setASetting(String var, String value)
	{
		//System.out.println("Setting the setting " + var + " to " + value);
		
		if(var.equals("baseAlly") || var.equals("Base Ally Number"))
		{
			baseAlly = Integer.parseInt(value);
			mF.setMenuItem("Base Ally Number " + value);
		}
		if(var.equals("baseEnemy") || var.equals("Base Enemy Number"))
		{
			baseEnemy = Integer.parseInt(value);
			mF.setMenuItem("Base Enemy Number " + value);
		}
		if(var.equals("enemyIncrement") || var.equals("Enemy Increment"))
		{
			enemyIncrement = Integer.parseInt(value);
			mF.setMenuItem("Enemy Increment " + value);
		}
		if(var.equals("alliedColour") || var.equals("Allied Colour"))
		{
			alliedColour = value;
			mF.setMenuItem("Allied Colour " + value);
		}
		if(var.equals("enemyColour") || var.equals("Enemy Colour"))
		{
			enemyColour = value;
			mF.setMenuItem("Enemy Colour " + value);
		}
		if(var.equals("speed") || var.equals("Speeds"))
		{
			speed = Integer.parseInt(value);
			mF.setMenuItem("Speeds " + value);
		}
		if(var.equals("saveReplay") || var.equals("Save"))
		{
			if(saveReplay)	//if savereplay was already true set to false
			{
				saveReplay = false;
				//no need to set tickbox to false as it is false by default
			} else {
				if(!Boolean.parseBoolean(value))
				{
					saveReplay = false;
				} else {
				saveReplay = true;
				mF.setMenuItem("Save Replays");
				}
			}
		}
	}
}
