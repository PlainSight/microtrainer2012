package Core;


public class Unit
{
	private UnitType type;
	
	
	private int id;
	private int faction;
	private int x;
	private int y;
	private int hp;
	private int targetx;
	private int targety;
	private Unit target;
	private int coolDown;
	private int speed;	//this is currently unused but may be needed if movement modifiers are added
	private UnitState state;
	private boolean alive = true;
	private boolean selected = false;
	Quadtree quad = null;	//used for target finding - stores root node of other factions quadtree
	Quadtree myquad = null;	//used for adding to own factions quadtree when required because of movement
	Quadtree specificquad = null;	//used for removing from quadtree
	
	GeohashSpace myGeo = null;
	Unit parent = null;
	Unit child = null;
	
	public Unit(UnitType t, int spawnx, int spawny, int id)
	{
		type = t;
		x = spawnx;
		y = spawny;
		faction = type.faction;
		hp = type.maxHp;
		speed = type.moveSpeed;
		state = UnitState.STOPPED;
	}
	
	//geohash stuff
	public void giveParent(Unit u)
	{
		parent = u;
	}
	public void giveChild(Unit u)
	{
		child = u;
	}
	public Unit getChild(Unit u)
	{
		return child;
	}
	public Unit getParent(Unit u)
	{
		return parent;
	}
	public void giveSpecificGeo(GeohashSpace s)
	{
		myGeo = s;
	}
	private void removeSpecificGeo()
	{
		if(myGeo == null) return;
		
		if(parent == null)
		{
			if(child != null)
			{
				child.giveParent(null);
			}
			myGeo.relink(child);
		} else {
			if(child != null)
			{
				child.giveParent(parent);
			}
			parent.giveChild(child);
		}
		child = null;
		parent = null;
	}
	
	public int getId()
	{
		return id;
	}
	
	
	public void giveTargetQuadtree(Quadtree q)
	{
		quad = q;
	}
	
	public void giveQuadtree(Quadtree q)
	{
		myquad = q;
	}
	
	public void giveSpecificQuadtree(Quadtree q)
	{
		specificquad = q;
	}
	
	//for use with swarm pathing
	private int desiredX;
	private int desiredY;
	private boolean canMove;
	private boolean hasDesire;	//desired units have priority
	
	private int xMove;
	private int yMove;
	
	public void moveCommand(int tx, int ty)
	{
		//System.out.println("move");
		targetx = tx;
		targety = ty;
		state = UnitState.MOVING;
	}
	public void attackCommand(Unit u)
	{
		//System.out.println("attack");
		target = u;
		state = UnitState.ATTACKING;
	}
	public void attackMoveCommand(int tx, int ty)
	{
		//System.out.println("attack move");
		targetx = tx;
		targety = ty;
		state = UnitState.ATTACKMOVING;
	}
	
	public boolean hasTarget()
	{
		return !(target==null);
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void die()
	{
		removeSpecificGeo();
		specificquad.remove(this);
	}
	
	public int getRange()
	{
		return type.range;
	}
	
	public void setSelected()
	{
		selected = true;
	}
	
	public void setUnselected()
	{
		selected = false;
	}
	
	public void setDoMove(int tx, int ty)
	{
		xMove = tx;
		yMove = ty;
	}
	
	public void doMove()
	{
		removeSpecificGeo();
		
		x += xMove;
		y += yMove;
		
		Engine.hash.hash(this);
		
		//System.out.println(this + " " + specificquad.hasUnitInside(this) + " " + specificquad.minx + " <= " + x + " < " + specificquad.maxx + " " + specificquad.miny + " <= " + y + " < " + specificquad.maxy); 
		
		if(specificquad == null)
		{
			//this means that a unit moved, and would have been good to remove from quad but the quad got split first and as there was no valid quad meant it got confused.
			
			myquad.add(this);
		} else {
			if(!specificquad.hasUnitInside(this))
			{
				specificquad.remove(this);

				myquad.add(this);
			}
		}
		
	}
	
	public UnitState getState()
	{
		return state;
	}
	
	public boolean hasDesire()
	{
		return hasDesire;
	}
	
	public boolean canMove()
	{
		return canMove;
	}
	
	public int getDesiredX()
	{
		return desiredX;
	}
	
	public int getDesiredY()
	{
		return desiredY;
	}
	
	//states: Stopped, Attacking, Moving, AttackMoving		maybe include HoldPosition later

	public int getX() 
	{
		return x;
	}
	public int getY() 
	{
		return y;
	}
	public int getR()
	{
		return type.radius;
	}
	public int getSpeed()
	{
		return type.moveSpeed;
	}
	public int getFaction()
	{
		return faction;
	}
	public int getHp()
	{
		return hp;
	}
	
	public void takeTurn()
	{
		
		//need to set up default things
		canMove = true;
		hasDesire = false;
		
		if(target != null)
		{
			if(target.isDead())
			{
				target = null;
			}
		}
		
		//move towards attack target if not in range
		
		//if in range damage target if cooldown is not up
		
		if(coolDown != 0)
		{
			coolDown--;
		}
		
		//ATTACKING
		if(state == UnitState.ATTACKING)
		{
			if(target != null)
			{
				if(!target.isDead())
				{
					//attack the unit
					attack(target);
				} else {
					state = UnitState.STOPPED;
					target = null;
				}
			} else {
				state = UnitState.STOPPED;
			}
			//revert to stopped
		}
		
		//STOPPED
		if(state == UnitState.STOPPED)	//this should be same as attack move but without the moving
		{
			//attack target if have one
			if(target != null)
			{
				if(!target.isDead())
				{
					if(distance(target) < type.targetAquisitionRange)
					{
						attack(target);
						return;
					} else {
						target = null;
					}
				}
			}
			
			//prompt the engine to find units
			//if don't, try to find target then attack
			Unit u = quad.findNearest(this);
			if(u != null)
			{
				if(distance(u) < type.targetAquisitionRange)
				{
					target = u;
				}
			}
			
			if(target != null)
			{
				attack(target);
			}
		}
		
		//MOVING
		if(state == UnitState.MOVING)
		{
			target = null;
			//move towards target except if it is obstructed by friendly units with the same target
			
			//if at location or near location but blocked by friendly units who have same move
			//target then state = Stopped
			
			if(distanceFromTarget() < 20)
			{
				state = UnitState.STOPPED;
				return;
			}
			
			move(targetx, targety);
			return;
		}
		
		//ATTACK MOVING
		if(state == UnitState.ATTACKMOVING)
		{
			//attack target if have one
			if(target != null)
			{
				if(!target.isDead())	//target is alive
				{
					if(distance(target) < type.targetAquisitionRange)
					{
						attack(target);
						return;
					} else {
						target = null;
					}
				} else {
					target = null;
				}
			}
			
			//prompt the engine to find units
			//if don't, try to find target then attack
			
			
			Unit u = quad.findNearest(this);
			if(u != null)
			{
				if(distance(u) < type.targetAquisitionRange)
				{
					target = u;
				}
			}
			
			if(target != null)
			{
				if(!target.isDead())
				{
					attack(target);
				} else {
					move(targetx, targety);
				}
			} else {
				move(targetx, targety);
			}
			
		}
	}
	
	public void attack(Unit u)
	{
		if(distance(u) > type.range)
		{
			move(u.getX(), u.getY());
		} else {
			canMove = false;
			if(coolDown == 0)
			{
				u.damage(type.damage);
				shot = u;
				coolDown = type.maxCoolDown;
			}
		}
		
		
	}
	public void move(int x, int y) //will probably set target vectors or something
	{
		hasDesire = true;
		desiredX = x;
		desiredY = y;
	}
	
	
	private Unit shot;
	
	public Unit getShot()
	{
		Unit temp = shot;
		shot = null;
		return temp;
	}

	public boolean isDead() {
		return !alive;
	}

	public void damage(int d) 
	{
		//System.out.println(this.toString() + " getting hit");
		hp -= d;
		if(hp < 1) alive = false;
	}

	public double distance(Unit u)
	{
		return Math.hypot(x - u.getX(), y - u.getY());
	}
	
	public double distanceFromTarget()
	{
		return Math.hypot(x - targetx, y - targety);
	}
}