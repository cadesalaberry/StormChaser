/** USLocalizer.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 */

import bluetooth.StartCorner;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;

public class LightLocalizer implements BluetoothReporter, LineListener {

	RobotCtrl robotCtrl;
	Odometer odometer;
	OdometerCorrection odoCorrect;
	LineSensor lineSensor;
	String status;
	final double THRESHOLD = 45;

	// distance between center of rotation and the light
	private final double DISTANCE_LIGHT_TO_ROTATION_CENTER = 10.0;

	// Corrected position to pass
	double[] pos = new double[3];
	private int corner;

	private double[] result = new double[4];
	private double[] angles = { -1, -1, -1, -1 };

	public LightLocalizer(RobotCtrl robotCtrl, LineSensor lineSensor,OdometerCorrection odoCorrect) {
		this.robotCtrl = robotCtrl;
		this.lineSensor = lineSensor;
		this.odometer = robotCtrl.getOdometer();
		this.odoCorrect = odoCorrect;
	}

	public void start() {

		lineSensor.start();
		robotCtrl.turnLeft(50);
		boolean has4lines = false;

		// double startAngle = odometer.getHeading();

		while (!has4lines) {

			if (lineSensor.checkForLine()) {

				int line = -1;

				// Confirm That a line has been seen

				Sound.beep();

				double angleToCompute = odometer.getTheta();
				// angles[lineCounter] = odometer.getHeading();

				if (Math.abs(angleToCompute) < THRESHOLD) {
					line = 3;
				} else if (Math.abs(angleToCompute - 90) < THRESHOLD) {
					line = 0;
				} else if (Math.abs(angleToCompute - 180) < THRESHOLD) {
					line = 1;
				} else if (Math.abs(angleToCompute - 270) < THRESHOLD) {
					line = 2;
				}

				if (line != -1)
					angles[line] = angleToCompute;
				line = -1;

				/*
				 * See if the array is full
				 */

				has4lines = (angles[0] != -1) && (angles[1] != -1)
						&& (angles[2] != -1) && (angles[3] != -1);

				LCD.drawString("" + angles[0], 0, 4);
				LCD.drawString("" + angles[1], 0, 5);
				LCD.drawString("" + angles[2], 0, 6);
				LCD.drawString("" + angles[3], 0, 7);

				try {
					Thread.sleep(300);
				} catch (Exception E) {
				}

			}

			try {
				Thread.sleep(50);
			} catch (Exception E) {
			}
		}

		robotCtrl.stop();
		correctPosition(angles);

	}
	public void start2(){
		
		Sound.beep();
		robotCtrl.setRobotSpeed(100);
		/*if(Chaser.CORNER == StartCorner.BOTTOM_LEFT){
			robotCtrl.travelTo(15,-10);
			robotCtrl.travelTo(-15, -10);
			robotCtrl.travelTo(15,-10);
			robotCtrl.travelTo(15, 15);
			robotCtrl.travelTo(15,-15);
			robotCtrl.travelTo(15,15);
		}
		
		else if (Chaser.CORNER == StartCorner.BOTTOM_RIGHT){
			robotCtrl.travelTo(285,-10);
			robotCtrl.travelTo(315, -10);
			robotCtrl.travelTo(285,-10);
			robotCtrl.travelTo(285, 15);
			robotCtrl.travelTo(285,-15);
			robotCtrl.travelTo(285,15);
		}
		else if (Chaser.CORNER == StartCorner.TOP_RIGHT){
			robotCtrl.travelTo(285,310);
			robotCtrl.travelTo(315, 310);
			robotCtrl.travelTo(285,310);
			robotCtrl.travelTo(285, 285);
			robotCtrl.travelTo(285,315);
			robotCtrl.travelTo(285,285);
		}
		else if(Chaser.CORNER == StartCorner.TOP_LEFT){
			robotCtrl.travelTo(15,310);
			robotCtrl.travelTo(-15, 310);
			robotCtrl.travelTo(15,310);
			robotCtrl.travelTo(15, 285);
			robotCtrl.travelTo(15,315);
			robotCtrl.travelTo(15,285);
		}*/
		robotCtrl.turn(-45);
		odoCorrect.start();
		robotCtrl.goForward(20);
		robotCtrl.goBackward(20);
		robotCtrl.goForward(20);
		robotCtrl.goBackward(20);
		robotCtrl.turn(90);
		robotCtrl.goForward(20);
		robotCtrl.goBackward(20);
		robotCtrl.goForward(20);
		robotCtrl.goBackward(20);
	
		
		
	}

	@Deprecated
	private boolean checkConsistencyInAngles(double[] inputAngles) {

		int error = 15;
		double thetaX = inputAngles[3] - inputAngles[1];
		double thetaY = inputAngles[2] - inputAngles[0];
		return 180 - error < thetaX && thetaX < 180 + error
				&& 180 - error < thetaY && thetaY < 180 + error;
	}

	public void correctPosition(double[] angles) {

		/*
		 * Assumes that the robot is turning counter clockwise.
		 */

		double thetaY = (angles[2] - angles[0]) % 360;
		double thetaX = (angles[3] - angles[1]) % 360;

		if (thetaY < 0) {
			thetaY = 360 + thetaY;
		}

		if (thetaX < 0) {
			thetaX = 360 + thetaX;
		}

		// do trig to compute (0,0) and 0 degrees

		result[0] = -DISTANCE_LIGHT_TO_ROTATION_CENTER
				* Math.cos(Math.toRadians((thetaY) / 2));
		result[1] = -DISTANCE_LIGHT_TO_ROTATION_CENTER
				* Math.cos(Math.toRadians((thetaX) / 2));
		result[2] = odometer.getTheta()
				+ (90 - (angles[3] - 180) + (angles[1] - angles[3] / 2));
		try {
			odometer.setPosition(result, new boolean[] { true, true, true });
		} catch (Exception e) {
			LCD.drawString("cannot set odo", 0, 7);
		}
	}

	public String reportStatus(boolean getName) {

		if (getName) {

			status = new String("angles[0]");
			status += "\tangles[1]";
			status += "\tangles[2]";
			status += "\tangles[3]";

		} else {

			status = new String("" + angles[0]);
			status += "\t" + angles[1];
			status += "\t" + angles[2];
			status += "\t" + angles[3];

		}
		return status;
	}

	@Override
	public void lineIsRead() {
		// TODO Auto-generated method stub

	}
}
