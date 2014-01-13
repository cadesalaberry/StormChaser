package backup;

/** USLocalizer.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 0
 */
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class USLocalizer_v2 {

	UltrasonicSensor sensorUS;
	RobotCtrl robotCtrl;
	String status;

	private final int US_THRESHOLD = 35;
	private final int US_BANDWIDTH = 15;
	private final int TRIGGER_BANDWIDTH = 5;
	private final int SPEED_FAST = 20;
	private final int SPEED_SLOW = 5;
	private final int US_REFRESH_SLOW = 150;
	private final int US_REFRESH_FAST = 50;

	private int iAngleA = 0, iAngleB = 0, iDeltaTheta = 0;
	private boolean slowZone = false;
	private boolean triggerZone = false;
	private boolean risingEdge = false;
	private boolean fallingEdge = false;
	private boolean angle1Set = false;
	private boolean angle2Set = false;
	private boolean inFirstSlowZone = true;
	private int distanceUS = 0;

	public USLocalizer_v2(RobotCtrl robotCtrl, SensorPort port) {
		this.robotCtrl = robotCtrl;
		this.sensorUS = new UltrasonicSensor(port);
	}

	public void start() {

		/*
		 * If the robot is looking around the trigger zone, turn it to 90
		 * degrees.
		 */
		if (slowZone)
			rotateRandom();

		/*
		 * Start rotating the robot left.
		 */
		refreshValues();
		fallingEdge = (distanceUS > US_THRESHOLD + US_BANDWIDTH);
		risingEdge = (!slowZone && !fallingEdge);

		/*
		 * Stay in the loop until localization is done.
		 */
		while (!angle2Set) {

			iAngleA = 0;
			iAngleB = 0;
			/*
			 * rotate the robot until it is looking around the trigger zone.
			 */
			while (!slowZone) {

				refreshValues();
				setRotationSpeed(SPEED_FAST);

				/*
				 * Refresh speed of the US Sensor in the Fast Zone.
				 */
				try {
					Thread.sleep(US_REFRESH_SLOW);
				} catch (Exception e) {
				}
			}

			/*
			 * Slow down because it it around the trigger value.
			 */
			setRotationSpeed(SPEED_SLOW);

			while (slowZone) {

				refreshValues();

				/*
				 * Set the first Angle if not set
				 */
				if (triggerZone && !angle1Set) {

					Sound.beep();
					iAngleA = getCurrentAngle();
					if (fallingEdge)
						setRotationSpeed(-SPEED_SLOW);

				}
				/*
				 * Set the second Angle if the first one have been set, and it
				 * is not looking at the first slow zone anymore.
				 */
				else if (triggerZone && angle1Set && !inFirstSlowZone) {

					Sound.beep();
					iAngleB = getCurrentAngle();
				}

				/*
				 * Refresh speed of the US Sensor in the Slow Zone.
				 */
				try {
					Thread.sleep(US_REFRESH_FAST);
				} catch (Exception e) {
				}
			}
		}
		correctAngle();

		/*
		 * This is the explaination of the previous loop. if (getFilteredData()
		 * < US_THRESHOLD - US_BANDWIDTH) { // rotate until US == 37 // slow
		 * down rotation // wait for US == 42 // record angle // speed up when
		 * US > 47 // slow down when US < 47 // wait for US == 42 // record
		 * angle // correct angle // face zero }
		 * 
		 * else if (getFilteredData() > US_THRESHOLD + US_BANDWIDTH) { // rotate
		 * until US == 47 // slow down rotation // wait for US == 42 // record
		 * angle // change rotation // speed up when US > 47 // slow down when
		 * US < 47 // wait for US == 42 // record angle // correct angle // face
		 * zero }
		 */
	}

	private void rotateRandom() {
		robotCtrl.setSpeeds(0, SPEED_FAST);
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
	}

	private void setRotationSpeed(int speed) {
		robotCtrl.setSpeeds(0, speed);
	}

	private int getCurrentAngle() {
		return 0;
	}

	private void correctAngle() {
		/*
		 * Compute angle to turn'
		 */
		iDeltaTheta = (iAngleA + iAngleB) / 2;

		/*
		 * Turn robot to face 45 degrees.
		 */
		robotCtrl.turn(iDeltaTheta);

		// odo.setPosition(new double[] { 0.0, 0.0, 45.0 }, new boolean[] {
		// true,true, true });
	}

	private void refreshValues() {

		this.distanceUS = getUSDistance();

		slowZone = (distanceUS > US_THRESHOLD - US_BANDWIDTH && distanceUS < US_THRESHOLD
				+ US_BANDWIDTH);

		triggerZone = (distanceUS < US_THRESHOLD + TRIGGER_BANDWIDTH && distanceUS > US_THRESHOLD
				- TRIGGER_BANDWIDTH);
		angle1Set = (iAngleA != 0);
		angle2Set = (iAngleB != 0);
		if (angle1Set && !slowZone)
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
		return sensorUS.getDistance();
	}

	public String reportStatus(boolean getName) {

		if (getName) {
			/*
			 * status = new String( "Current Time"+ "\tDelay"+ "\tPower"+
			 * "\tSpeed"+ "\tLeftTacho"+ "\tRightTacho" ); } else { status = new
			 * String( ""+currentDelay+ "\t"+launcherDelay+ "\t"+launcherPower+
			 * "\t"+launcherSpeed+ "\t"+Motor.A.getTachoCount()+
			 * "\t"+Motor.B.getTachoCount() );
			 */

			status = new String("distanceUS");
			status += "\tiAngleA";
			status += "\tiAngleB";
			status += "\tiDeltaTheta";
			status += "\tslowZone";
			status += "\trisingEdge";
			status += "\tfallingEdge";
			status += "\tangle1Set";
			status += "\tangle2Set";
			status += "\tinFirstSlowZone";

		} else {
			status = new String("" + distanceUS);
			status += "\t" + iAngleA;
			status += "\t" + iAngleB;
			status += "\t" + iDeltaTheta;
			status += "\t";
			if (slowZone)
				status += "" + slowZone;
			status += "\t";
			if (risingEdge)
				status += "" + risingEdge;
			status += "\t";
			if (fallingEdge)
				status += "" + fallingEdge;
			status += "\t";
			if (angle1Set)
				status += "" + angle1Set;
			status += "\t";
			if (angle2Set)
				status += "" + angle2Set;
			status += "\t";
			if (inFirstSlowZone)
				status += "" + inFirstSlowZone;
		}
		return status;
	}
}
