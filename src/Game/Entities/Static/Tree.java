package Game.Entities.Static;

import java.awt.Graphics;
import java.awt.Rectangle;

import Main.Handler;
import Resources.Images;

public class Tree extends StaticBase {
	private Rectangle Tree;

	public Tree(Handler handler,int xPosition, int yPosition) {
		super(handler);
		 this.setY(yPosition);
	     this.setX(xPosition);
		
	}

	@Override
	public void render(Graphics g) {
		g.drawImage(Images.Tree, this.getX(), this.getY(), 64, 64, null);
		Tree = new Rectangle(this.getX(), this.getY()+5, 64, 55);

	}
	
    @Override
    public Rectangle GetCollision() {
    	return Tree;
    	//return new Rectangle();
    }
    
}
