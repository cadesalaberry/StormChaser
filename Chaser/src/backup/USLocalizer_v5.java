package backup;

/** USLocalizer.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 0
 */
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class USLocalizer_v5 {

	UltrasonicSensor sensorUS;
	RobotCtrl robotCtrl;
	Odometer odometer;
	String status;

	private final int US_THRESHOLD = 32;
	private final int US_BANDWIDTH_HIGH = 28;
	private final int US_BANDWIDTH_LOW = 12;

	private final double MIN_US_THRESHOLD = US_THRESHOLD - US_BANDWIDTH_LOW;
	private final double MAX_US_THRESHOLD = US_THRESHOLD + US_BANDWIDTH_HIGH;

	private final int TRIGGER_BANDWIDTH = 5;

	private final int SPEED_FAST = 20;
	private final int SPEED_SLOW = 10;

	private final int US_REFRESH_SLOW = 0;

	private int iAngleA = 0, iAngleB = 0, iDeltaTheta = 0;

	private boolean slowZone = false;
	private boolean triggerZone = false;
	private boolean fallingEdge = false;
	private boolean inFirstSlowZone = true;

	private int distanceUS = 0;

	public USLocalizer_v5(RobotCtrl robotCtrl, SensorPort port) {
		this.robotCtrl = robotCtrl;
		this.odometer = robotCtrl.getOdometer();
		this.sensorUS = new UltrasonicSensor(port);
	}

	public void carlos() {

		setRotationSpeed(SPEED_FAST);

		while (getUSDistance() < MAX_US_THRESHOLD)
			try {
				Thread.sleep(300);

			} catch (Exception e) {
			}

		// keep rotating until the robot sees a wall, then latch the angle
		while (getUSDistance() > MIN_US_THRESHOLD) {
			refreshValues();
		}
		;
		Sound.beep();
		iAngleA = odometer.getHeading();

		setRotationSpeed(-SPEED_FAST);
		while (getUSDistance() < MAX_US_THRESHOLD)
			refreshValues();
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}

		// keep rotating until the sensor sees a wall, then latch the angle
		while (getUSDistance() > MIN_US_THRESHOLD)
			;
		{
			refreshValues();
		}

		Sound.beep();
		iAngleB = odometer.getHeading();

		correctAngle();
	}

	public void start() {

		int slow = SPEED_SLOW;
		int fast = SPEED_FAST;

		/*
		 * If the robot is looking around the trigger zone, turn it to 90
		 * degrees.
		 */
		refreshValues();
		if (slowZone || !fallingEdge)
			rotateUntil(255);

		/*
		 * Start rotating the robot left.
		 */
		refreshValues();

		/*
		 * Stay in the loop until localization is done.
		 */
		while (iAngleB == 0) {

			iAngleA = 0;
			iAngleB = 0;

			/*
			 * rotate the robot until it is looking around the trigger zone.
			 */
			while (!slowZone && iAngleA == 0) {

				setRotationSpeed(fast);
				refreshValues();

				/*
				 * Refresh speed of the US Sensor in the Fast Zone.
				 */
				try {
					Thread.sleep(US_REFRESH_SLOW);
				} catch (Exception e) {
				}
			}

			while (slowZone) {

				/*
				 * Slow down because it it around the trigger value.
				 */
				setRotationSpeed(slow);

				refreshValues();

				/*
				 * Set the first Angle if not set
				 */
				if (triggerZone && iAngleA == 0 && fallingEdge
						&& inFirstSlowZone) {

					Sound.beep();
					iAngleA = getCurrentAngle();
					setRotationSpeed(-slow);

				}
				/*
				 * Set the second Angle if the first one have been set, and it
				 * is not looking at the first slow zone anymore.
				 */
				else if (triggerZone && iAngleA != 0 && !inFirstSlowZone) {

					Sound.beep();
					iAngleB = getCurrentAngle();
				}
			}
		}
		correctAngle();

		/*
		 * This is the explaination of the previous loop. if (getUSDistance() <
		 * US_THRESHOLD - US_BANDWIDTH) { // rotate until US == 37 // slow down
		 * rotation // wait for US == 42 // record angle // speed up when US >
		 * 47 // slow down when US < 47 // wait for US == 42 // record angle //
		 * correct angle // face zero }
		 * 
		 * else if (getUSDistance() > US_THRESHOLD + US_BANDWIDTH) { // rotate
		 * until US == 47 // slow down rotation // wait for US == 42 // record
		 * angle // change rotation // speed up when US > 47 // slow down when
		 * US < 47 // wait for US == 42 // record angle // correct angle // face
		 * zero }
		 */
	}

	private void rotateUntil(int degrees) {
		robotCtrl.setSpeeds(0, 30);
		while (distanceUS < degrees) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
			refreshValues();
		}

	}

	private void setRotationSpeed(int speed) {
		robotCtrl.setSpeeds(0, speed);
	}

	private int getCurrentAngle() {
		return (int) robotCtrl.getHeading();
	}

	private void correctAngle() {
		/*
		 * Compute angle to turn'
		 */
		iDeltaTheta = (iAngleA + iAngleB) / 2;

		/*
		 * Turn robot to face 45 degrees.
		 */
		robotCtrl.turnTo(iDeltaTheta);

		odometer.setPosition(new double[] { 15.0, 15.0, 45.0 }, new boolean[] {
				true, true, true });
	}

	private void refreshValues() {

		this.distanceUS = getUSDistance();

		slowZone = (distanceUS > US_THRESHOLD - US_BANDWIDTH_LOW && distanceUS < US_THRESHOLD
				+ US_BANDWIDTH_HIGH);

		triggerZone = (distanceUS < US_THRESHOLD + TRIGGER_BANDWIDTH && distanceUS > US_THRESHOLD
				- TRIGGER_BANDWIDTH);

		if (!slowZone)
			fallingEdge = (distanceUS > US_THRESHOLD + US_BANDWIDTH_HIGH);

		if (iAngleA != 0 && !slowZone)
			inFirstSlowZone = false;
	}

	private int getUSDistanceFiltered() {

		int distance = 0;
		int filterControl = 0;

		// do a ping
		sensorUS.ping();

		// there will be a delay here
		distance = sensorUS.getDistance();

		// rudimentary filter
		if (distance > 200 && filterControl < 3) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance > 200) {
			// true over 200, therefore set distance to over 200
			this.distanceUS = distance;
		} else {
			// distance went below 200, therefore reset everything.
			filterControl = 0;
			this.distanceUS = distance;
		}
		sensorUS.off();
		return this.distanceUS;

	}

	private int getUSDistance() {

		sensorUS.ping();
		distanceUS = sensorUS.getDistance();
		return distanceUS;
	}

	public String reportStatus(boolean getName) {

		if (getName) {

			status = new String("distanceUS");
			status += "\tHeading";
			status += "\tiAngleA";
			status += "\tiAngleB";
			status += "\tiDeltaTheta";
			status += "\tslowZone";
			status += "\tfallingEdge";
			status += "\tinFirstSlowZone";

		} else {

			status = new String("" + distanceUS);
			status += "\t" + robotCtrl.getHeading();
			status += "\t" + iAngleA;
			status += "\t" + iAngleB;
			status += "\t" + iDeltaTheta;
			status += "\t";
			if (slowZone)
				status += "" + slowZone;
			status += "\t";
			if (fallingEdge)
				status += "" + fallingEdge;
			status += "\t";
			if (inFirstSlowZone)
				status += "" + inFirstSlowZone;
		}
		return status;
	}
}
