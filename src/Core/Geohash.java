package Core;


public class Geohash 
{
	private GeohashSpace[][] spaces;
	private int startx;
	private int starty;
	private int width;
	private int height;
	private int size;	//x and y dimension of each hash
	
	public Geohash(int sx, int sy, int w, int h, int s)
	{
		startx = sx;
		starty = sy;
		width = w;
		height = h;
		size = s;
		
		int xnum = width / size;
		int ynum = height / size;
		
		spaces = new GeohashSpace[xnum][ynum];
		
		for(int x = 0; x < xnum; x++)
		{
			for(int y = 0; y < ynum; y++)
			{
				spaces[x][y] = new GeohashSpace();
			}
		}
		
	}
	
	public void hash(Unit u)
	{
		int[] co = coordinate(u);
		spaces[co[0]][co[1]].addUnit(u);
	}
	
	public int[] coordinate(Unit u)
	{
		int x = u.getX() - startx;
		int y = u.getY() - starty;
		
		return new int[] {x / size, y / size};
	}
	
	public GeohashSpace get(int x, int y)
	{
		return spaces[x][y];
	}
}
