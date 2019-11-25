package Core;
import java.util.*;


public class Selection 
{
	//lazy implementation
	
	ArrayList<Unit> units;
	
	public Selection()
	{
		units = new ArrayList<Unit>();
	}
	
	public void addUnit(Unit u)
	{
		units.add(u);
	}
	
	public ArrayList<Unit> getSelection()
	{
		return units;
	}
}
