package backup;

import lejos.nxt.NXTRegulatedMotor;

public class RobotCtrl_v1 {
	public static final double DEFAULT_LEFT_RADIUS = 2.75;
	public static final double DEFAULT_RIGHT_RADIUS = 2.75;
	public static final double DEFAULT_WIDTH = 15.8;
	public NXTRegulatedMotor leftMotor, rightMotor;
	private double leftRadius, rightRadius, width;
	private double forwardSpeed, rotationSpeed;

	public RobotCtrl_v1(NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width, double leftRadius,
			double rightRadius) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
	}

	public RobotCtrl_v1(NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor) {
		this(leftMotor, rightMotor, DEFAULT_WIDTH, DEFAULT_LEFT_RADIUS,
				DEFAULT_RIGHT_RADIUS);
	}

	public RobotCtrl_v1(NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width) {
		this(leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS,
				DEFAULT_RIGHT_RADIUS);
	}

	// accessors
	public double getDisplacement() {
		return (leftMotor.getTachoCount() * leftRadius + rightMotor
				.getTachoCount() * rightRadius)
				* Math.PI / 360.0;
	}

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
}
