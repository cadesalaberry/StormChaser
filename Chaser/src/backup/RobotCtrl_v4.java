package backup;

import lejos.nxt.NXTRegulatedMotor;

public class RobotCtrl_v4 {
	private final static double DEFAULT_LEFT_RADIUS = Chaser.LEFT_RADIUS;
	private final static double DEFAULT_RIGHT_RADIUS = Chaser.RIGHT_RADIUS;
	private final static double DEFAULT_WIDTH = Chaser.WIDTH;

	public NXTRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;

	private final int FORWARD_SPEED = 150;// 250
	private final int ROTATE_SPEED = 100;// 150
	private double xC, yC, thetaC, dist;
	private double thetaD, thetaR;
	private double leftRadius = 0;
	private double rightRadius = 0;
	private double width = 0;
	private double forwardSpeed, rotationSpeed;

	private boolean US = false;// <--- Ultrasonic sensor boolean!!

	public RobotCtrl_v4(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width, double leftRadius,
			double rightRadius) {
		this.odometer = odometer;
		this.leftMotor = odometer.getLeftMotor();
		this.rightMotor = odometer.getRightMotor();
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;

		leftMotor.setSpeed(40);
		rightMotor.setSpeed(40);
	}

	public RobotCtrl_v4(Odometer odometer) {
		this(odometer, odometer.getLeftMotor(), odometer.getRightMotor());
	}

	public RobotCtrl_v4(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor) {
		this(odometer, leftMotor, rightMotor, DEFAULT_WIDTH);
	}

	public RobotCtrl_v4(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width) {
		this(odometer, leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS,
				DEFAULT_RIGHT_RADIUS);
	}

	public boolean travelTo(double x, double y) {

		// Gets the x and y value between nxt and the target point
		xC = x - odometer.getX();
		yC = y - odometer.getY();
		// Calculate the actual distance between the nxt and the target point
		dist = (Math.sqrt((xC * xC * 100) + (yC * yC * 100))) / 10;
		// Calculates the angle that the nxt should turn, in order to face
		// the target point
		thetaD = Math.toDegrees(Math.atan2(yC, xC));
		// LCD.drawInt((int) (thetaD * 180 / Math.PI), 0, 6);
		// LCD.drawInt((int) dist, 0, 5);
		// Turns the nxt to an angle to face the destination
		turnTo(thetaD);
		if (US)
			return false;

		this.setSpeeds(FORWARD_SPEED, 0);

		// Rotates the wheels of the nxt
		leftMotor.rotate(getTachoFromDistance(this.leftRadius, dist), true);
		rightMotor.rotate(getTachoFromDistance(this.rightRadius, dist), false);
		return true;
	}

	public void turnTo(double theta) {

		// return in degrees
		thetaR = odometer.getTheta();

		if ((theta - thetaR) < -180) {
			thetaC = (theta - thetaR + 360);
		} else if ((theta - thetaR) > 360) {
			thetaC = (theta - thetaR - 360);
		} else {
			thetaC = (theta - thetaR);
		}

		turn(thetaC);
	}

	/**
	 * get the tacho the motors should rotate. in order the robot to rotate at
	 * the given angle.
	 * 
	 * @param radius
	 *            (in centimeters)
	 * @param width
	 *            (in centimeters)
	 * @param angle
	 *            (in degrees)
	 * @return tacho (in degrees)
	 */

	private static int getTachoFromAngle(double radius, double width,
			double angle) {
		return (int) ((width * angle) / (2 * radius));
	}

	/**
	 * Get the tacho the motor should rotate for the wheel to travel the given
	 * distance. It relies on the width of the wheel.
	 * 
	 * @param radius
	 *            (in centimeters)
	 * @param distance
	 *            (in centimeters)
	 * @return tacho (in degrees)
	 */

	private static int getTachoFromDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// Accessors
	public double getDisplacement() {
		return (leftMotor.getTachoCount() * leftRadius + rightMotor
				.getTachoCount() * rightRadius)
				* Math.PI / 360.0;
	}

	/**
	 * This method return the angle variation from its initial angle (in
	 * degrees).
	 * 
	 * @return angle
	 */
	public double getPreciseHeading() {
		return (leftMotor.getTachoCount() * leftRadius - rightMotor
				.getTachoCount() * rightRadius)
				/ width;
	}

	/**
	 * This method return the angle variation from its initial angle (in
	 * degrees).
	 * 
	 * @return angle (in degrees)
	 */
	public int getHeading() {
		return (int) ((leftMotor.getTachoCount() * leftRadius - rightMotor
				.getTachoCount() * rightRadius) / width);
	}

	public void getDisplacementAndHeading(double[] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();

		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI
				/ 360.0;
		data[1] = (leftTacho * leftRadius - rightTacho * rightRadius) / width;
	}

	// mutators

	public void setSpeeds(double forwardSpeed, double rotationalSpeed) {
		double leftSpeed, rightSpeed;

		this.forwardSpeed = forwardSpeed;
		this.rotationSpeed = rotationalSpeed;

		leftSpeed = (forwardSpeed + rotationalSpeed * width * Math.PI / 360.0)
				* 180.0 / (leftRadius * Math.PI);
		rightSpeed = (forwardSpeed - rotationalSpeed * width * Math.PI / 360.0)
				* 180.0 / (rightRadius * Math.PI);

		// set motor directions
		if (leftSpeed > 0.0)
			leftMotor.forward();
		else {
			leftMotor.backward();
			leftSpeed = -leftSpeed;
		}

		if (rightSpeed > 0.0)
			rightMotor.forward();
		else {
			rightMotor.backward();
			rightSpeed = -rightSpeed;
		}

		// set motor speeds
		if (leftSpeed > 900.0)
			leftMotor.setSpeed(900);
		else
			leftMotor.setSpeed((int) leftSpeed);

		if (rightSpeed > 900.0)
			rightMotor.setSpeed(900);
		else
			rightMotor.setSpeed((int) rightSpeed);
	}

	/**
	 * Make the robot turn until it reaches the specified angle.
	 * 
	 * @param angle
	 */
	public void turn(double angle) {

		this.setSpeeds(0, ROTATE_SPEED);

		// Rotates the nxt to face a particular angle
		leftMotor.rotate(-getTachoFromAngle(leftRadius, width, angle), true);
		rightMotor.rotate(getTachoFromAngle(rightRadius, width, angle), false);

	}

	public void changeDirectionOfRotation() {
		this.setSpeeds(0, -this.rotationSpeed);
	}

	public void flt() {
		leftMotor.flt(true);
		rightMotor.flt(true);
	}

	// Stops both motors
	public void stop() {
		rightMotor.stop();
		leftMotor.stop();
	}

	// Accessors
	public Odometer getOdometer() {
		return this.odometer;
	}

}
