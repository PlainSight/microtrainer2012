package Core;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class UnitType 
{
	public String type;
	public int maxHp;
	public int moveSpeed;
	public int maxCoolDown;
	public int range;
	public int targetAquisitionRange;
	public int radius;
	public int damage;
	public int faction;
	
	//this object shouldnt be attached to anything before there is a faction
	public boolean hasFaction()
	{
		return faction != 0;
	}
	
	public UnitType(File f)
	{
		try {
			Scanner sc = new Scanner(f);
			
			while(sc.hasNext())
			{
				setAVariable(sc.next(), sc.next());
			}
			
			
		} catch (FileNotFoundException e) {}
	}
	
	public void giveFaction(int f)
	{
		faction = f;
	}
	
	public void setAVariable(String var, String value)
	{
		if(var.equals("type"))
		{
			type = value;
		}
		if(var.equals("maxHp"))
		{
			maxHp = Integer.parseInt(value);
		}
		if(var.equals("moveSpeed"))
		{
			moveSpeed = Integer.parseInt(value);
		}
		if(var.equals("maxCoolDown"))
		{
			maxCoolDown = Integer.parseInt(value);
		}
		if(var.equals("range"))
		{
			range = Integer.parseInt(value);
		}
		if(var.equals("targetAquisitionRange"))
		{
			targetAquisitionRange = Integer.parseInt(value);
		}
		if(var.equals("radius"))
		{
			radius = Integer.parseInt(value);
		}
		if(var.equals("damage"))
		{
			damage = Integer.parseInt(value);
		}
	}
}
