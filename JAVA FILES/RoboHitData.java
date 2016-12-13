package spacecadets2016;

public class RoboHitData {

	public double hits;
	public double misses;
	public double distanceTotal;
	
	public double getAccuracy() {
		if (hits+misses == 0) {
			return -1;
		}
		return (hits/(hits+misses) * 100);
	}
	
	public double getAverageHitDistance() {
		if (hits > 0) {
			return distanceTotal/hits;
		}
		return -1;
	}
	
}
