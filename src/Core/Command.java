package Core;

public interface Command
{	
	public int execute(Engine game);
	public int getTime();
	public String toFile();
}



/*class SelectionCommand implements Command
{	
	private int tick;
	
	public SelectionCommand(int x1, int y1, int x2, int y2, int time)
	{
		tick = time;
	}
	
	public SelectionCommand(String hotkey)
	{
		
	}

	public void execute()
	{
		// TODO Auto-generated method stub
		
	}

	public int getTime() {
		return tick;
	}

	@Override
	public String toFile() {
		// TODO Auto-generated method stub
		return null;
	}
}

class TargetCommand implements Command
{
	private int tick;
	
	public TargetCommand(String type, int x2, int y2, int time)
	{
		tick = time;
	}

	public void execute()
	{
		// TODO Auto-generated method stub
		
	}

	public int getTime() {
		return tick;
	}

	@Override
	public String toFile() {
		// TODO Auto-generated method stub
		return null;
	}
}*/

