package spacecadets2016;

public class Location {

	public double x;
	public double y;
	public double d;
	public double h;
	public double v;
	
	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Location(double radarHeadingRadians, double myX, double myY, double distance, double enemyHeading, double enemyVelocity) {
		this(radarHeadingRadians, myX, myY, distance);
		h = enemyHeading;
		v = enemyVelocity;
	}
	
	public Location(double radarHeadingRadians, double myX, double myY, double distance) {
		
		double vd;
		double hd;
		if (radarHeadingRadians >= 0 && (radarHeadingRadians <= Math.PI/2)) {
			vd = distance * Math.cos(radarHeadingRadians);
			hd = distance * Math.sin(radarHeadingRadians);
			x = myX + hd;
			y = myY + vd;
		} else if (radarHeadingRadians >= Math.PI/2 && (radarHeadingRadians <= Math.PI)) {
			vd = distance * Math.cos(Math.PI - radarHeadingRadians);
			hd = distance * Math.sin(Math.PI - radarHeadingRadians);
			x = myX + hd;
			y = myY - vd;
		} else if (radarHeadingRadians >= Math.PI && (radarHeadingRadians <= Math.PI + Math.PI/2)) {
			vd = distance * Math.cos(radarHeadingRadians - Math.PI);
			hd = distance * Math.sin(radarHeadingRadians - Math.PI);
			x = myX - hd;
			y = myY - vd;
		} else if (radarHeadingRadians >= Math.PI + Math.PI/2 && (radarHeadingRadians <= 2*Math.PI)) {
			vd = distance * Math.cos(2* Math.PI - radarHeadingRadians);
			hd = distance * Math.sin(2* Math.PI - radarHeadingRadians);
			x = myX - hd;
			y = myY + vd;
		}
		
		d = distance;
		
	}
	
	/**
	 * Gets bearing in DEGREES
	 * @param myX
	 * @param myY
	 * @param heading
	 * @return
	 */
	public double getBearing(double myX, double myY, double heading) {
		
		double hd;
		double vd;
		double bearing = 0;
		
		if (myX - x < 0 && myY - y < 0) {
			hd = x - myX;
			vd = y - myY;
			bearing = Math.atan(vd/hd)/(2*Math.PI) *360;
		} else if (myX - x < 0 && myY - y > 0) {
			hd = x - myX;
			vd = myY - y;
			bearing = (Math.atan(vd/hd) + Math.PI/2)/(2*Math.PI) *360;
		} else if (myX - x > 0 && myY - y > 0) {
			hd = myX - x;
			vd = myY - y;
			bearing = (Math.PI/2 - Math.atan(vd/hd) + Math.PI)/(2*Math.PI) *360;
		} else if (myX - x > 0 && myY - y < 0) {
			hd = myX - x;
			vd = y - myY;
			bearing = (Math.PI/2 + Math.PI + Math.atan(vd/hd))/(2*Math.PI) *360;
		}
		
		return heading - bearing;
	}
	
}
