package spacecadets2016;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.GunTurnCompleteCondition;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RoundEndedEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class JammyDodgerV extends AdvancedRobot {

	private Map<String,Location> enemyLocations;
	private Map<String,RoboHitData> enemyData;
	private Map<Bullet,String> bulletTargets;
	Location dangerZone = null;
	
	double lastBearing;
	boolean onWall;
	double latestDistance;
	boolean enabled;
	boolean retreat;
	double toTurn;
	double toMove;
	boolean noTurn;
	double hits;
	double  misses;
	double averageHitDistance;
	int opponents;
	double wallTurn;
	
	boolean normal;
    boolean peek; // Don't turn if there's a robot there
    double moveAmount; // How much to move
    
    final static int TRIGGER = 1000;
	
	/**
	 * run: Test's default behavior
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		System.out.println("Jammy Dodger Mark V - Loading");
		
		enemyData = new HashMap<String,RoboHitData>();
		
		File file = getDataFile("output.dat");
		try {
			FileInputStream stream = new FileInputStream(file);
			
			FileReader fr = new FileReader(file);
			//BufferedReader br = new BufferedReader(stream);
			ObjectInputStream ois = new ObjectInputStream(stream);
			enemyData = (Map<String, RoboHitData>) ois.readObject();
			ois.close();
			
		} catch (IOException e) {
			System.out.println("Could not load previous file!");
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFound!");
		}
		
		/*setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);*/
		setBulletColor(Color.red);
		enemyLocations = new HashMap<String,Location>();
		bulletTargets = new HashMap<Bullet,String>();
		enabled = true;
		setColors(Color.getHSBColor(57, 18, 10),Color.red,Color.red); // body,gun,radar
		
		System.out.println("Enabled!");
		
		opponents = getOthers();
		
		if (getOthers() > TRIGGER) {
			// Initialize moveAmount to the maximum possible for this battlefield.
	        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
	        // Initialize peek to false
	        peek = false;

	        // turnLeft to face a wall.
	        // getHeading() % 90 means the remainder of
	        // getHeading() divided by 90.
	        turnLeft(getHeading() % 90);
	        ahead(moveAmount);
	        // Turn the gun to turn right 90 degrees.
	        peek = true;
	        turnGunRight(90);
	        turnRight(90);
		}

		// Robot main loop
		while(true) {

			if (getOthers() <= TRIGGER) {
				if (!normal) {
					normal = true;
					turnRight(90);
					ahead(100);
				}

				setAdjustRadarForGunTurn(true);
				setAdjustRadarForRobotTurn(true);
				setAdjustGunForRobotTurn(true);
				if (getEnergy() > 30) {
					if (getOthers() > 1) {
						basicEvade();
						//basicSeek();
					} else {
						basicSeek();
					}
				} else {
					System.out.println("[ALERT] Energy Low - Evasion activated");
					basicEvade();
				}
			} else {
				wallSkim();
			}
		}
	}
	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		File file = getDataFile("output.dat");
		try {
			RobocodeFileOutputStream stream = new RobocodeFileOutputStream(file);
			PrintStream ps = new PrintStream(stream);
			//FileWriter fw = new FileWriter(stream);
			ps.print(enemyData);
			
			ps.close();
			System.out.println("SAVED!");
			System.out.println(file.getAbsolutePath());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*@Override
	public void onWin(WinEvent event) {
		File file = getDataFile("output.dat");
		try {
			RobocodeFileOutputStream stream = new RobocodeFileOutputStream(file);
			PrintStream ps = new PrintStream(stream);
			ps.print(enemyData);
			
			ps.close();
			System.out.println("SAVED!");
			System.out.println(file.getAbsolutePath());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}*/
	
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		RoboHitData temp;
		if (enemyData.get(event.getName()) !=null) {
			temp = enemyData.get(event.getName());
			temp.distanceTotal += enemyLocations.get(event.getName()).d;
			temp.hits += 1;
			System.out.println("[INFO] Bullet hit enemy: " + event.getName());
			System.out.println("[INFO] My accuracy of hitting this enemy is: " + temp.getAccuracy());
		}
		super.onBulletHit(event);
	}
	
	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		RoboHitData temp;
		if (bulletTargets.get(event.getBullet()) == null) {
			return;
		}
		if (enemyData.get(bulletTargets.get(event.getBullet())) !=null) {
			temp = enemyData.get(bulletTargets.get(event.getBullet()));
			temp.misses += 1;
		}
		super.onBulletMissed(event);
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		RoboHitData temp;
		if (bulletTargets.get(event.getBullet()) == null) {
			return;
		}
		if (enemyData.get(bulletTargets.get(event.getBullet())) !=null) {
			temp = enemyData.get(bulletTargets.get(event.getBullet()));
			temp.misses += 1;
		}
		super.onBulletHitBullet(event);
	}
	
	private void wallSkim() {
              // Look before we turn when ahead() completes.
              peek = true;
              // Move up the wall
              setAhead(moveAmount);
              // Don't look now
              peek = false;
              // Turn to the next wall
              setTurnRight(90);
              execute();
	}
	
	private void basicSeek() {
		//ahead(100);
		turnRadarRight(360);
		if (!noTurn) {
		if (!retreat) {
			toMove = toMove + latestDistance/2;
			setAhead(toMove);
			setTurnRight(toTurn);
		} else {
			setAhead(toMove);
			retreat = false;
		}
		}
		
		if (noTurn) {
			if (!(getX() > getBattleFieldWidth() - 50 || getY() > getBattleFieldHeight() - 50 || getX() < 50 || getY() < 50)) {
				noTurn = false;
				
			}
			if (getDistanceRemaining() == 0) {
				setAhead(100);
			}
			setTurnRight(wallTurn);
		}
		execute();
		toMove = 0;
		toTurn = 0;
	}
	
	private void basicEvade() {
		setTurnRadarRight(90);
		int aheadVal = 100;
		if (!noTurn) {
		if (dangerZone == null) {
			toTurn = toTurn + (90*Math.random());
		} else {
			if (Math.abs(getX() - dangerZone.x) < 200 && Math.abs(getY() - dangerZone.y) < 200) {
				toTurn = (dangerZone.getBearing(getX(), getY(), getHeading()) + 180);
				aheadVal = aheadVal + 100;
			} else {
				toTurn = toTurn + (90*Math.random());
			}
			
		}
		toMove = toMove + aheadVal;
		setTurnRight(toTurn);
		setAhead(toMove);
		}
		if (!onWall) {
		if (getX() > getBattleFieldWidth()-(getBattleFieldWidth()/4) && getHeading() >= 0 && getHeading() <= 180) {
			setTurnRight(90);
		} else if (getX() > getBattleFieldWidth()-(getBattleFieldWidth()/4) ) {
			setTurnRight(0);
		} else if (getX() < (getBattleFieldWidth()/4) && getHeading() >= 180 && getHeading() <= 360) {
			setTurnRight(90);
		} else if(getX() < (getBattleFieldWidth()/4) ) {
			setTurnRight(0);
		} else if (getY() > getBattleFieldHeight()-(getBattleFieldHeight()/4) && (getHeading() >= 270 && getHeading() <= 360) || (getHeading() >= 0 && getHeading() <= 90)) {
			setTurnRight(90);
		} else if (getY() > getBattleFieldHeight()-(getBattleFieldHeight()/4)) {
			setTurnRight(0);
		} else if (getY() < (getBattleFieldHeight()/4) && (getHeading() >= 90 && getHeading() <= 270) ) {
			setTurnRight(90);
		} else if (getY() < (getBattleFieldHeight()/4)){
			setTurnRight(0);
		}
		}
		if (noTurn) {
			if (!(getX() > getBattleFieldWidth() - 50 || getY() > getBattleFieldHeight() - 50 || getX() < 50 || getY() < 50)) {
				noTurn = false;
			}
			if (getDistanceRemaining() == 0) {
				setAhead(100);
			}
			setTurnRight(wallTurn);
		}
		execute();
		retreat = false;
	}
	
	private Location getDangerZone() {
		Location dangerZone = null;
		
		double avX = 0;
		double avY = 0;
		int counter = 0;
		
		for (Location l : enemyLocations.values()) {
			avX = avX + l.x;
			avY = avY + l.y;
			counter++;
		}
		
		avX = avX/counter;
		avY = avY/counter;
		
		dangerZone = new Location(avX,avY);
		
		return dangerZone;
	}
	
	private void fireScanned(Location l, double bearing, String name) {
		setAdjustRadarForGunTurn(true);
		turnGunRight(getHeading() - getGunHeading() + bearing);
		waitFor(new GunTurnCompleteCondition(this));
		Bullet b = fireBullet(Math.min(400/l.d,3));
		
		bulletTargets.put(b, name);
		
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		enemyLocations.put(e.getName(), new Location(getRadarHeading()/360 * 2*Math.PI, getX(), getY(), e.getDistance()));
		if (enemyData.get(e.getName()) == null) {
			enemyData.put(e.getName(), new RoboHitData());
		}
		dangerZone = getDangerZone();
		if (!enabled) {
			return;
		}
		// Replace the next line with any behavior you would like
		latestDistance = e.getDistance();
		lastBearing = e.getBearing();
		/*if (e.getDistance() <= 100) {
			fire(5);
		} else if (e.getDistance() <= 150) {
			fire(4);
		} else if (e.getDistance() <= 150) {
			fire(3);
			//turnRight(this.getHeading() + e.getBearing());
		} else if (e.getDistance() <= 300) {
			//turnRight(this.getHeading() + e.getBearing());
			//ahead(100);
			fire(2);
		} else if (e.getDistance() <= getBattleFieldWidth()/2){
			//turnRight(this.getHeading() + e.getBearing());
			//ahead(100);
			fire(1);
			//rapidFire(1,2);
		}*/
		
		if (getOthers() > TRIGGER) {
			 fire(2);
	            if (peek) {
	                  scan();
	            }
	            return;
		}
		
		if (!(getEnergy() < 5) || e.getEnergy() < 20) {
				if (enemyData.get(e.getName()).getAccuracy() < 20) {
					if (e.getDistance() < enemyData.get(e.getName()).getAverageHitDistance() + 20) {
						fireScanned(enemyLocations.get(e.getName()),e.getBearing(), e.getName());
					} else {
						if (enemyData.get(e.getName()).hits == 0) {
							fireScanned(enemyLocations.get(e.getName()),e.getBearing(), e.getName());
						}
					}
			} else {
				fireScanned(enemyLocations.get(e.getName()),e.getBearing(), e.getName());
			}
		}
		//if (!(getOthers() > 1)) {
			if (e.getBearing() > 0) {
				toTurn = toTurn + (45 + 45 * Math.abs(Math.sin(e.getDistance())));
			} else if (e.getBearing() < 0) {
				toTurn = toTurn - (45 + 45* Math.abs(Math.sin(e.getDistance())));
			}
		//}
		if (e.getEnergy() > getEnergy() + 10 && e.getDistance() < 100) {
			System.out.println("[ALERT] Scanned robot has superior energy levels & is near - Retreat!");
			//back(200);
			toMove = -200;
			retreat = true;
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		System.out.println("[ALERT] Hit by bullet");
		if (getOthers() >TRIGGER ) {
			return;
		}
		if (!enabled) {
			return;
		}
		if (!onWall) {
		// Replace the next line with any behavior you would like
		//turnRight(this.getHeading() + e.getBearing() + 45);
			toTurn = (Math.random() * 45);
			toMove = (Math.random()*50 + 50);
		}
		
	}
	
	public void reAlign() {
		System.out.println("[INFO] Realigning to last heading");
		if (!enabled) {
			return;
		}
		setTurnGunRight(this.getHeading() + lastBearing);
	}
	
	public void onHitEnemy(HitRobotEvent e) {
		
		if (getOthers() > TRIGGER) {
			if (e.getBearing() > -90 && e.getBearing() < 90) {
                back(100);
          } // else he's in back of us, so set ahead a bit.
          else {
                ahead(100);
          }
			return;
		}
		
		if ((getOthers() > 1 && getEnergy()>50) || getOthers() == 1) {
		turnGunRight(this.getHeading() - this.getGunHeading() + e.getBearing());
		fire(Rules.MAX_BULLET_POWER);
		toMove = -(10);
		}
		
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		System.out.println("[INFO] Collision with wall");
		if (getOthers() > TRIGGER) {
			return;
		}
		if (!enabled) {
			return;
		}
		if (!onWall) {
		onWall = true;
		// Replace the next line with any behavior you would like
		wallTurn = (e.getBearing() + 180 + Math.random()* 45);
		//waitFor(new TurnCompleteCondition(this));
		noTurn = true;
		//reAlign();
		if (getOthers() > 1) {
			toMove = toMove + (100);
		}
		setAhead(toMove);
		toMove = 0;
		onWall = false;
		}
	}	
	
}
