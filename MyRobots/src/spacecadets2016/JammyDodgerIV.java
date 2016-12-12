package spacecadets2016;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.GunTurnCompleteCondition;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class JammyDodgerIV extends AdvancedRobot {

	private Map<String,Location> enemyLocations;
	Location dangerZone = null;
	
	double lastBearing;
	boolean onWall;
	double latestDistance;
	boolean enabled;
	boolean retreat;
	
	/**
	 * run: Test's default behavior
	 */
	@Override
	public void run() {
		
		System.out.println("Jammy Dodger Mark IV - Loading");
		
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setBulletColor(Color.red);
		enemyLocations = new HashMap<String,Location>();
		enabled = true;
		setColors(Color.getHSBColor(57, 18, 10),Color.red,Color.red); // body,gun,radar
		
		System.out.println("Enabled!");

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			if (getEnergy() > 15) {
				if (getOthers() > 1) {
					//basicEvade();
					basicEvade();
				} else {
					basicSeek();
				}
			} else {
				System.out.println("[ALERT] Energy Low - Evasion activated");
				basicEvade();
			}
		}
	}
	
	
	
	private void basicSeek() {
		//ahead(100);
		turnRadarRight(360);
		if (!retreat) {
			ahead(latestDistance /2);
		} else {
			retreat = false;
		}
		execute();
		//back(100);
		//turnGunRight(360);
	}
	
	private void basicEvade() {
		setTurnRadarRight(90);
		int aheadVal = 100;
		if (dangerZone == null) {
			setTurnRight(90*Math.sin(latestDistance));
		} else {
			if (Math.abs(getX() - dangerZone.x) < 200 && Math.abs(getY() - dangerZone.y) < 200) {
				setTurnRight((dangerZone.getBearing(getX(), getY(), getHeading()) + 180)/2);
				aheadVal = aheadVal + 100;
			} else {
				if (getX() > getBattleFieldWidth() - 50 || getY() > getBattleFieldHeight() - 50 || getX() < 50 || getY() < 50) {
					//setTurnLeft(90 + 90*Math.sin(latestDistance));
					reAlign();
				} else {
					setTurnRight(90*Math.sin(latestDistance));
				}
				
			}
		}
		setAhead(aheadVal);
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
	
	
	private void fireScanned(Location l, double bearing) {
		setAdjustRadarForGunTurn(true);
		turnGunRight(getHeading() - getGunHeading() + bearing);
		waitFor(new GunTurnCompleteCondition(this));
		fire(Math.min(400/l.d,3));
		
		/*double t;
		t = (l.d / (20 - 3*(Math.min(400/l.d, 3))));
		
		double ss;
		
		ss = t*l.d;
		
		double theta = Math.atan(ss/l.d);*/
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		enemyLocations.put(e.getName(), new Location(getRadarHeading()/360 * 2*Math.PI, getX(), getY(), e.getDistance()));
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
		if ((getOthers() > 1)|| e.getDistance() < 300) {
			if (!(getEnergy() < 20) || e.getEnergy() < 20) {
				fireScanned(enemyLocations.get(e.getName()),e.getBearing());
			}
		}
		if (!(getOthers() > 1)) {
			if (e.getBearing() > 0) {
				setTurnRight(45 + 45 * Math.abs(Math.sin(e.getDistance())));
			} else if (e.getBearing() < 0) {
				setTurnLeft(45 + 45* Math.abs(Math.sin(e.getDistance())));
			}
		}
		if (e.getEnergy() > getEnergy() + 10 && e.getDistance() < 100) {
			System.out.println("[ALERT] Scanned robot has superior energy levels & is near - Retreat!");
			back(200);
			retreat = true;
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		System.out.println("[ALERT] Hit by bullet");
		if (!enabled) {
			return;
		}
		if (!onWall) {
		// Replace the next line with any behavior you would like
		//turnRight(this.getHeading() + e.getBearing() + 45);
			setTurnRight(Math.random() * 45);
			setAhead(Math.random()*50 + 50);
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
		
		turnGunRight(this.getHeading() - this.getGunHeading() + e.getBearing());
		fire(Rules.MAX_BULLET_POWER);
		setBack(10);
		
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		System.out.println("[INFO] Collision with wall");
		if (!enabled) {
			return;
		}
		if (!onWall) {
		onWall = true;
		// Replace the next line with any behavior you would like
		turnRight(e.getBearing() + 180 + Math.random()* 45);
		//reAlign();
		if (getOthers() > 1) {
			setAhead(100);
		}
		setAhead(100);
		onWall = false;
		}
	}	
	
}
