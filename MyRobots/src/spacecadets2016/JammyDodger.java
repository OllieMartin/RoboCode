package spacecadets2016;
import java.awt.Color;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class JammyDodger extends Robot {

	double lastBearing;
	boolean onWall;
	double latestDistance;
	
	/**
	 * run: Test's default behavior
	 */
	@Override
	public void run() {
		
		
		
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.getHSBColor(57, 18, 10),Color.red,Color.red); // body,gun,radar

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			if (getOthers() > 1) {
				//basicEvade();
				basicEvade();
			} else {
				basicSeek();
			}
		}
	}
	
	
	
	private void basicSeek() {
		//ahead(100);
		turnGunRight(360);
		ahead(latestDistance /2);
		//back(100);
		//turnGunRight(360);
	}
	
	private void basicEvade() {
		turnGunRight(90);
		turnRight(20);
		ahead(latestDistance/10);
	}
	
	

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		latestDistance = e.getDistance();
		lastBearing = e.getBearing();
		if (e.getDistance() <= 100) {
			fire(5);
		} else if (e.getDistance() <= 150) {
			fire(4);
		} else if (e.getDistance() <= 200) {
			fire(3);
			//turnRight(this.getHeading() + e.getBearing());
		} else if (e.getDistance() <= 300) {
			//turnRight(this.getHeading() + e.getBearing());
			//ahead(100);
			fire(2);
		} else {
			//turnRight(this.getHeading() + e.getBearing());
			//ahead(100);
			fire(1);
			//rapidFire(1,2);
		}
		if (!(getOthers() > 1)) {
		if (e.getBearing() > 0) {
			turnRight(45 + 45 * Math.abs(Math.sin(e.getDistance())));
		} else if (e.getBearing() < 0) {
			turnLeft(45 + 45* Math.abs(Math.sin(e.getDistance())));
		}
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		if (!onWall) {
		// Replace the next line with any behavior you would like
		//turnRight(this.getHeading() + e.getBearing() + 45);
			turnRight(Math.random() * 45);
			ahead(Math.random()*50 + 50);
		}
		
	}
	
	public void reAlign() {
		turnRight(this.getHeading() + lastBearing);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		if (!onWall) {
		onWall = true;
		// Replace the next line with any behavior you would like
		reAlign();
		if (getOthers() > 1) {
			ahead(100);
		}
		ahead(100);
		onWall = false;
		}
	}	
	
}
