package Interface;

import Core.Unit;


public class Appearance 
{
	int tick;
	AppearanceUnit[] units;
	AppearanceShot[] shots;
	private int unitCount = 0;
	private int shotCount = 0;
	AppearanceSelect select;
	
	AppearanceUnit[] unitHash = new AppearanceUnit[4096];
	
	
	public Appearance(int us, int ss, int t)
	{
		tick = t;
		units = new AppearanceUnit[us];
		shots = new AppearanceShot[ss];
	}
	
	public void addUnit(Unit u)
	{
		AppearanceUnit temp = new AppearanceUnit(u);
		units[unitCount++] = temp;
		unitHash[temp.id % 4096] = temp;
	}
	
	public void addShot(Unit u1, Unit u2)
	{
		AppearanceShot temp = new AppearanceShot(u1, u2);
		shots[shotCount++] = temp;
	}
	
	public void addSelect(int[] in)
	{
		if(in[0] == 1)
        {
            int x = Math.min(in[1], in[3]);
            int y = Math.min(in[2], in[4]);
            int width = Math.abs(in[1] - in[3]);
            int height = Math.abs(in[2] - in[4]);
            
            select = new AppearanceSelect(x, y, width, height);
        }
	}
	
	
	
	class AppearanceSelect
	{
		int x;
		int y;
		int width;
		int height;
		
		public AppearanceSelect(int q, int w, int e, int r)
		{
			x = q;
			y = w;
			width = e;
			height = r;
		}
	}
	
	
	class AppearanceUnit
	{
		int id;
		int x;
		int y;
		int r;
		int hp;
		
		boolean selected;
		
		Core.UnitState state; //what doing
		int faction;
		
		/*
		 * Holds data about the appearance of a unit including references to its graphics file,
		 * location, current state and frame within that state, also holds id which links back
		 * to engine object
		 */
		public AppearanceUnit(Unit u)
		{
			x = u.getX();
			y = u.getY();
			r = u.getR();
			hp = u.getHp();
			selected = u.isSelected();
			id = u.getId();
			faction = u.getFaction();
			state = u.getState();
		}
		
	}
	
	class AppearanceShot
	{
		int xo;
		int yo;
		
		int xt;
		int yt;
		
		public AppearanceShot(Unit from, Unit to)
		{
			xo = from.getX();
			yo = from.getY();
			
			xt = to.getX();
			yt = to.getY();
		}
	}
	
}
