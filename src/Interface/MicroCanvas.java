package Interface;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;

import Core.Command;
import Core.Engine;
import Core.GenericCommand;

public class MicroCanvas extends Canvas implements KeyListener, MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	private Graphics g;
	private int width;
	private int height;
	private Engine engine;
	private MicroCanvas thisCanvas = this;
	
	private BufferStrategy buffer;
	private BufferedImage bufferedImage;
	private Graphics2D g2d = null;
	private Color background = Color.white;
	
	private Color fC;
	private Color sC;
	
	private int tick;
	
	private InputBuffer input;
	private LinkedList<Command> commandQueue;
	
	private LinkedList<Appearance> appearanceQueue = new LinkedList<Appearance>();
	
	
//	private boolean finishedLastPaint = true;
	
	public MicroCanvas(int x, int y, Engine e, InputBuffer b) {
		width = x;
		height = y;
		
		input = b;
		engine = e;
		commandQueue = e.getCommmandQueue();
		
		setIgnoreRepaint(true);
		
		setPreferredSize(new Dimension(x, y));
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setVisible(true);
	}
	
	public void fix()
	{
		createBufferStrategy(4);
		buffer = thisCanvas.getBufferStrategy();
		bufferedImage = (BufferedImage) thisCanvas.createImage(1920, 1080);
	}
	
	public void refresh()
	{
		//System.out.println("start render: " + System.nanoTime());
		
		paint();
	}
	
	
	//multithreaded rendering
	public void paint()
	{
		(new Thread() {
		public void run()
		{
			Appearance latest = null;
			while(true)
			{
				//waits for next appearance
//				while(appearanceQueue.isEmpty())
//				{
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				//continually rerenders until there is another appearance, however renders may cause some delays
//				if(!appearanceQueue.isEmpty())
//				{
//					latest = appearanceQueue.pop();
//				}
				
				//this continually rerenders until a new appearance then gets the latest appearance
				//can have some jerky motions
				while(!appearanceQueue.isEmpty())
				{
					latest = appearanceQueue.pop();
				}
				
				try
				{
					g2d = bufferedImage.createGraphics();
					g2d.setColor(background);
					g2d.fillRect(0, 0, thisCanvas.getWidth(), thisCanvas.getHeight());

					//draw to g
					//drawScreen(appearanceQueue.pop());
					if(latest != null)
					{
						drawScreen(latest);
					}

					g = buffer.getDrawGraphics();
					g.drawImage(bufferedImage, 0, 0, null);
					if( !buffer.contentsLost())
					{
						buffer.show();
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				} finally {
					if(g != null)
					{
						g.dispose();
					}

					if(g2d != null)
					{
						g2d.dispose();
					}
				}


			}
		}
		}).start();
	}
	
	public void addAppearance(Appearance a)
	{
		appearanceQueue.add(a);
	}
	
	public void drawScreen(Appearance a)
	{
		//fC for first Colour sC for second Colour
		
		tick = a.tick;
		
		for(int i = 0; i < a.units.length; i++)
		{
			int x = a.units[i].x;
			int y = a.units[i].y;
			int r = a.units[i].r;
			int c = a.units[i].faction;	//if 1 fC, if 2 sC
			
			g2d.setColor(c == 1 ? fC : sC);
			g2d.fillOval(x-r, y-r, r*2, r*2);
			g2d.drawString(Integer.toString(a.units[i].hp), x, y-r-10);
			
			
			if(a.units[i].selected)
			{
				g2d.setColor(Color.green);
				g2d.drawOval(x-r-2, y-r-2, r*2 + 4, r*2 + 4);
			}
		}
		
		for(int i = 0; i < a.shots.length; i++)
		{
			g2d.setColor(Color.black);
			g2d.drawLine(a.shots[i].xo, a.shots[i].yo, a.shots[i].xt, a.shots[i].yt);
		}
		
		//draw selection boxes
        int[] iN = input.view();
        if(iN[0] == 1)
        {
            int xbox = Math.min(iN[1], iN[3]);
            int ybox = Math.min(iN[2], iN[4]);
            g2d.setColor(Color.green);
            g2d.drawRect(xbox, ybox, Math.abs(iN[1]-iN[3]), Math.abs(iN[2]-iN[4]));
        }
        
        if(engine.isReplay())
		{
			g2d.setColor(Color.black);
			{
				g2d.drawString("Replay", 350, 30);
			}
		}
        
        g2d.setColor(Color.black);
        
        if(1000000000 < (System.nanoTime() - lastTime))
        {
        	fps = framesInLastSecond;
        	framesInLastSecond = 0;
        	lastTime = System.nanoTime();
        }
        framesInLastSecond++;
        g2d.drawString("FPS: " + fps, 6, 14);
        
        
	}
	
	private long lastTime = System.nanoTime();
	private int fps = 60;
	private int framesInLastSecond = 0;
	
	public void setColours(String[] c)
	{
		fC = cToC(c[0]);
		sC = cToC(c[1]);
	}
	
	private Color cToC(String c)
	{
		//"Red", "Blue", "Green", "Purple", "Black"
		if(c.equals("Red"))
		{
			return Color.red;
		}
		if(c.equals("Blue"))
		{
			return Color.blue;
		}
		if(c.equals("Green"))
		{
			return Color.green;
		}
		if(c.equals("Purple"))
		{
			return Color.magenta;
		} else {
			return Color.black;
		}
	}
	
	public void checkCommands()
    {
		int[] co = input.read();
    	if(co[0] != 0)
        {
        	commandQueue.add(new GenericCommand(tick + 1, co[0], Arrays.copyOfRange(co, 1, co.length)));
        }
    }
	
	public void mouseReleased(MouseEvent e)
    {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            input.leftMouseReleased(e.getX(), e.getY());
        }
        checkCommands();
    }
    
    public void mouseDragged(MouseEvent e)
    {
    	//for some reason mouse Dragging cannot see what mouse button is dragging
    	input.leftMouseMoving(e.getX(), e.getY());
    	checkCommands();
    }
    
    public void mousePressed(MouseEvent e)
    {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            input.leftMousePressed(e.getX(), e.getY());
        }
        if(e.getButton() == MouseEvent.BUTTON3)
        {
            input.rightMousePressed(e.getX(), e.getY());
        }
        checkCommands();
    }
    
    
    public void keyPressed(KeyEvent k) 
    {
		if(k.getKeyChar() == 'a' || k.getKeyChar() == 'A')
		{
			input.aKeyPressed();
		}
		if(k.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			input.controlPressed();
		}
		if(k.getKeyChar() <= 57 && k.getKeyChar() >= 48)	//accepts numbers in range 0-9
		{
			input.number(k.getKeyChar() - 48);
		}
		checkCommands();
	}

	public void keyReleased(KeyEvent k)
	{
		if(k.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			input.controlReleased();
		}
		checkCommands();
	}
	
	public void mouseEntered(MouseEvent arg0)
	{
		
	}
	
	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mouseMoved(MouseEvent arg0)	{}
	public void mouseClicked(MouseEvent arg0) {}
	public void keyTyped(KeyEvent arg0)	{}
}
