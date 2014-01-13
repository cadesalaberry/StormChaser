package backup;

import lejos.nxt.NXTRegulatedMotor;

public class RobotCtrl_v2 {
	public final static double DEFAULT_LEFT_RADIUS = 2.67;
	public final static double DEFAULT_RIGHT_RADIUS = 2.67;
	public final static double DEFAULT_WIDTH = 16.1;

	public NXTRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;

	private final int FORWARD_SPEED = 150;// 250
	private final int ROTATE_SPEED = 100;// 150
	private double xC, yC, dist, thetaD, thetaC, thetaR;
	private double leftRadius = 0;
	private double rightRadius = 0;
	private double width = 0;

	private double forwardSpeed, rotationSpeed;

	public RobotCtrl_v2(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width, double leftRadius,
			double rightRadius) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		this.odometer = odometer;
	}

	public RobotCtrl_v2(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor) {
		this(odometer, leftMotor, rightMotor, DEFAULT_WIDTH,
				DEFAULT_LEFT_RADIUS, DEFAULT_RIGHT_RADIUS);
	}

	public RobotCtrl_v2(Odometer odometer, NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width) {
		this(odometer, leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS,
				DEFAULT_RIGHT_RADIUS);
	}

	public void travelTo(double x, double y) {

		// Gets the x and y value between nxt and the target point
		xC = x - odometer.getX();
		yC = y - odometer.getY();
		// Calculate the actual distance between the nxt and the target point
		dist = (Math.sqrt((xC * xC * 100) + (yC * yC * 100))) / 10;
		// Calculates the angle that the nxt should turn, in order to face
		// the target point
		thetaD = Math.atan2(yC, xC);
		// LCD.drawInt((int) (thetaD * 180 / Math.PI), 0, 6);
		// LCD.drawInt((int) dist, 0, 5);
		// Turns the nxt to an angle to face the destination
		turnTo(thetaD);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		// Rotates the wheels of the nxt
		leftMotor.rotate(getTachoFromDistance(this.leftRadius, dist), true);
		rightMotor.rotate(getTachoFromDistance(this.rightRadius, dist), false);
	}

	public void turnTo(double theta) {

		// return in radians.
		thetaR = odometer.getTheta();

		if ((theta - thetaR) < -Math.PI) {
			thetaC = theta - thetaR + 2 * Math.PI;
		} else if ((theta - thetaR) > Math.PI) {
			thetaC = theta - thetaR - 2 * Math.PI;
		} else {
			thetaC = theta - thetaR;
		}
		// LCD.drawInt((int) (thetaC * 180 / Math.PI), 0, 4);
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		// Rotates the nxt to face a particular angle
		leftMotor.rotate(-getTachoFromAngle(leftRadius, width, thetaC), true);
		rightMotor.rotate(getTachoFromAngle(rightRadius, width, thetaC), false);
	}

	/**
	 * get the angle the motors should rotate. in order the robot to rotate at
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
		return ((int) ((width * angle) / (2 * radius)));
	}

	// Converts distance to another distance with respect to the radius of the
	// wheel
	private static int getTachoFromDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// accessors
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
	public double getHeading() {
		return (leftMotor.getTachoCount() * leftRadius - rightMotor
				.getTachoCount() * rightRadius)
				/ width;
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

	public void rotate(int iAngle) {
		int rotationSpeed = ROTATE_SPEED;

		if (iAngle < 0)
			rotationSpeed = -ROTATE_SPEED;

		while (getHeading() < iAngle) {
			setSpeeds(0, ROTATE_SPEED);
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}
		}
	}

	// Rotate the whole robot at a specified speed
	public void turn(int speed) {
		rightMotor.setSpeed(-speed);
		leftMotor.setSpeed(speed);
		leftMotor.forward();
		rightMotor.backward();

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
