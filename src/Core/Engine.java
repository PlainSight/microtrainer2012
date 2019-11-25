package Core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import Interface.Appearance;
import Interface.MicroCanvas;
import Interface.MicroFrame;


public class Engine 
{
	public static MicroFrame mF;
	public static MicroCanvas mC;
	
	//settings 
	private File settingsFile;
	private File[] unitFiles;
	
	private LinkedList<Command> inputs = new LinkedList<Command>();;
	public static int tick;
	Selection[] selections;
	Selection currentSelection;
	
	private Quadtree[] trees; //tree[faction - 1] note: faction is of target faction
	
	//game objects	- modified during a round
	private boolean running = false;
	Unit[] units;
	int unitNumber = 0;
	
	int currentNumberOfAllies = 0;
	int currentNumberOfEnemies = 0;
	private String alliedColour = null;
	private String enemyColour = null;
	private int speed = 0;
	
	public static int numberOfQuads = 0;
	public static Geohash hash;
	
	private int nextId = 0;
	
	private boolean ready = false;
	
	Replay currentReplay;
	GameSetting settings;
	GameSetting replaySettings;
	
	boolean replayMode = false;
	
	private int[] targetFaction = {0, 2, 1};	//maps faction to be target to each other
	int playerFaction = 1;
	
	public static void main(String[] args)
	{
		new Engine();
	}
	
	public Engine()
	{
		mF = new MicroFrame(this);
		mC = mF.getCanvas();
		//inputs = mC.getInputBuffer();
		settingsFile = new File("settings.txt");
		unitFiles = new File[] {new File("Marine.txt"), new File("Baneling.txt"), new File("Zergling.txt")};

		settings = new GameSetting(settingsFile, mF);
		
		mC.refresh();
		
		mainLoop();
	/*	try
		{
			mainLoop();
		} catch (Exception e)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			printErrorLog(sw.toString());
		}*/
	}
	
	
	
	public void mainLoop()
	{
		boolean nextRoundReplay = false;
		
		while(true)
		{
			boolean win = playRound();
			if(nextRoundReplay)
			{
				replayMode = false;
				nextRoundReplay = false;
			}
			if(win)
			{
				settings.nextRound();	//this increments enemy counts
			} else {
				if(replayMode)
				{
					loadReplay();
					nextRoundReplay = true;
				}
			}
		}
	}
	

	
	public boolean playRound()	//returns true if won or false if lost
	{
		//debugging
		//numberOfQuads = 0;
		
		nextId = 0;
		ready = false;
		tick = 0;
		inputs.clear();

		//load settings
		currentNumberOfAllies = currentSettings().currentAlly;
		currentNumberOfEnemies = currentSettings().currentEnemy;
		
//		stress testing
		// currentNumberOfAllies = 400;
		// currentNumberOfEnemies = 1000;
		
		alliedColour = currentSettings().alliedColour;
		enemyColour = currentSettings().enemyColour;
		speed = currentSettings().speed;
		//finished loading settings
		
		
		//temporary loading of unit types
		UnitType marine = new UnitType(unitFiles[0]);
		marine.giveFaction(1);
		UnitType baneling = new UnitType(unitFiles[1]);
		baneling.giveFaction(2);
		UnitType badMarine = new UnitType(unitFiles[0]);
		badMarine.giveFaction(2);
		UnitType zergling = new UnitType(unitFiles[2]);
		zergling.giveFaction(2);

		if(!replayMode)
		{
			settings.saveSettings(settingsFile);
			currentReplay = new Replay();
		}
		
		//tell canvas which colours
		mC.setColours(new String[] {alliedColour, enemyColour});
		
		//set up selection groups
		selections = new Selection[10];
		//now set up units and their positions 
		units = new Unit[2048];
		unitNumber = 0;
		
		//spawn allies
		for(int i = 0; i < currentNumberOfAllies; i++)
		{
			double distance = Math.pow((25*i), 0.7);
			double angle = Math.pow((i*2), 0.7);
			
			addUnit(new Unit(marine, 700 + (int) (distance*Math.cos(angle)), 500 + (int) (distance*Math.sin(angle)), nextId++));
		}
		
		//spawn enemies
		for(int i = 0; i < currentNumberOfEnemies; i++)
		{
			double distance = Math.pow((25*i), 0.7);
			double angle = Math.pow((i*2), 0.7);
			
			//addUnit(new Unit(zergling, 100 , 100 )); This helps with some very unlikely bugs which occur when units are tightly packed
			addUnit(new Unit(zergling, 200 + (int) (distance*Math.cos(angle)), 200 + (int) (distance*Math.sin(angle)), nextId++));
		}
		
		ready = true;	//ready for render
		
		running = true;
		
		trees = new Quadtree[2];
		
		trees[0] = new Quadtree(8192, 8192, 1024, 1024, null);
		trees[1] = new Quadtree(8192, 8192, 1024, 1024, null);
		
		hash = new Geohash(-3072, -3072, 8192, 8192, 64);
		
		//this adds the correct units to the correct Quadtree
		for(int i = 0; i < unitNumber; i++)
		{
			trees[units[i].getFaction()-1].add(units[i]);
			units[i].giveQuadtree(trees[units[i].getFaction()-1]);
			units[i].giveTargetQuadtree(trees[targetFaction[units[i].getFaction()]-1]);
		}
		
		System.out.println("Starting round with " + currentNumberOfAllies + " allies and " + currentNumberOfEnemies + " enemies");
		
		//start the round
		return gameloop();
	}
	
	public void addUnit(Unit u)
	{
		units[unitNumber] = u;
		unitNumber++;
	}
	
	public void removeUnit(int index)
	{
		units[index] = units[unitNumber-1];
		units[unitNumber-1] = null;
		unitNumber--;
	}
	
	public void printErrorLog(String error)
	{
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(new File(System.currentTimeMillis() + " Error.txt")));
			out.write(error);
			out.close();
		} catch (IOException e) {
			System.out.println("File writing failed for replay");
		}
	}

	public boolean gameloop()	//returns win or not
	{
		int endtick = Integer.MAX_VALUE;
		
		long longestTime = 0;
		int longestTimeTick = 0;
		
		while(running)
		{
			long startTime = System.nanoTime();
			if((currentNumberOfAllies == 0 || currentNumberOfEnemies == 0) && endtick == Integer.MAX_VALUE)
			{
				endtick = tick + 25;
			}
			
			if(tick == endtick)
			{
				running = false;
			}
			
			
			tick++;
			System.out.println("TICK: " + tick + " with " + currentNumberOfAllies + " allies and " + currentNumberOfEnemies + " enemies.");
			
			boolean paused = false;
			
			
			//read and apply commands for when not in a replay
			while(!inputs.isEmpty())
			{
				if(inputs.peek().getTime() <= tick)
				{
					
					int commandNumber = inputs.pop().execute(this);
					
					//System.out.println("EXECUTING INPUT: " + commandNumber);
					
					if(commandNumber == 8)
					{
						//we want to play replay
						replayMode = true;
						return false;
					}
					if(commandNumber == 9)
					{
						//we want to restart the round
						return false;
					}
					if(commandNumber == 11)
					{
						//we want to reset to base number of units
						settings.reset();
						return false;
					}
					if(commandNumber == 5)
					{
						//we want to pause the game
						paused = true;
					}
				} else {
					if(!paused)
					{
						break;	//if there are no more commands on the current tick and the game isn't paused break the loop
					}
				}
			}
			
			//baneling commands
			//every 100 ticks enemies which are not moving attack move at a target ally 
			if(tick%50 == 0 && currentNumberOfAllies != 0)
			{
				for(int i = 0; i < unitNumber; i++)
				{
					if(units[i].getFaction() == 2)
					{
						if(!units[i].hasTarget() )
						{
							Unit tar = units[i].quad.findRandomTarget(units[i]);
							units[i].attackMoveCommand(tar.getX(), tar.getY());
						}
					}
				}
			}
			
			for(int i = 0; i < unitNumber; i++)
			{				
				units[i].takeTurn();
				//find targets, take shots, decide things
				Unit temp = units[i].getShot();
				if(temp != null)
				{
					addShot(units[i], temp);
				}
			}
			
			//move and update units in geohash and quadtree
			moveUnits();
			
			System.out.println("Number of quads: " + numberOfQuads);
			long temptime = System.nanoTime() - startTime;
			System.out.println("Time for part 1 in nanoseconds: " + temptime );
			if(temptime > longestTime)
			{
				longestTime = temptime;
				longestTimeTick = tick;
			}
			long time2 = System.nanoTime();
			
			
			//prepare stuff for rendering
			Interface.Appearance appearance = new Appearance(unitNumber, shots.size(), tick);
			for(int i = 0; i < unitNumber; i++)
			{
				appearance.addUnit(units[i]);
			}
			for(Shot s: shots)
			{
				appearance.addShot(s.from, s.to);
			}
			
			//now appearance is ready.
			mC.addAppearance(appearance);
			
			//clean up
			shots = new ArrayList<Shot>();
			
			//remove dead units
			for(int i = 0; i < unitNumber; i++)
			{
				if(units[i].isDead())
				{
					units[i].die();
					if(units[i].getFaction() == 1)
					{
						currentNumberOfAllies--;
					} else {
						currentNumberOfEnemies--;
					}
					removeUnit(i--);
				}
			}
			
			
			System.out.println("Time for part 2 in nanoseconds: " + (System.nanoTime() - time2) );
			
			int sleepTime = 16 - (int) ((System.nanoTime() - startTime)/1000000);
			if(sleepTime < 0) sleepTime = 0;
			try{Thread.sleep(sleepTime);}catch(Exception error)
			{
				error.printStackTrace();
			}
		}	//end of game loop
		
		
		System.out.println("LONGEST TIME: " + longestTime + " at TICK: " + longestTimeTick);
		
		ready = false; //no more rendering
		
		//after game ends come to here. Game ends when replay mode is activated or round finishes
		if(replayMode)	//if replayMode is true then replay has finished
		{
			return false;
		} else {
			if(currentNumberOfAllies == 0)
			{
				//return win as false
				return false;
			} else {
				//save replay and return win as true
				if(settings.saveReplay)
				{
					saveReplay();
				}
				return true;
			}
			
		}
	}
	
	public LinkedList<Command> getCommmandQueue()
	{
		return inputs;
	}
	
	private ArrayList<Shot> shots = new ArrayList<Shot>();
	
	private class Shot
	{	
		Unit from;
		Unit to;
		public Shot(Unit a, Unit b)
		{
			from = a;
			to = b;
		}
	}
	
	public void addShot(Unit shooter, Unit target)
	{
		shots.add(new Shot(shooter, target));
	}
	
	public boolean isReady()
	{
		return ready;
	}
	
	public GameSetting currentSettings()
	{
		if(replayMode)
		{
			return replaySettings;
		} else {
			return settings;
		}
	}
	
	public File getSettingsFile()
	{
		return settingsFile;
	}
	
	public boolean isReplay()
	{
		return replayMode;
	}
	
	public GameSetting getSettings()
	{
		return settings;
	}
	
	public Unit[] getUnits()
	{
		return units;
	}
	
	public void saveReplay()
	{	
		currentReplay.saveReplay(settings);
	}
	
	public void loadReplay()
	{
		JFileChooser chooser = new JFileChooser("replays");
		
		chooser.showOpenDialog(mF);
		
		if(chooser.getSelectedFile() == null || !chooser.getSelectedFile().toString().endsWith(".rep"))
		{
			return;
		}
		File f = chooser.getSelectedFile();
		
		currentReplay = new Replay(f);
	}
	
	public boolean between(int x1, int y1, int x2, int y2, Unit u)
	{
		int r = u.getR();
		int minx = Math.min(x1, x2) - r;
		int maxx = Math.max(x1, x2) + r;
		int miny = Math.min(y1, y2) - r;
		int maxy = Math.max(y1, y2) + r;
		int x = u.getX();
		int y = u.getY();
		
		return (x < maxx && y < maxy && x > minx && y > miny);
	}
	
	public void changeSelection()
	{
		if(currentSelection == null) 
		{
			currentSelection = new Selection();
			return;
		}
		for(Unit u : currentSelection.getSelection())
		{
			u.setUnselected();
		}
		currentSelection = new Selection();
	}
	
	
	private void moveUnits()
	{
		int compares = 0;
		for(int uu = 0; uu < unitNumber; uu++)
		{
			if(!units[uu].canMove()) continue;
			
//			double maxMove = u.getSpeed();
			int x = units[uu].getX();
			int y = units[uu].getY();
			int r = units[uu].getR();
			
			//calculate repulsive component
			double repulsiveY = 0;
			double repulsiveX = 0;
			
			int[] xy = hash.coordinate(units[uu]);

			for(int i = 0; i < 9; i++)
			{
				for(Unit uuu = hash.get(xy[0] - 1 + (i/3), xy[1] - 1 + (i%3)).getLink(); uuu != null; uuu = uuu.child)
				{
					if(units[uu] == uuu) continue;	//doesn't check against self
					
					compares++;
					
					//for complete collisions
					if(x == uuu.getX() && y == uuu.getY())
					{
						repulsiveY += (uu % 5) - 2;
						repulsiveX += (uu % 7) - 3;
					}
					
					double distanceSquared = square(x - uuu.getX()) + square(y - uuu.getY());
					int c = 8; //constant, the larger it is the more separateion
					
					//this makes collision with units which can't move better
					if(!uuu.canMove())
					{
						c = 12;
					}
					double undirectedRepulse = (c * square(r)) / ( Math.sqrt(distanceSquared) * ( distanceSquared + (c * r) ) );
					
					repulsiveX += (x - uuu.getX()) * undirectedRepulse;
					repulsiveY += (y - uuu.getY()) * undirectedRepulse;
				}
			}
			
			double xd = 0;
			double yd = 0;
			
			if(units[uu].hasDesire())
			{
				//calculate relative desire
				xd = units[uu].getDesiredX() - x;
				yd = units[uu].getDesiredY() - y;
				
				double ts = Math.hypot(xd, yd);
				
				double scaler = units[uu].getSpeed() / ts;
				
				xd *= scaler;
				yd *= scaler;
			}
			
			double xr = repulsiveX;// / (double) repulsiveNumber;
			double yr = repulsiveY;// / (double) repulsiveNumber;
			
			double xf = 1.0*xr + 1.0*xd;
			double yf = 1.0*yr + 1.0*yd;
			
			//should scale movement up to speed limit
			
			units[uu].setDoMove((int) xf, (int) yf);
		}
		System.out.println("Compares this frame: " + compares);
		
		for(int i = 0; i < unitNumber; i++)
		{
			if(units[i].canMove())
			{
				
				units[i].doMove();
			}
		}
	}
	
	public double square(int x)
	{
		return (double) x*x;
	}
}
