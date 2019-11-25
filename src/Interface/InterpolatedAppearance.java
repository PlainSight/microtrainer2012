package Interface;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Core.Unit;


public class InterpolatedAppearance
{
	
	private InterpolatedAppearanceUnit[] units = new InterpolatedAppearanceUnit[1048576];	//max number of units ever
	
	private InterpolatedAppearanceUnit[] rendering = new InterpolatedAppearanceUnit[4096];	//max number of units at a time
	private int numberRendering = 0;
	
	public InterpolatedAppearance(Appearance a)
	{
	
		for(int i = 0; i < a.units.length; i++)
		{
			units[a.units[i].id] = new InterpolatedAppearanceUnit(a.units[i]);
		}
	}
	
	public void update(Appearance a)
	{
		for(int i = 0; i < a.units.length; i++)
		{
			if(units[a.units[i].id] != null)
			{
				units[a.units[i].id].next(a.units[i]);
			} else {
				units[a.units[i].id] = new InterpolatedAppearanceUnit(a.units[i]);
				rendering[numberRendering++] = units[a.units[i].id];
			}
		}
	}
	
	public void render()
	{
		for (int i = 0; i < numberRendering; i++)
		{
			
			if(rendering[i].age() > 5)
			{
				//unit is no longer alive
				units[rendering[i].id] = null;
				rendering[i] = rendering[numberRendering];
				numberRendering--;
				i--;
			} else {
				//render unit
				
				
				
				
			}
			
		}
	}
	
	
	
}
