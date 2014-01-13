package backup;

/***********************
 author : Nigel Kut
 date : 30.03.12
 ************************/

import lejos.util.Timer;
import lejos.util.TimerListener;

public class OdometerCorrection_v4 implements TimerListener {

	private Odometer odometer;
	private LineSensor lineSensorL;
	private LineSensor lineSensorR;
	private Timer odoCorrectionTimer;
	private RobotCtrl robotCtrl;

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
	private static final int DEFAULT_REFRESH = 100;

	private double tachoLeft1, tachoLeft2, tachoLeftDelta;
	private double tachoRight1, tachoRight2, tachoRightDelta;
	private int lineNum;
	private double time1, time2, timeDelta;
	private boolean lineReadL = false;
	private boolean lineReadR = false;
	private double thetaError, thetaError2, odoTheta = 0;
	private double odoX, odoY;
	private double absDistance, generalHeading, distanceError, distanceErrorL,
			distanceErrorR;
	private boolean lineRead;

	// constructors
	public OdometerCorrection_v4(RobotCtrl robotCtrl, LineSensor lineSensorL,
			LineSensor lineSensorR, int odoCorrectionRefresh) {
		this.robotCtrl = robotCtrl;
		this.odometer = robotCtrl.getOdometer();
		this.lineSensorL = lineSensorL;
		this.lineSensorR = lineSensorR;
		this.odoCorrectionTimer = new Timer(odoCorrectionRefresh, this);
	}

	public OdometerCorrection_v4(RobotCtrl robotCtrl, LineSensor lineSensorL,
			LineSensor lineSensorR) {
		this(robotCtrl, lineSensorL, lineSensorR, DEFAULT_REFRESH);
	}

	public void start() {
		odoCorrectionTimer.start();
	}

	public void timedOut() {

		lineSensorR.start();
		lineSensorL.start();
		if (lineSensorL.checkForLine()) {
			tachoLeft1 = robotCtrl.leftMotor.getTachoCount();
			tachoRight1 = robotCtrl.rightMotor.getTachoCount();
			time1 = System.currentTimeMillis();
			lineReadL = true;
		}
		if (lineSensorR.checkForLine()) {
			tachoLeft2 = robotCtrl.leftMotor.getTachoCount();
			tachoRight2 = robotCtrl.rightMotor.getTachoCount();
			time2 = System.currentTimeMillis();
			lineReadR = true;
		}

		// do correction only when both light sensors have read a line
		if (lineReadL && lineReadR) {

			tachoLeftDelta = Math.abs(tachoLeft2 - tachoLeft1);
			tachoRightDelta = Math.abs(tachoRight2 - tachoRight1);
			lineRead = true;
			if (Math.abs(tachoLeftDelta - tachoRightDelta) > 360) {
				lineRead = false;
			}

			if (lineRead) {
				correctHeading();

				absDistance = lightToCenter * Math.sin(thetaError + angle);
				generalHeading = getHeading();

				if (getHeading() == 0 || getHeading() == 180)
					correctX();
				else
					correctY();
				lineReadL = false;
				lineReadR = false;
			}
		}

	}

	// Heading Correction!!
	public void correctHeading() {

		tachoLeftDelta = Math.abs(tachoLeft2 - tachoLeft1);
		tachoRightDelta = Math.abs(tachoRight2 - tachoRight1);

		distanceErrorL = getDistanceFromTacho(tachoLeftDelta, leftRadius);
		distanceErrorR = getDistanceFromTacho(tachoRightDelta, rightRadius);
		distanceError = (distanceErrorL + distanceErrorR) / 2;
		thetaError = Math
				.toDegrees(Math.atan((distanceError / lightLToLightR)));

		timeDelta = time1 - time2;
		thetaError2 = timeDelta / Math.abs(timeDelta) * thetaError;
		odometer.setTheta(getHeading() - thetaError2);
	}

	// Horizontal correction (along x-axis)
	public void correctX() {

		odoX = odometer.getX();

		if (generalHeading == 0) {
			lineNum = (int) (Math.abs((odoX - lightToCenter) / 30.48));
			odometer.setX(lineNum * 30.48 + absDistance);
		}

		else if (generalHeading == 180) {
			lineNum = (int) (Math.abs((odoX + lightToCenter) / 30.48));
			odometer.setX(lineNum * 30.48 - absDistance);
		}
	}

	// Vertical correction (along y-axis)
	public void correctY() {

		odoY = odometer.getY();

		if (generalHeading == 90) {
			lineNum = (int) (Math.abs((odoY - lightToCenter) / 30.48));
			odometer.setY(lineNum * 30.48 + absDistance);
		}

		else if (generalHeading == 270) {
			lineNum = (int) (Math.abs((odoY + lightToCenter) / 30.48));
			odometer.setY(lineNum * 30.48 - absDistance);
		}
	}

	// return the general orientation of robot (0 || 90 || 180 || 270)
	public int getHeading() {

		odoTheta = odometer.getTheta();

		if (odoTheta > (360 - thetaBand) && odoTheta < (0 + thetaBand))
			return 0;
		else if (odoTheta > (90 - thetaBand) && odoTheta < (90 + thetaBand))
			return 90;
		else if (odoTheta > (180 - thetaBand) && odoTheta < (180 - thetaBand))
			return 180;
		else
			return 270;

	}

	// return the Distance from tachoCountValue from motors
	public double getDistanceFromTacho(double tachoValue, double radius) {
		return (tachoValue / 180 * Math.PI * radius);
	}

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
			status += "\t" + (getHeading() + thetaError2);

		}

		return status;
	}

	public void stop() {
		lineSensorL.stop();
		lineSensorR.stop();
		odoCorrectionTimer.stop();
	}

}