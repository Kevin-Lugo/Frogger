package Game.World;

import java.awt.Graphics;
import java.util.Random;

import java.awt.Rectangle;
import Main.Handler;
import Resources.Animation;
import Resources.Images;
import jdk.internal.org.objectweb.asm.Handle;


public class WaterArea extends BaseArea {

    private Animation anim;
    private Rectangle water;
    

    WaterArea(Handler handler, int yPosition) {
        super(handler, yPosition);
        // Instantiate the animation of this Water, and it starts it at a random frame.
        anim=new Animation(384,Images.Water,new Random().nextInt(3));

    }

    @Override
    public void tick() {
        anim.tick();	// Animation frame movement.
    }

    @Override
    public void render(Graphics g) {
        for (int i = 0; i < 9; i++) {
            g.drawImage(anim.getCurrentFrame(), i*64, yPosition,64,66, null);
        }
    }
    public Rectangle GetCollision() {
    	return water;
    }
    public double getMaxY() {
    	
    	return this.water.getMaxY();
    }
    public double getMinY() {
    	return this.water.getMinY();
    }
    
    
    }

