package backup;

/***********************
 author : Nigel Kut
 date : 30.03.12
 ************************/

import lejos.util.Timer;
import lejos.util.TimerListener;

public class OdometerCorrection_v1 implements TimerListener {

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
	private static final int thetaBand = 15;
	// refresh time
	private static final int DEFAULT_REFRESH = 100;

	private int tachoLeft1, tachoLeft2, tachoLeftDelta;
	private int tachoRight1, tachoRight2, tachoRightDelta;
	private int lineNum;
	private double time1, time2, timeDelta;
	private boolean lineReadL = false;
	private boolean lineReadR = false;
	private double thetaError, odoTheta = 0;
	private double odoX, odoY;
	private double absDistance, generalHeading, distanceError, distanceErrorL,
			distanceErrorR;

	// constructors
	public OdometerCorrection_v1(RobotCtrl robotCtrl, LineSensor lineSensorL,
			LineSensor lineSensorR, int odoCorrectionRefresh) {
		this.robotCtrl = robotCtrl;
		this.odometer = robotCtrl.getOdometer();
		this.lineSensorL = lineSensorL;
		this.lineSensorR = lineSensorR;
		this.odoCorrectionTimer = new Timer(odoCorrectionRefresh, this);
	}

	public OdometerCorrection_v1(RobotCtrl robotCtrl, LineSensor lineSensorL,
			LineSensor lineSensorR) {
		this(robotCtrl, lineSensorL, lineSensorR, DEFAULT_REFRESH);
	}

	public void start() {
		odoCorrectionTimer.start();
	}

	public void stop() {
		lineSensorL.stop();
		lineSensorR.stop();
		odoCorrectionTimer.stop();
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

			correctHeading();

			absDistance = lightToCenter * Math.sin(thetaError + angle);
			generalHeading = getHeading();

			if (getHeading() == 0 || getHeading() == 180)
				correctX();
			else
				correctY();
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
		odometer.setTheta(getHeading() + (timeDelta / Math.abs(timeDelta))
				* thetaError);
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
	public double getDistanceFromTacho(int tachoValue, double radius) {
		return (tachoValue / 180 * Math.PI * radius);
	}

	public String reportStatus(boolean getName) {

		String status;
		if (getName) {

			status = new String("x");
			status += "\ty";
			status += "\ttheta";
			status += "\ttacho LeftDelta";
			status += "\ttacho RightDelta";
			status += "\tabsolute distance";

		} else {

			status = new String("" + odometer.getX());
			status += "\t" + odometer.getY();
			status += "\t" + odometer.getTheta();
			status += "\t" + tachoLeftDelta;
			status += "\t" + tachoRightDelta;
			status += "\t + absDistance";
			status += "\t";

		}
		return status;
	}

}