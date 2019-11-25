package Core;
import java.util.Scanner;

public class GenericCommand implements Command
{
	int time;
	int commandNumber;
	int[] otherParameters;
	
	public GenericCommand(int t, int cN, int[] oP)
	{
		time = t;
		commandNumber = cN;
		otherParameters = oP;
	}
	
	public GenericCommand(String line)
	{
		Scanner sc = new Scanner(line);
		time = Integer.parseInt(sc.next());
		commandNumber = Integer.parseInt(sc.next());
		otherParameters = new int[6];
		int i = 0;
		while(sc.hasNext())
		{
			otherParameters[i] = Integer.parseInt(sc.next());
			i++;
		}
	}

	public int execute(Engine game)
	{
		Unit[] uni = game.units;
		if(commandNumber == 2)	//mouse made selection x1, y1, x2, y2
		{
			game.changeSelection();
			for(int i = 0; i < game.unitNumber; i++)
			{
				if(game.between(otherParameters[0], otherParameters[1], otherParameters[2], otherParameters[3], uni[i]) && uni[i].getFaction() == game.playerFaction)
				{
					uni[i].setSelected();
					game.currentSelection.addUnit(uni[i]);
				}
			}
		}
		if(commandNumber == 3 && game.currentSelection != null) //command has been given
		{
			for(Unit u : game.currentSelection.getSelection())	//move
			{
				u.moveCommand(otherParameters[0], otherParameters[1]);
			}
		}
		if(commandNumber == 4 && game.currentSelection != null)	//attack
		{
			//look for target
			Unit target = null;
	  loop: for(int i = 0; i < game.unitNumber; i++)
			{
				if(game.between(otherParameters[0], otherParameters[1], otherParameters[0], otherParameters[1], uni[i]))
				{
					target = uni[i];
					break loop;
				}
			}
			if(target != null)	//attack
			{
				for(Unit u : game.currentSelection.getSelection())
				{
					u.attackCommand(target);
				}
			} else {	//attackmove
				for(Unit u : game.currentSelection.getSelection())
				{
					u.attackMoveCommand(otherParameters[0], otherParameters[1]);
				}
			}
		}
		if(commandNumber == 6)	//recall selection
		{
			if(game.selections[otherParameters[0]] != null)
			{
				if(game.selections[otherParameters[0]].getSelection().size() != 0)
				{
					game.changeSelection();
					for(Unit u : game.selections[otherParameters[0]].getSelection())
					{
						game.currentSelection.addUnit(u);
					}
					for(Unit u : game.currentSelection.getSelection())
					{
						u.setSelected();
					}
				}
			}
		}
		if(commandNumber == 7)	//assign selection
		{
			game.selections[otherParameters[0]] = new Selection();
			for(Unit u : game.currentSelection.getSelection())
			{
				game.selections[otherParameters[0]].addUnit(u);
			}
		}
		//special commands
		/*if(commandNumber == 8)	//replay
		{
			//tell engine to play replay
		}
		if(commandNumber == 9)	//restart
		{
			//restart round
		}
		if(commandNumber == 11)	//reset to base
		{
			
		}
		//needs to be fixed
		if(commandNumber == 5) //pause
		{
			game.paused = true;
		}
		*/
		return commandNumber;
	}

	public int getTime() 
	{
		return time;
	}
	
	public String toFile() 
	{
		String other = "";
		for(int i = 0; i < otherParameters.length; i++)
		{
			other += otherParameters[i] + " ";
		}
		return time + " " + commandNumber + " " + other;
	}
	
	
	
	
}