package Game.World;

import Game.Entities.Dynamic.Player;
import Game.Entities.Static.LillyPad;
import Game.Entities.Static.Log;
import Game.Entities.Static.StaticBase;
import Game.Entities.Static.Tree;
import Game.Entities.Static.Turtle;
import Game.GameStates.State;
import Main.GameSetUp;
import Main.Handler;
import UI.UIManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * Literally the world. This class is very important to understand. Here we
 * spawn our hazards (StaticBase), and our tiles (BaseArea)
 * 
 * We move the screen, the player, and some hazards. How? Figure it out.
 */
public class WorldManager {

	private ArrayList<BaseArea> AreasAvailables; // Lake, empty and grass area (NOTE: The empty tile is just the "sand"
													// tile. Ik, weird name.)
	private ArrayList<StaticBase> StaticEntitiesAvailables; // Has the hazards: LillyPad, Log, Tree, and Turtle.

	private ArrayList<BaseArea> SpawnedAreas; // Areas currently on world
	private ArrayList<StaticBase> SpawnedHazards; // Hazards currently on world.
	public int LillyChoice = 1;
	Long time;
	Boolean reset = true;

	Handler handler;

	private Player player; // How do we find the frog coordinates? How do we find the Collisions? This bad
							// boy.

	UIManager object = new UIManager(handler);
	UI.UIManager.Vector object2 = object.new Vector();

	private ID[][] grid;
	private int gridWidth, gridHeight; // Size of the grid.
	private int movementSpeed; // Movement of the tiles going downwards.

	public WorldManager(Handler handler) {
		this.handler = handler;

		AreasAvailables = new ArrayList<>(); // Here we add the Tiles to be utilized.
		StaticEntitiesAvailables = new ArrayList<>(); // Here we add the Hazards to be utilized.

		AreasAvailables.add(new GrassArea(handler, 0));
		AreasAvailables.add(new WaterArea(handler, 0));
		AreasAvailables.add(new EmptyArea(handler, 0));

		StaticEntitiesAvailables.add(new LillyPad(handler, 0, 0));
		StaticEntitiesAvailables.add(new Log(handler, 0, 0));
		StaticEntitiesAvailables.add(new Tree(handler, 0, 0));
		StaticEntitiesAvailables.add(new Turtle(handler, 0, 0));

		SpawnedAreas = new ArrayList<>();
		SpawnedHazards = new ArrayList<>();

		player = new Player(handler);

		gridWidth = handler.getWidth() / 64;
		gridHeight = handler.getHeight() / 64;
		movementSpeed = 1;
		// movementSpeed = 20; I dare you.

		/*
		 * Spawn Areas in Map (2 extra areas spawned off screen) To understand this, go
		 * down to randomArea(int yPosition)
		 */

		for (int i = 0; i < gridHeight + 2; i++)
			// Draws every Area except the area where the frog will spawn
			if (i != 10)
				SpawnedAreas.add(randomArea((-2 + i) * 64));

		PlayerSpawn();
		

		// Not used atm.
		grid = new ID[gridWidth][gridHeight];
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				grid[x][y] = ID.EMPTY;
			}
		}
	}

	public void tick() {
		deadfrog();
		if (this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[2])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[1];
		}
		if (this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[0])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[2];
		}
		if (this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[1])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[0];
		}
		if (this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[3])) {
			this.object2.addVectors();
		}
		if (this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[4])
				&& this.object2.isUIInstance) {
			this.object2.scalarProduct(handler);
		}

		if (this.reset) {
			time = System.currentTimeMillis();
			this.reset = false;
		}

		if (this.object2.isSorted) {

			if (System.currentTimeMillis() - this.time >= 2000) {
				this.object2.setOnScreen(true);
				this.reset = true;
			}

		}
		for (BaseArea area : SpawnedAreas) {
			area.tick();
		}
		for (StaticBase hazard : SpawnedHazards) {
			hazard.tick();
		}

		for (int i = 0; i < SpawnedAreas.size(); i++) {
			SpawnedAreas.get(i).setYPosition(SpawnedAreas.get(i).getYPosition() + movementSpeed);

			// Check if Area (thus a hazard as well) passed the screen.
			if (SpawnedAreas.get(i).getYPosition() > handler.getHeight()) {
				// Replace with a new random area and position it on top
				SpawnedAreas.set(i, randomArea(-2 * 64));
			}
			// Make sure players position is synchronized with area's movement
			if (SpawnedAreas.get(i).getYPosition() < player.getY()
					&& player.getY() - SpawnedAreas.get(i).getYPosition() < 3) {
				player.setY(SpawnedAreas.get(i).getYPosition());
			}
		}

		HazardMovement();

		StaticTree();

		player.tick();
		// make player move the same as the areas
		player.setY(player.getY() + movementSpeed);

		object2.tick();

	}

// If the frog touches the water it sets game state to GameOver Statea
	public void deadfrog() {

		for (int i = 0; i < SpawnedAreas.size(); i++) {
			if(SpawnedAreas.get(i) instanceof WaterArea) {
				if (player.getPlayerCollision().getY() == SpawnedAreas.get(i).getYPosition()) {
					for (int j = 0; j < SpawnedHazards.size(); j++) {
						if(SpawnedHazards.get(j).GetCollision() != null && 
								player.getPlayerCollision().intersects(SpawnedHazards.get(j).GetCollision())) {
							return;
						}
					}
					State.setState(handler.getGame().GameOverState);
				}
			}
		}
	}

	// This method wont let the frog jump over the tree
	private void StaticTree() {

		for (int i = 0; i < SpawnedHazards.size(); i++) {

			if (SpawnedHazards.get(i) instanceof Tree) {
				if (SpawnedHazards.get(i).GetCollision() != null
						&& player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())) {
					if (player.Getfacing().equals("UP")) {
						player.setY(player.getY() + 16);
					} else if (player.Getfacing().equals("DOWN")) {
						player.setY(player.getY() - 16);
					} else if (player.Getfacing().equals("LEFT")) {
						player.setX(player.getX() + 16);
					} else if (player.Getfacing().equals("RIGHT")) {
						player.setX(player.getX() - 16);
					}

				}
			}
		}

	}

	// Makes the player not Spawn in the Water Area and draws the area where the
	// player will spawn
	public void PlayerSpawn() {
		int i = 10;
		SpawnedAreas.add(NotRandomArea((i - 2) * 64));

		if (SpawnedAreas.get(i) instanceof GrassArea) {
			player.setX((gridWidth / 2) * 64);
			player.setY((gridHeight - 3) * 64);

		} else if (SpawnedAreas.get(i) instanceof EmptyArea) {
			player.setX((gridWidth / 2) * 64);
			player.setY((gridHeight - 3) * 64);
		} else {

			player.setX((gridWidth / 2) * 64);
			player.setY((gridHeight - 3) * 64);
		}

	}

	private void HazardMovement() {

		for (int i = 0; i < SpawnedHazards.size(); i++) {

			// Moves hazard down
			SpawnedHazards.get(i).setY(SpawnedHazards.get(i).getY() + movementSpeed);

			// Moves Log or Turtle to the right
			if (SpawnedHazards.get(i) instanceof Log) {
				SpawnedHazards.get(i).setX(SpawnedHazards.get(i).getX() + 1);
				if (SpawnedHazards.get(i).getX() == 640) {
					SpawnedHazards.get(i).setX(-64 * 2);
				}

				// Verifies the hazards Rectangles aren't null and
				// If the player Rectangle intersects with the Log or Turtle Rectangle, then
				// move player to the right.
				if (SpawnedHazards.get(i).GetCollision() != null
						&& player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())) {
					player.setX(player.getX() + 1);

				}
			}
			if (SpawnedHazards.get(i) instanceof Turtle) {
				SpawnedHazards.get(i).setX(SpawnedHazards.get(i).getX() - 1);
				if (SpawnedHazards.get(i).getX() == 0) {
					SpawnedHazards.get(i).setX(768);
				}

				// Verifies the hazards Rectangles aren't null and
				// If the player Rectangle intersects with the Log or Turtle Rectangle, then
				// move player to the right.
				if (SpawnedHazards.get(i).GetCollision() != null
						&& player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())
						&& SpawnedHazards.get(i) instanceof Log) {
					player.setX(player.getX() + 1);

				}
				if (SpawnedHazards.get(i).GetCollision() != null
						&& player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())
						&& SpawnedHazards.get(i) instanceof Turtle) {
					player.setX(player.getX() - 1);

				}

			}

			// if hazard has passed the screen height, then remove this hazard.
			if (SpawnedHazards.get(i).getY() > handler.getHeight()) {
				SpawnedHazards.remove(i);
			}
		}
	}

	public void render(Graphics g) {

		for (BaseArea area : SpawnedAreas) {
			area.render(g);
		}

		for (StaticBase hazards : SpawnedHazards) {
			hazards.render(g);

		}

		player.render(g);
		this.object2.render(g);

	}

	/*
	 * Given a yPosition, this method will return a random Area out of the Available
	 * ones.) It is also in charge of spawning hazards at a specific condition.
	 */
	private BaseArea NotRandomArea(int yPosition) {
		Random rand = new Random();
		BaseArea randomArea = AreasAvailables.get(rand.nextInt(AreasAvailables.size()));
		if (randomArea instanceof GrassArea) {
			randomArea = new GrassArea(handler, yPosition);
			SpawnHazard(yPosition, randomArea);
		} else {
			randomArea = new EmptyArea(handler, yPosition);
		}

		return randomArea;

	}

	private BaseArea randomArea(int yPosition) {
		Random rand = new Random();

		// From the AreasAvailable, get me any random one.
		BaseArea randomArea = AreasAvailables.get(rand.nextInt(AreasAvailables.size()));

		if (randomArea instanceof GrassArea) {
			randomArea = new GrassArea(handler, yPosition);
			SpawnHazard(yPosition, randomArea);
		} else if (randomArea instanceof WaterArea) {
			randomArea = new WaterArea(handler, yPosition);
			SpawnHazard(yPosition, randomArea);

		}

		else {
			randomArea = new EmptyArea(handler, yPosition);
		}
		return randomArea;
	}

	/*
	 * This method Allow multiple lillyPads in the same line and also doesn't allow
	 * to spawn to LillyPads in a row
	 * 
	 */
	private void LillyPadSpawn(int yPosition, BaseArea area, int choice) {
		int randTimes;
		Random rand = new Random();
		int randInt;


		if (LillyChoice != 3) {

			randTimes = rand.nextInt(9);

			for (int i = 0; i <= randTimes; i++) {

				randInt = 64 * rand.nextInt(9);

				SpawnedHazards.add(new LillyPad(handler, randInt, yPosition));
				LillyChoice = 3;
				// System.out.println("After if" + LillyChoice);
			}
		} else {
			if (choice <= 2) {
				randInt = 64 * rand.nextInt(4);
				SpawnedHazards.add(new Log(handler, randInt, yPosition));
				LillyChoice = 2;
				// System.out.println("After if" + LillyChoice);
			} else {
				randInt = 64 * rand.nextInt(3);
				SpawnedHazards.add(new Turtle(handler, randInt, yPosition));
				LillyChoice = 4;
				// System.out.println("After if" + LillyChoice);
			}

		}

	}

	// spawns multiply turtles
	private void TurtleSpawn(int yPosition, BaseArea area, int choice) {
		Random rand = new Random();
		int randInt;
		int low = 1;
		int high = 5;
		int result = rand.nextInt(high - low) + low;
		int randTimesTur = result;
		;

		if (LillyChoice != 4) {

			randInt = rand.nextInt(9);
			// spawns the turtle ans set the distance between them
			for (int i = 1; i <= randTimesTur; i++) {

				if (randTimesTur == 1) {

					randInt = 0;
				} else if (randTimesTur == 2) {

					randInt = randInt + 5 * 64;
				} else if (randTimesTur == 3) {

					randInt = randInt + 4 * 64;
				} else if (randTimesTur == 4) {

					randInt = randInt + 3 * 64;
				}

				SpawnedHazards.add(new Turtle(handler, randInt, yPosition));

				LillyChoice = 4;

			}

		}

	}

// Spawns from 1 to 4 logs
	private void LogSpawn(int yPosition, BaseArea area, int choice) {

		Random rand = new Random();
		int randInt;
		int low = 1;
		int high = 5;
		int result = rand.nextInt(high - low) + low;
		int randTimesLog = result;
		;

		if (LillyChoice != 2) {

			randInt = rand.nextInt(9);
			// sets the discance bewteen each log
			for (int i = 1; i <= randTimesLog; i++) {

				if (randTimesLog == 1) {

				} else if (randTimesLog == 2) {

					randInt = randInt - 5 * 64;
				} else if (randTimesLog == 3) {

					randInt = randInt - 4 * 64;
				} else if (randTimesLog == 4) {

					randInt = randInt - 3 * 64;
				}

				SpawnedHazards.add(new Log(handler, randInt, yPosition));

				LillyChoice = 2;
				// System.out.println("After if" + LillyChoice);

			}

		}
	}

	/*
	 * Given a yPositionm this method will add a new hazard to the SpawnedHazards
	 * ArrayList LillyChoice sets to 1 if the last hazard was a tree LillyChoice
	 * sets to 2 if the last hazard was a Log LillyChoice sets to 3 if the last
	 * hazard was a LilliyPad LillyChoice sets to 4 if the last hazard was a turtle
	 * 
	 * 
	 */
	private void SpawnHazard(int yPosition, BaseArea area) {
		Random rand = new Random();
		int randInt;

		int choice = rand.nextInt(7);
		// Chooses between Log or Lillypad

		if (area instanceof GrassArea) {

			randInt = 64 * rand.nextInt(4);
			SpawnedHazards.add(new Tree(handler, randInt, yPosition));
			LillyChoice = 1;

		} else if (area instanceof WaterArea) {

			if (choice <= 2) {
				LogSpawn(yPosition, area, choice);

			} else if (choice >= 5) {

				LillyPadSpawn(yPosition, area, choice);

			} else {
				TurtleSpawn(yPosition, area, choice);

			}

		}

	}

}
