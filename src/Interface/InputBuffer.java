package Interface;

public class InputBuffer
{
    private boolean mouseDragging = false;
    private boolean mouseJustReleased = false;
    private boolean mouseClicked = false;
    private int mouseClickedX;
    private int mouseClickedY;
    private int mouseMovedX;
    private int mouseMovedY;
    private int mouseCommandX;
    private int mouseCommandY;
    private boolean attack = false;
    private boolean attackclick = false;
    private boolean pause = false;
    private boolean control = false;
    private int number = -1;
    private boolean replay = false;
    private boolean restartRound = false;
    private boolean resetToBase = false;
    
    public void restart()
    {
    	restartRound = true;
    }
    public void replay()
    {
    	replay = true;
    }
    public void reset()
    {
    	resetToBase = true;
    }
    public void number(int n)
    {
    	number = n;
    }
    public void controlPressed()
    {
    	control = true;
    }
    public void controlReleased()
    {
    	control = false;
    }
    public void pause()
    {
    	pause = true;
    }
    public void aKeyPressed()
    {
    	attack = true;
    }
    public void leftMousePressed(int x, int y)
    {
    	if(attack)
    	{
    		mouseCommandX = x;
            mouseCommandY = y;
            mouseClicked = true;
            attackclick = true;
    	} else {
    		mouseDragging = true;
            mouseClickedX = x;
            mouseClickedY = y;
            mouseMovedX = x;
            mouseMovedY = y;
            mouseClicked = false;
    	}
    }
    public void leftMouseMoving(int x, int y)
    {
        mouseMovedX = x;
        mouseMovedY = y;
    }
    public void leftMouseReleased(int x, int y)
    {
    	if(attackclick)
    	{
    		attackclick = false;
    	} else {
    		mouseMovedX = x;
            mouseMovedY = y;
            mouseDragging = false;
            mouseJustReleased = true;
    	}
    }
    public void rightMousePressed(int x, int y)
    {
        mouseCommandX = x;
        mouseCommandY = y;
        mouseClicked = true;
    }
    public int[] view()
    {
    	if(mouseDragging)
        {
            return new int[]{1, mouseClickedX, mouseClickedY, mouseMovedX, mouseMovedY};
        } else return new int[]{0};
    }
    
    public int[] read()	//holds what is going to be commands for the game  engine
    {
        //key: 1 = selecting, 2 = selected, 3 = command, 4 = attack, 5 = pause, 6 = assignment, 7 = recall, 0 = nothing
        if(mouseJustReleased)
        {
            mouseJustReleased = false;
            return new int[]{2, mouseClickedX, mouseClickedY, mouseMovedX, mouseMovedY};
        }
        else if(mouseClicked)
        {
        	if(attack)	//attack some target or place
        	{
        		mouseClicked = false;
        		attack = false;
        		return new int[]{4, mouseCommandX, mouseCommandY};
        	} else {	//move
        		mouseClicked = false;
                return new int[]{3, mouseCommandX, mouseCommandY};
        	}
        }
        else if(pause)
        {
        	pause = false;
        	return new int[]{5};
        }
        else if(replay)
        {
        	replay = false;
        	return new int[]{8};
        }
        else if(control && number != -1)	//recall
        {
        	int re = number;
        	number = -1;
        	return new int[]{7,re};
        }
        else if(number != -1)	//assignment
        {
        	int re = number;
        	number = -1;
        	return new int[]{6, re};
        }
        else if(restartRound)
        {
        	restartRound = false;
        	return new int[]{9};
        }
        else if(resetToBase)
        {
        	resetToBase = false;
        	return new int[]{11};
        }
        else return new int[]{0};
    }
}
