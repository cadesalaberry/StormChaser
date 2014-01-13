package backup;
/** USLocalizer.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 7
 */

import BluetoothReporter;
import Odometer;
import RobotCtrl;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class USLocalizer_v8 implements BluetoothReporter{

	UltrasonicSensor sensorUS;
	RobotCtrl robotCtrl;
	Odometer odometer;
	String status;

	private final int US_THRESHOLD = 32;
	private final int US_BANDWIDTH_HIGH = 10;
	private final int US_BANDWIDTH_LOW = 8;

	private final double MIN_US_THRESHOLD = US_THRESHOLD - US_BANDWIDTH_LOW;
	private final double MAX_US_THRESHOLD = US_THRESHOLD + US_BANDWIDTH_HIGH;

	private final int TRIGGER_BANDWIDTH = 5;

	private final int SPEED_FAST = 60;
	private final int SPEED_SLOW = 20;

	private final int US_REFRESH_SLOW = 0;

	private int iAngleA = 0, iAngleB = 0, iDeltaTheta = 0;

	private boolean slowZone = false;
	private boolean triggerZone = false;
	private boolean fallingEdge = false;
	private boolean inFirstSlowZone = true;

	private int distanceUS = 0;
	private int distance = 0;
	private int filterControl = 0;
	private int corner;

	public USLocalizer_v8(RobotCtrl robotCtrl, SensorPort port, int corner) {
		this.robotCtrl = robotCtrl;
		this.odometer = robotCtrl.getOdometer();
		this.sensorUS = new UltrasonicSensor(port);
		this.corner = corner;
	}

	public void startSimple() {

		setRotationSpeed(SPEED_FAST);
		while (getUSDistanceFiltered() < MAX_US_THRESHOLD) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}

		//setRotationSpeed(-SPEED_SLOW);
		robotCtrl.changeDirectionOfRotation();

		// keep rotating until the robot sees a wall, then latch the angle
		while (getUSDistanceFiltered() > US_THRESHOLD) {// NIGEL!!!!!!!!!!!!!!!!!!!! was MinUsThres
			refreshValues();
		}

		Sound.beep();
		iAngleA = getCurrentAngle();

		robotCtrl.changeDirectionOfRotation();
		//Modification Nigel
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}

		while (getUSDistanceFiltered() > US_THRESHOLD){ 
			Sound.buzz();//NIGEL WAS MAX US THRES and <
			refreshValues();
		}
			
		

		// keep rotating until the sensor sees a wall, then latch the angle
		/*while (getUSDistanceFiltered() > MIN_US_THRESHOLD) 
		{
			refreshValues();
		}*/

		Sound.beep();
		iAngleB = getCurrentAngle();
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		
		robotCtrl.changeDirectionOfRotation();

		correctAngle();
		robotCtrl.goForward(28);
		robotCtrl.turn(90);
		robotCtrl.goForward(28);
		robotCtrl.travelTo(0, 0);
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
		return (int) odometer.getTheta();
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
		boolean[] temp = new boolean[] { true, true, true };
		double x = -10;
		double y =-10;
		double theta = 45.0;
		if(corner == 2)
		{
			x = 310.0;
			theta = 135.0;
		}
		else if(corner == 3)
		{
			y = 310.0;
			theta = 225.0;
		}
		else if(corner == 4)
		{
			theta = 315.0;
			x = 310.0;
			y = 310.0;
		}
		
		odometer.setPosition(new double[] { x, y,theta},
				temp);
		
		robotCtrl.turn(0);
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

	public int getUSDistanceFiltered() {

		// do a ping
		sensorUS.ping();

		// there will be a delay here
		distance = sensorUS.getDistance();

		// rudimentary filter
		if (distance > 220 && filterControl < 3) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance > 220) {
			// true over 200, therefore set distance to over 200
			this.distanceUS = distance;
			//Modified
			filterControl = 0;
		} else {
			// distance went below 200, therefore reset everything.
			filterControl = 0;
			this.distanceUS = distance;
		}
		//Modification Milena
		//sensorUS.off();
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

