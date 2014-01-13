/** USLocalizer.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 7
 */

import bluetooth.StartCorner;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class USLocalizer implements BluetoothReporter {

	UltrasonicSensor sensorUS;
	RobotCtrl robotCtrl;
	Odometer odometer;
	String status;

	private final int US_THRESHOLD = 40; // old:32
	private final int US_BANDWIDTH_HIGH = 40;
	private final int US_BANDWIDTH_LOW = 8;

	private final double US_THRESHOLD_MIN = US_THRESHOLD - US_BANDWIDTH_LOW;
	private final double US_THRESHOLD_MAX = US_THRESHOLD + US_BANDWIDTH_HIGH;

	private final int TRIGGER_BANDWIDTH = 5;

	private final int SPEED_FAST = 50;
	private final int SPEED_SLOW = 30;

	private final int US_REFRESH_SLOW = 80;
	private final int US_REFRESH_FAST = 10;

	private int iAngleA = 0, iAngleB = 0, iDeltaTheta = 0;

	private boolean slowZone = false;
	private boolean triggerZone = false;
	private boolean fallingEdge = false;
	private boolean inFirstSlowZone = true;

	private int distanceUS = 0;
	private int distance = 0;
	private int filterControl = 0;
	private int corner;

	public USLocalizer(RobotCtrl robotCtrl, SensorPort port) {
		this.robotCtrl = robotCtrl;
		this.odometer = robotCtrl.getOdometer();
		this.sensorUS = new UltrasonicSensor(port);
	}
	
	/**
	 * We had some trouble debugging the USLocalizer at some points. We just decided to write another version.
	 */
	public void startSimple() {

		robotCtrl.setRobotSpeed(SPEED_FAST);
		robotCtrl.turnLeft(SPEED_SLOW);
		/*
		 * Turn until it reaches a high distance, meaning it is facing void.
		 */
		while (true) {
			while (getUSDistanceFiltered() < 40) { // 200!!!!!!!!!!!!!
				Sound.beep();
				try {
					Thread.sleep(US_REFRESH_SLOW);
				} catch (Exception e) {
				}
			}
		}

		/*
		 * Turn until it is close to the threshold value.
		 */
		/*
		 * while (getUSDistanceFiltered() > US_THRESHOLD_MAX) { try {
		 * Thread.sleep(US_REFRESH_SLOW); } catch (Exception e) { } } /* Turn
		 * until the threshold value is reached. Record the angle, and then
		 * change the direction of rotation.
		 * 
		 * while (getUSDistanceFiltered() > US_THRESHOLD_MIN) {//
		 * NIGEL!!!!!!!!!!!!!!!!!!!! // was MinUsThres try {
		 * Thread.sleep(US_REFRESH_FAST); } catch (Exception e) { } //
		 * refreshValues(); }
		 * 
		 * Sound.beep(); iAngleA = getCurrentAngle(); LCD.drawInt(iAngleA, 0,
		 * 5);
		 * 
		 * robotCtrl.turnRight(SPEED_SLOW); getUSDistanceFiltered(); try {
		 * Thread.sleep(500); } catch (Exception e) { } getUSDistanceFiltered();
		 * try { Thread.sleep(500); } catch (Exception e) { }
		 * 
		 * /* Turn until it is close to the threshold value.
		 * 
		 * while (getUSDistanceFiltered() > US_THRESHOLD_MAX) { try {
		 * Thread.sleep(US_REFRESH_SLOW); } catch (Exception e) { } }
		 * 
		 * LCD.drawInt(getUSDistanceFiltered(), 6, 5); // // Modification Nigel
		 * // try { // Thread.sleep(2000); // } catch (Exception e) {}
		 * 
		 * while (getUSDistanceFiltered() > US_THRESHOLD_MIN) { try {
		 * Thread.sleep(US_REFRESH_FAST); } catch (Exception e) { } // NIGEL WAS
		 * MAX US THRES and < // refreshValues(); }
		 * 
		 * 
		 * Sound.beep(); iAngleB = getCurrentAngle(); LCD.drawInt(iAngleB, 0,
		 * 6); robotCtrl.stop(); robotCtrl.setRobotSpeed(SPEED_FAST);
		 * 
		 * correctAngle();
		 * 
		 * goToRightPosition();
		 */

	}

	/**
	 * Localize with the UltrasonicSensor. Storm Chaser turns left until it sees 200,
	 * continue until it sees the wall, record the angle, change direction of rotation,
	 * continue rotating until there is a wall, record the second angle,
	 * then compute to find how to to correct the angle.
	 * 
	 */
	public void startSimple2() {

		robotCtrl.setRobotSpeed(SPEED_FAST);
		robotCtrl.turnLeft(SPEED_FAST);
		/*
		 * Turn until it reaches a high distance, meaning it is facing void.
		 */
		while (getUSDistanceFiltered() < 200) {
			try {
				Thread.sleep(US_REFRESH_SLOW);
			} catch (Exception e) {
			}
		}

		/*
		 * Turn until it is close to the threshold value.
		 */
		while (getUSDistanceFiltered() > US_THRESHOLD_MAX) {
			try {
				Thread.sleep(US_REFRESH_FAST);
			} catch (Exception e) {
			}
		}
		robotCtrl.turnLeft(SPEED_SLOW);
		/*
		 * Turn until the threshold value is reached. Record the angle, and then
		 * change the direction of rotation.
		 */
		while (getUSDistanceFiltered() > US_THRESHOLD_MIN) {// NIGEL!!!!!!!!!!!!!!!!!!!!
			// was MinUsThres
			try {
				Thread.sleep(US_REFRESH_FAST);
			} catch (Exception e) {
			}
			// refreshValues();
		}

		Sound.beep();
		iAngleA = getCurrentAngle();
		LCD.drawInt(iAngleA, 0, 5);

		robotCtrl.turnRight(SPEED_FAST);

		getUSDistanceFiltered();
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		getUSDistanceFiltered();
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}

		/*
		 * Turn until it is close to the threshold value.
		 */
		while (getUSDistanceFiltered() > US_THRESHOLD_MAX) {
			try {
				Thread.sleep(US_REFRESH_FAST);
			} catch (Exception e) {
			}
		}

		robotCtrl.turnRight(SPEED_SLOW);

		LCD.drawInt(getUSDistanceFiltered(), 6, 5);
		// // Modification Nigel
		// try {
		// Thread.sleep(2000);
		// } catch (Exception e) {}

		while (getUSDistanceFiltered() > US_THRESHOLD_MIN) {
			try {
				Thread.sleep(US_REFRESH_FAST);
			} catch (Exception e) {
			}
			// NIGEL WAS MAX US THRES and <
			// refreshValues();
		}

		Sound.beep();
		iAngleB = getCurrentAngle();
		LCD.drawInt(iAngleB, 0, 6);
		robotCtrl.stop();

		robotCtrl.setRobotSpeed(SPEED_FAST);

		correctAngle();

		goToRightPosition();
	}

	/**
	 * First tentative for the code. using the while loops led to nothing, I just change  the method.
	 */
	@Deprecated
	public void start() {

		int slow = SPEED_SLOW;
		int fast = SPEED_FAST;

		/*
		 * If the robot is looking around the trigger zone, turn it to 90
		 * degrees.
		 */
		refreshValues();
		if (slowZone || !fallingEdge) {
			robotCtrl.setSpeeds(0, 30);
			while (distanceUS < 255) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				refreshValues();
			}
		}

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

		goToRightPosition();

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

	private void setRotationSpeed(int speed) {
		robotCtrl.setSpeeds(0, speed);
	}

	private int getCurrentAngle() {
		return (int) odometer.getHeading();
	}
	
	/**
	 * Do the math to correct the heading of StormChaser from the two angles it got.
	 */
	private void correctAngle() {
		/*
		 * Compute angle to turn'
		 */
		iDeltaTheta = (iAngleA + iAngleB) / 2;

		/*
		 * Turn robot to face 45 degrees.
		 */
		robotCtrl.turn2(iDeltaTheta%360);

		boolean[] temp = new boolean[] { true, true, true };
		double x;
		double y; 
		double theta;
		if ( Chaser.CORNER == StartCorner.BOTTOM_LEFT){
			x=-10;
			y=-10;
			theta=45;
		}
		else if (Chaser.CORNER == StartCorner.BOTTOM_RIGHT) {
			x = 310.0;
			y=-10;
			theta = 135.0;
			Sound.beep();
		} else if (Chaser.CORNER==StartCorner.TOP_LEFT) {
			y = 310.0;
			x=-10;
			theta = 315.0;
		} else {
			theta = 225.0;
			x = 310.0;
			y = 310.0;
		}
		

		odometer.setPosition(new double[] { x, y, theta }, temp);

	}

	private void goToRightPosition() {
		robotCtrl.goForward(36);

	}
	
	/**
	 * Used in start() to actualize the status of the robot.
	 */
	@Deprecated
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
	
	/**
	 * Gets the unfiltered distance from the ultrasonic sensor.
	 */
	
	public int getUSDistanceFiltered() {

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
			// Modified
			filterControl = 0;
		} else {
			// distance went below 200, therefore reset everything.
			filterControl = 0;
			this.distanceUS = distance;
		}
		// Modification Milena
		// sensorUS.off();
		return this.distanceUS;

	}

	/**
	 * Gets the unfiltered distance from the ultrasonic sensor.
	 */
	private int getUSDistance() {

		sensorUS.ping();
		distanceUS = sensorUS.getDistance();
		return distanceUS;
	}

	/**
	 * Returns a String representing the status of this class. (Structured as a
	 * table).
	 * 
	 */
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