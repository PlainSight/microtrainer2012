package Core;
import java.util.Arrays;


/* This class is made to ease the task of finding targets
 * each unit held within should update itself regularly.
 */

//will use quadtree for each faction

public class Quadtree 
{
	private int width;
	private int height;
	
	private int midx;
	private int midy;
	
	int minx;
	int miny;
	
	int maxx;
	int maxy;
	
	Unit[] units;
	int size;
	
	private Quadtree parent;
	private Quadtree[] children = new Quadtree[4];
	
	public Quadtree(int w, int h, int mx, int my, Quadtree p)
	{
		width = w;
		height = h;
		midx = mx;
		midy = my;
		parent = p;
		
		if(w > 2)	//only necessary with hyperdensely packed units as a precaution
		{
			units = new Unit[4];
		} else {
			units = new Unit[100];
		}
		
		minx = midx - width/2;
		maxx = midx + width/2;
		
		miny = midy - height/2;
		maxy = midy + height/2;
		
		Engine.numberOfQuads++;
	}
	
	private void split()
	{
		
		//we must split this node
		//01
		//23
		children[0] = new Quadtree(width/2,height/2, midx - width/4, midy - height/4, this);
		children[1] = new Quadtree(width/2,height/2, midx + width/4, midy - height/4, this);
		children[2] = new Quadtree(width/2,height/2, midx - width/4, midy + height/4, this);
		children[3] = new Quadtree(width/2,height/2, midx + width/4, midy + height/4, this);
		
		//put the nodes in children nodes
		for(int i = 0; i < 4; i++)
		{
			if(!putInChild(units[i]))
			{
				units[i].specificquad = null;
				splitRemove();
				//this unit becomes temporarily dereferenced by the quadtree
			}
		}

		//clean up node
		units = new Unit[4];
	}
	
	private void splitRemove()	//very rare decrements size for this and all parent nodes
	{
		for(Quadtree t = this; t != null; t = t.parent)
		{
			t.size--;
		}
	}
	
	public void printTree(int indent)
	{
		for(int i = 0; i < indent; i++)
			System.out.print("\t");
		System.out.println(this + " " + size + ":         " + units[0] + ", " + units[1] + ", " + units[2] + ", " + units[3]);
		if(children[0] != null)
		{
			for(int i = 0; i < 4; i++)
			{
				children[i].printTree(indent+1);
			}
		}
	}
	
	
	
	
	private boolean putInChild(Unit u)
	{
		for(int c = 0; c < 4; c++)
		{
			if(children[c].hasUnitInside(u))
			{
				children[c].add(u);
				return true;
			}
		}
		
		//means the unit is not longer within the confines of the parent bounds
		return false;
	}
	
	public void add(Unit u)
	{
		//if there are children then add to child
		
		if(children[0] != null)
		{
			putInChild(u);
		} else {
			if(size < units.length)
			{
				units[size] = u;
				u.giveSpecificQuadtree(this);
			} else {
				split();
				putInChild(u);
			}
		}
		size++;
	}
	
	public void remove(Unit u)
	{
		int index = 0;
		for(int i = 0; i < size; i++)
		{
			if(units[i] == u)
			{
				index = i;
				break;
			}
		}

		units[index] = units[size-1];
		units[size-1] = null;

		for(Quadtree t = this; t != null; t = t.parent)
		{
			t.size--;
			if(t.size < 4 && t.children[0] != null)
			{
				//combine child nodes
				t.units = concatAll(t.children[0].getUnits(), t.children[1].getUnits(), t.children[2].getUnits(), t.children[3].getUnits());
				t.children[0] = null;
				t.children[1] = null;
				t.children[2] = null;
				t.children[3] = null;
				Engine.numberOfQuads -= 4;
				//give the moved units the correct specific quad
				//need to check if unit has moved then it might be being put in wrong quad
				for(int i = 0; i < t.size; i++)
				{
					t.units[i].giveSpecificQuadtree(t);
				}
			}

		}

	}
	
	
	public Unit[] concatAll(Unit[] first, Unit[]... rest)
	{
		Unit[] result = Arrays.copyOf(first, 4);
		int offset = first.length;
		for (Unit[] array : rest) {
			if(array.length == 0) continue;
			int i = 0;
			while(i < array.length)
			{
				if(array[i] == null)
				{
					break;
				}
				result[offset++] = array[i++];
			}
		}
		return result;
	}
		
	public Unit[] getUnits()
	{
		return Arrays.copyOfRange(units, 0, size);
	}
	
	//checks whether a given unit would be inside the bounds of this quad
	public boolean hasUnitInside(Unit u)
	{	
		return (minx <= u.getX() && u.getX() < maxx && miny <= u.getY() && u.getY() < maxy);
	}
	
	//this is approximate and assumes width = height
	public boolean intersects(Unit u, int r)
	{
		double distancesqr = Math.pow(u.getX() - midx, 2) + Math.pow(u.getY() - midy, 2);
		return (distancesqr < Math.pow(0.71*width + r, 2));
	}
	
	public Unit findNearest(Unit u)
	{
		int range = u.getRange() + u.getR();
		
		if(!intersects(u, range))
		{
			return null;
		}
		
		Unit nearest = null;
		
		if(children[0] == null)
		{
			for(int i = 0; i < size; i++)
			{
				if(nearest == null)
				{
					nearest = units[i];
				} else {
					//debug
					if(units[i] == null)
					{
						System.out.println("error inc: " + i + " " + this);
					}
					
					if(u.distance(units[i]) < u.distance(nearest))
					{
						nearest = units[i];
					}
				}
			}
		}
		
		if(children[0] == null)
		{
			return nearest;
		} else {
			for(int c = 0; c < 4; c++)
			{
				Unit temp = children[c].findNearest(u);
				if(temp == null) continue;
				if(nearest == null)
				{
					nearest = temp;
				} else {
					if(u.distance(temp) < u.distance(nearest))
					{
						nearest = temp;
					}
				}
			}
			return nearest;
		}
	}
	
	public Unit findRandomTarget(Unit u)
	{
		
		if(children[0] == null)
		{
			for(int i = 0; i < size; i++)
			{
				return units[i];
			}
		} else {
			for(int c = 0; c < 4; c++)
			{
				Unit temp = children[c].findRandomTarget(u);
				if(temp == null) 
				{
					continue;
				} else {
					return temp;
				}
			}
		}
		return null;
	}
}
