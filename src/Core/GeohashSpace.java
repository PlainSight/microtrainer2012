package Core;


public class GeohashSpace
{
	private Unit unitlink;
	
	public void addUnit(Unit u)
	{
		u.giveSpecificGeo(this);
		if(unitlink == null)
		{
			unitlink = u;
		} else {
			unitlink.giveParent(u);
			u.giveChild(unitlink);
			unitlink = u;
		}
	}
	
	public void relink(Unit u)
	{
		unitlink = u;
	}
	
	public Unit getLink()
	{
		return unitlink;
	}
}
