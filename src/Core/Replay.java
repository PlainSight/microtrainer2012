package Core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class Replay 
{
	private ArrayList<Command> commands;
	private int index = 0;
	private GameSetting settings;
	private boolean valid = true;	//true if replay file is actually a replay
	
	//this simply constructs the setting file and command array
	public Replay(File f)
	{
		int lineCount = 0;
		
		//File f = new File("replays" + File.separator + "replay.rep");
		try
		{
			Scanner sc = new Scanner(f);
			
			while(sc.hasNextLine())
			{
				sc.nextLine();
				lineCount++;
			}
			sc.close();
		} catch (IOException e) {System.out.println("something failed 1");}
		
		if(lineCount == 0)
		{
			valid = false;
		}
		
		try
		{
			Scanner sc = new Scanner(f);
			//load the settings
			//currentNumberOfMarines + " " + currentNumberOfBanelings + " " + speed + " " + alliedColour + " " + enemyColour + "\n";
			
			String settingsString = sc.nextLine();
			settings = new GameSetting(settingsString);
			
			for(int line = 0; line < lineCount-1; line++)	//lineCount -1 because of settings line
			{
				System.out.println(line);
				String parts = sc.nextLine();
				
				Command c = new GenericCommand(parts);
				commands.add(c);
			}
			sc.close();
		} catch (IOException e1) 
		{
			System.out.println("something failed 2");
			System.exit(0);
		}
	}
	
	//this is a replay used by the game engine during play to save commands
	public Replay()
	{
		commands = new ArrayList<Command>();
		
		
		
	}
	
	public GameSetting loadReplay()
	{
		index = 0;
		return settings;
	}
	
	
	public void execute(int tick, Engine game)
	{
		while(commands.get(index).getTime() == tick)
		{
			commands.get(index++).execute(game);
		}
	}
	
	
	
	public void finishReplay()
	{
		index = 0;
	}
	
	//simply creates files out of command strings
	public void saveReplay(GameSetting settings)
	{
		BufferedWriter out;
		String replayName = settings.getRoundString();

		String data = "";
		
		data += settings.getReplayString();
		
		for(Command c : commands)
		{
			data += c.toFile() + "\n";
		}
		
		try {
			if(!new File("replays").exists())
			{
				new File("replays").mkdir();
			}
			out = new BufferedWriter(new FileWriter(new File("replays" + File.separator + replayName +".rep")));
			out.write(data);
			out.close();
		} catch (IOException e) {
			System.out.println("File writing failed for replay");
		}
	}
	
}
