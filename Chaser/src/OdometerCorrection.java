/***********************
author : Nigel Kut
date : 30.03.12
 ************************/

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.Sound;
import lejos.nxt.NXTRegulatedMotor;

public class OdometerCorrection implements TimerListener {

	private Odometer odometer;
	private LineSensor lineSensorL;
	private LineSensor lineSensorR;
	private Timer odoCorrectionTimer;
	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;

	// distance from light sensor to center of rotation of robot
	private static final double lightToCenter = Chaser.LIGHT_TO_CENTER;
	// distance between two light sensors
	private static final double lightLToLightR = Chaser.LIGHT_L_TO_LIGHT_R;

	// angle (in radians) between 'lightToCenter' and 'lightLToLightR'
	private static final double angle = Chaser.ANGLE;
	private static final double leftRadius = Chaser.LEFT_RADIUS;
	private static final double rightRadius = Chaser.RIGHT_RADIUS;
	// allowable error in odometer theta
	private static final int thetaBand = 20;
	// refresh time
	private static final int DEFAULT_REFRESH = 50;

	private double tachoLeft1, tachoLeft2, tachoLeftDelta;
	private double tachoRight1, tachoRight2, tachoRightDelta;
	private int lineNum;
	private double time1, time2;
	private boolean lineReadL = false;
	private boolean lineReadR = false;
	private double thetaError, thetaError2, odoTheta = 0;
	private double odoX, odoY;
	private double absDistance, generalHeading, distanceError, distanceErrorL,
			distanceErrorR;
	private boolean lineRead;
	private double[] odoReadings = new double[3];
	private boolean[] conditions = { true, true, true };

	// constructors
	public OdometerCorrection(NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, Odometer odometer,
			LineSensor lineSensorL, LineSensor lineSensorR,
			int odoCorrectionRefresh) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.lineSensorL = lineSensorL;
		this.lineSensorR = lineSensorR;
		this.odoCorrectionTimer = new Timer(odoCorrectionRefresh, this);
	}

	public OdometerCorrection(NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, Odometer odometer,
			LineSensor lineSensorL, LineSensor lineSensorR) {
		this(leftMotor, rightMotor, odometer, lineSensorL, lineSensorR,
				DEFAULT_REFRESH);
	}

	public void start() {

		//startLight();

		odoCorrectionTimer.start();
	}

	public void startLight() {
		
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
	}

	public void timedOut() {

		
		
		correct();
		
		//commented out to let it always on.
		/*if (!Chaser.isTurning && Chaser.isNavigating) {
			startLight();
			correct();
		} else {
			stopLight();

		}*/

	}

	public void correct() {

		if (lineSensorL.checkForLine()) {
			tachoLeft1 = leftMotor.getTachoCount();
			tachoRight1 = rightMotor.getTachoCount();
			time1 = System.currentTimeMillis();
			lineReadL = true;
		}
		if (lineSensorR.checkForLine()) {
			tachoLeft2 = leftMotor.getTachoCount();
			tachoRight2 = rightMotor.getTachoCount();
			time2 = System.currentTimeMillis();
			lineReadR = true;
		}

		// do correction only when both light sensors have read a line
		if (lineReadL && lineReadR) {
			Sound.buzz();
			lineReadL = false;
			lineReadR = false;
			odometer.getPosition(odoReadings, conditions);
			odoX = odoReadings[0];
			odoY = odoReadings[1];
			odoTheta = odoReadings[2];
			tachoLeftDelta = Math.abs(tachoLeft2 - tachoLeft1);
			tachoRightDelta = Math.abs(tachoRight2 - tachoRight1);
			lineRead = true;

			if (Math.abs(tachoLeftDelta - tachoRightDelta) > 180) {
				lineRead = false;
			}

			if (lineRead) {
				correctHeading();

				absDistance = (1.6) * lightToCenter
						* Math.sin(thetaError + angle);
				generalHeading = getHeading();
				// LCD.drawString(Odometer.formattedDoubleToString(absDistance,
				// 2), 0, 5);
				if (getHeading() == 0 || getHeading() == 180)
					correctX();
				else
					correctY();
				lineRead = false;

			}
		}
	}

	// Heading Correction!!
	public void correctHeading() {

		distanceErrorL = getDistanceFromTacho(tachoLeftDelta, leftRadius);
		distanceErrorR = getDistanceFromTacho(tachoRightDelta, rightRadius);

		distanceError = (distanceErrorL + distanceErrorR) / 2.0;

		thetaError = Math.atan((distanceError / lightLToLightR));

		// if time1 < time2, set the sign to negative.
		int sign = time1 < time2 ? -1 : 1;

		thetaError2 = sign * Math.toDegrees(thetaError);
		// LCD.drawString(Odometer.formattedDoubleToString(thetaError2, 2), 0,
		// 4);

		odometer.setTheta(getHeading() + thetaError2);
	}

	// Horizontal correction (along x-axis)
	public void correctX() {

		if (generalHeading == 0) {
			lineNum = (round((odoX - lightToCenter) / 30.48));
			odometer.setX(lineNum * 30.48 + absDistance);
		}

		else if (generalHeading == 180) {
			lineNum = (round((odoX + lightToCenter) / 30.48));
			odometer.setX(lineNum * 30.48 - absDistance);
		}
	}

	// Vertical correction (along y-axis)
	public void correctY() {

		if (generalHeading == 90) {
			lineNum = (round((odoY - lightToCenter) / 30.48));
			odometer.setY(lineNum * 30.48 + absDistance);
		}

		else if (generalHeading == 270) {
			lineNum = (round((odoY + lightToCenter) / 30.48));
			odometer.setY(lineNum * 30.48 - absDistance);
		}
	}

	// return the general orientation of robot (0 || 90 || 180 || 270)
	public int getHeading() {

		if (odoTheta > (360 - thetaBand) || odoTheta < (0 + thetaBand))
			return 0;
		else if (odoTheta > (90 - thetaBand) && odoTheta < (90 + thetaBand))
			return 90;
		else if (odoTheta > (180 - thetaBand) && odoTheta < (180 + thetaBand))
			return 180;
		else
			return 270;

	}

	// return the Distance from tachoCountValue from motors
	public double getDistanceFromTacho(double tachoValue, double radius) {
		return (tachoValue / 180 * Math.PI * radius);
	}

	public int round(double number) {
		if (number > (0.5 + (int) number)) {
			return ((int) (number) + 1);
		} else
			return (int) (number);
	}
	
	/**
	 * Returns a String representing the status of this class. (Structured as a
	 * table).
	 * 
	 */
	public String reportStatus(boolean printName) {

		String status;

		if (printName) {

			status = new String("x");
			status += "\ty";
			status += "\ttheta";
			status += "\tdErrorL";
			status += "\tdErrorR";
			status += "\tthetaError2";

		} else {

			status = new String("" + (int) odometer.getX());
			status += "\t" + (int) odometer.getY();
			status += "\t" + (int) odometer.getTheta();
			status += "\t" + distanceErrorL;
			status += "\t" + distanceErrorR;
			status += "\t" + (getHeading() - thetaError2);

		}

		return status;
	}

	public void stop() {
		odoCorrectionTimer.stop();

		//stopLight();
	}

	public void stopLight() {
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		lineSensorL.stop();
		lineSensorR.stop();
	}

}