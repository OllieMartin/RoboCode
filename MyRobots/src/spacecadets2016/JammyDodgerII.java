package spacecadets2016;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class JammyDodgerII extends AdvancedRobot {

	private Map<String,Location> enemyLocations;
	Location dangerZone = null;
	
	double lastBearing;
	boolean onWall;
	double latestDistance;
	boolean enabled;
	boolean found;
	private byte scanDirection = 1;
	
	/**
	 * run: Test's default behavior
	 */
	@Override
	public void run() {
		
		enemyLocations = new HashMap<String,Location>();
		
		setColors(Color.black, Color.black, Color.black);
		if (getX() < getBattleFieldWidth()/2) {
			turnRight(360 - getHeading() + 90);
			ahead(getBattleFieldWidth()/2 - getX());
		}
		else if (getX() > getBattleFieldWidth()/2) {
			turnRight(360 - getHeading() - 90);
			ahead(getX() - getBattleFieldWidth()/2);
		}
		if (getY() < getBattleFieldHeight()/2) {
			turnRight(360 - getHeading());
			ahead(getBattleFieldHeight()/2 - getY());
		}
		else if (getY() > getBattleFieldHeight()/2) {
			turnRight(360 - getHeading() - 180);
			ahead(getY() - getBattleFieldHeight()/2);
		}
		
		enabled = true;
		
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.getHSBColor(57, 18, 10),Color.red,Color.red); // body,gun,radar

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			if (getOthers() > 1) {
				basicEvade();
				//basicSeek();
			} else {
				basicSeek();
			}
		}
	}
	
	
	
	private void basicSeek() {
		//ahead(100);
		//if (!found || !enabled)
		setTurnRadarRight(360);
		execute();
		/*else {
			scanDirection *= -1;
			setTurnRadarRight(360 * scanDirection);
			execute();
		}*/
		ahead(latestDistance /2);
		//back(100);
		//turnGunRight(360);
	}
	
	private void basicEvade() {
		setTurnRadarRight(360);
		execute();
		int aheadVal = 100;
		if (dangerZone == null) {
			turnRight(90*Math.sin(latestDistance));
		} else {
			if (Math.abs(getX() - dangerZone.x) < 200 && Math.abs(getY() - dangerZone.y) < 200) {
				turnRight(dangerZone.getBearing(getX(), getY(), getHeading()) + 180);
				aheadVal = aheadVal + 100;
			} else {
				turnRight(90*Math.sin(latestDistance));
			}
		}
		ahead(aheadVal);
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
		setFire(Math.min(400/l.d,3));
		
		double t;
		t = (l.d / (20 - 3*(Math.min(400/l.d, 3))));
		
		double ss;
		
		ss = t*l.d;
		
		double theta = Math.atan(ss/l.d);
		turnGunRight(getHeading() - getGunHeading() + bearing);
		
		
		execute();
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		//setTurnRadarRight(getHeading() - getRadarHeading() + e.getBearing());
		//scanDirection *= -1;
		//setTurnRadarRight(360*scanDirection);
		//execute();
		enemyLocations.put(e.getName(), new Location(getRadarHeading()/360 * 2*Math.PI, getX(), getY(), e.getDistance(),e.getHeading(),e.getVelocity()));
		dangerZone = getDangerZone();
		System.out.println(e.getName() + " at x: " + enemyLocations.get(e.getName()).x);
		if (!enabled) {
			return;
		}
		found = true;
		//execute();
		// Replace the next line with any behavior you would like
		latestDistance = e.getDistance();
		lastBearing = e.getBearing();
		fireScanned(enemyLocations.get(e.getName()),e.getBearing());
		if (!(getOthers() > 1)) {
			if (e.getBearing() > 0) {
				turnRight(45 + 45 * Math.abs(Math.sin(e.getDistance())));
			} else if (e.getBearing() < 0) {
				turnLeft(45 + 45* Math.abs(Math.sin(e.getDistance())));
			}
			//turnRight(e.getBearing());
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		if (!enabled) {
			return;
		}
		if (!onWall) {
		// Replace the next line with any behavior you would like
		//turnRight(this.getHeading() + e.getBearing() + 45);
			turnRight(Math.random() * 45);
			ahead(Math.random()*50 + 50);
		}
		
	}
	
	public void reAlign() {
		if (!enabled) {
			return;
		}
		turnGunRight(this.getHeading() + lastBearing);
	}
	
	public void onHitEnemy(HitRobotEvent e) {
		
		turnRight(this.getHeading()+e.getBearing());
		fire(Rules.MAX_BULLET_POWER);
		back(10);
		
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		if (!enabled) {
			return;
		}
		if (!onWall) {
		onWall = true;
		// Replace the next line with any behavior you would like
		turnRight(e.getBearing() + 180 + Math.random()* 45);
		//reAlign();
		if (getOthers() > 1) {
			ahead(100);
		}
		ahead(100);
		onWall = false;
		}
	}	
	
}
