package Interface;

import Interface.Appearance.AppearanceUnit;

class InterpolatedAppearanceUnit
{
	private AppearanceUnit before;
	private AppearanceUnit after;
	int id;
	
	public InterpolatedAppearanceUnit(AppearanceUnit u)
	{
		id = u.id;
		before = u;
		after = u;
	}
	
	public void next(AppearanceUnit au)
	{
		before = after;
		after = au;
		age = 0;
	}
	
	private int age = 0;
	
	public int age()
	{
		age++;
		return age;
	}
	
	//percent refers to what point we interpolate to
	//distance 0 = 100% before
	//distance 0.5 = half way between before and after
	//distance 1 = 100% after
	public int[] renderLocation(double distance)
	{
		double one = 1 - distance;
		double two = distance;
		
		int x = (int) (one * before.x + two * after.x);
		int y = (int) (one * before.y + two * after.y);
		
		return new int[] {x, y};
	}
}