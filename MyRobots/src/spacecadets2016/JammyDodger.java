package spacecadets2016;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class JammyDodger extends Robot {

	double lastBearing;
	
	/**
	 * run: Test's default behavior
	 */
	@Override
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			if (getOthers() > 1) {
				//basicEvade();
				basicSeek();
			} else {
				basicSeek();
			}
		}
	}
	
	
	
	private void basicSeek() {
		//ahead(100);
		turnGunRight(360);
		ahead(100);
		//back(100);
		//turnGunRight(360);
	}
	
	private void basicEvade() {
		turnRight(90);
		ahead(1000);
		turnRight(90);
		turnGunRight(360);
		back(1000);
		turnGunRight(360);
		turnRight(45);
	}
	
	

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
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
		}
		if (e.getBearing() > 0) {
			turnRight(45);
		} else if (e.getDistance() < 0) {
			turnRight(-45);
		}
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		//turnRight(this.getHeading() + e.getBearing() + 45);
		back(100);
		turnRight(22);
		
	}
	
	public void reAlign() {
		turnRight(this.getHeading() + lastBearing);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		reAlign();
		ahead(100);
	}	
	
}
