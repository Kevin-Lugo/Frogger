package Game.GameStates;

import Main.Handler;
import Resources.Images;
import UI.UIImageButton;
import UI.UIManager;
import Game.Entities.Dynamic.Player;
import java.awt.*;

public class GameOverState extends State {
		private int count = 0;
		private UIManager uiManager;
		private Player player = new Player(handler);
		private Handler Handler = handler;
		
	public GameOverState(Handler handler) {
		super(handler);
		uiManager = new UIManager(handler);
		handler.getMouseManager().setUimanager(uiManager);
		/*
         * Adds a button that by being pressed changes the State
         */
		uiManager.addObjects(new UIImageButton(33, handler.getGame().getHeight() - 150, 128, 64, Images.butstart, () -> {
            handler.getMouseManager().setUimanager(null);
            State.setState(handler.getGame().gameState);
            handler.getGame().reStart();
        }));
		
		 uiManager.addObjects(new UIImageButton(33 + 192 * 2,  handler.getGame().getHeight() - 150, 128, 64, Images.BTitle, () -> {
	            handler.getMouseManager().setUimanager(null);
	            State.setState(handler.getGame().menuState);
	        }));
		
	}

	@Override
	public void tick() {
		
		 handler.getMouseManager().setUimanager(uiManager);
	        uiManager.tick();
	        count++;
	        if( count>=30){
	            count=30;
	        }
	        if(handler.getKeyManager().pbutt && count>=30){
	            count=0;
	            State.setState(handler.getGame().gameState);
	        }
	}

	@Override
	public void render(Graphics g) {
		
		 g.drawImage(Images.GameOver,0,0,handler.getGame().getWidth(),handler.getGame().getHeight(),null);
	        uiManager.Render(g);
	        
	        g.setColor(Color.WHITE);
	        g.setFont(new Font("Times New Roman", Font.PLAIN, 50));
	        g.drawString( "SCORE: " + String.valueOf(handler.getPlayer().highscore) , handler.getWidth()/2 - 96, 100);
		
	}

}
