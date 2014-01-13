import lejos.nxt.LCD;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class RobotCtrl {
	private final static double DEFAULT_LEFT_RADIUS = Chaser.LEFT_RADIUS;
	private final static double DEFAULT_RIGHT_RADIUS = Chaser.RIGHT_RADIUS;
	private final static double DEFAULT_WIDTH = Chaser.WIDTH;

	public NXTRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;
	private UltrasonicSensor us;
	private OdometerCorrection odoCorrect;

	private final static int FORWARD_SPEED = 200;// 250
	private final static int ROTATION_SPEED = 125;// 150

	private final int OBSTACLE_TRESHOLD = 40;
	private int distanceUS;
	
	private static boolean avoidance = true; //<----- set this for obstacle avoidance
	
	private double xC, yC, thetaC, dist;
	private double thetaD, thetaR;
	private double leftRadius = 0;
	private double rightRadius = 0;
	private double width = 0;

	private boolean obstacle = false;// <--- Ultrasonic sensor boolean!!

	/**
	 * Constructor
	 * 
	 * @param odometer
	 * @param leftMotor
	 * @param rightMotor
	 * @param width
	 * @param leftRadius
	 * @param rightRadius
	 */

	public RobotCtrl(Odometer odometer, OdometerCorrection odoCorrect,NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width, double leftRadius,
			double rightRadius) {
		this.odometer = odometer;
		this.odoCorrect = odoCorrect;
		this.leftMotor = odometer.getLeftMotor();
		this.rightMotor = odometer.getRightMotor();
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;

		this.us = new UltrasonicSensor(SensorPort.S3);

		setRobotAccelerations(1000);
		setRobotSpeed(FORWARD_SPEED);
	}

	public RobotCtrl(Odometer odometer, OdometerCorrection odoCorrect) {
		this(odometer, odoCorrect,odometer.getLeftMotor(), odometer.getRightMotor());
	}

	public RobotCtrl(Odometer odometer, OdometerCorrection odoCorrect ,NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor) {
		this(odometer, odoCorrect ,leftMotor, rightMotor, DEFAULT_WIDTH);
	}

	public RobotCtrl(Odometer odometer, OdometerCorrection odoCorrect , NXTRegulatedMotor leftMotor,
			NXTRegulatedMotor rightMotor, double width) {
		this(odometer, odoCorrect , leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS,
				DEFAULT_RIGHT_RADIUS);
	}

	public boolean travelTo(double x, double y) {

		dist = face(x, y);

		if (avoidance && obstacleInFront())
			return false;

		// setRobotSpeed(FORWARD_SPEED);

		goForward(dist);

		return true;
	}

	/**
	 * Face the specified coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return distance from the point
	 */
	public double face(double x, double y) {
		// Gets the x and y value between nxt and the target point
		xC = x - odometer.getX();
		yC = y - odometer.getY();

		// Calculates the angle that the nxt should turn, in order to face
		// the target point

		thetaD = Math.toDegrees(Math.atan2(yC, xC));

		// Turns the nxt to an angle to face the destination

		turnTo(thetaD);

		return (Math.sqrt((xC * xC * 100.0) + (yC * yC * 100.0))) / 10.0;
	}

	/**
	 * Make the robot turn until it reaches the specified angle.
	 * 
	 * @param angle
	 */
	public void turnTo(double angle) { // ONLY WITH ODOCORRECTION

		// return in degrees
		thetaR = odometer.getTheta();

		if ((angle - thetaR) < -180) {
			thetaC = (angle - thetaR + 360);
		} else if ((angle - thetaR) > 360) {
			thetaC = (angle - thetaR - 360);
		} else {
			thetaC = (angle - thetaR);
		}
		//System.out.println("ThetaC: "+thetaC);
			if(-60 > thetaC || thetaC > 60){
				odoCorrect.stop();
				turn(thetaC);
				odoCorrect.start();
			}
			else
			turn(thetaC);
			
		
	}

	public void turn2(double angle) {

		// return in degrees
		odoCorrect.stop();
		thetaR = odometer.getTheta();

		if ((angle - thetaR) < -180) {
			thetaC = (angle - thetaR + 360);
		} else if ((angle - thetaR) > 360) {
			thetaC = (angle - thetaR - 360);
		} else {
			thetaC = (angle - thetaR);
		}

		turn(thetaC);
	}

	/**
	 * Make the robot turn until it reaches the specified angle.
	 * 
	 * @param angle
	 */
	public void turn(double angle) {

		/*
		 * int i = 0; double newAngle = odometer.getHeading() + angle;
		 * 
		 * if (angle < 0) this.setSpeeds(0, ROTATION_SPEED);
		 * 
		 * else if (angle > 0) this.setSpeeds(0, -ROTATION_SPEED);
		 * 
		 * while (!(-5 < odometer.getHeading() - newAngle &&
		 * odometer.getHeading() - newAngle < 5)) { LCD.drawInt(++i, 0, 5); try
		 * { Thread.sleep(50); } catch (Exception e) { } } LCD.drawChar('o', 0,
		 * 5); flt();
		 */
		
		// Rotates the nxt to face a particular angle
			setRobotSpeed(ROTATION_SPEED);
			leftMotor.rotate(-getTachoFromAngle(leftRadius, width, angle), true);
			rightMotor.rotate(getTachoFromAngle(rightRadius, width, angle), false);
			setRobotSpeed(FORWARD_SPEED);
			

	}

	@Deprecated
	public void turnImmediate(double angle) {

		setRobotSpeed(ROTATION_SPEED);

		// Rotates the nxt to face a particular angle
		leftMotor.rotate(-getTachoFromAngle(leftRadius, width, angle), true);
		rightMotor.rotate(getTachoFromAngle(rightRadius, width, angle), true);

	}

	public void changeDirectionOfRotation() {
		this.setSpeeds(0, -ROTATION_SPEED);
	}

	/**
	 * Added by Jonathan, since the dispenser and the basket are both at
	 * intersections of lines we need to a method to go to intersections.
	 */

	public void goToIntersect() {
		turnTo(odometer.getTheta() - 90);

		goForward(15.5);

		turnTo(odometer.getTheta() + 90);
		goForward(15.5);

	}

	// method that does the opposite of the goToIntersect
	public void fromIntersectToNode() {
		turnTo(odometer.getTheta() + 90);

		goForward(15.5);

		turnTo(odometer.getTheta() + 90);
		goForward(15.5);
	}

	/**
	 * Chech for obstacle in front, at 18 degrees left and at 18 degrees right.
	 * 
	 * @return true if obstacle is found.
	 */

	public boolean obstacleInFront() {

		// double originalAngle = odometer.getTheta();

		obstacle = checkForObstacle();

		// if (!obstacle) {
		// turnTo(originalAngle + 18);
		// obstacle = checkForObstacle();
		// }
		//
		// if (!obstacle) {
		// turnTo(originalAngle - 18);
		// obstacle = checkForObstacle();
		// }
		//
		//
		// turnTo(originalAngle);

		return obstacle;
	}

	public boolean checkForObstacle() {
		int numberOfPing = 10;
		int total = 0;
		int mean;

		for (int i = 0; i < numberOfPing; i++) {
			total += getUSDistance();
		}

		mean = total / numberOfPing;

		obstacle = (mean < OBSTACLE_TRESHOLD);

		return obstacle;
	}

	private int getUSDistance() {

		us.ping();
		distanceUS = us.getDistance();
		return distanceUS;
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
		return (int) ((width * angle) / (2.0 * radius));
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

	/*
	 * Mutators
	 */

	public void setSpeeds(double forwardSpeed, double rotationalSpeed) {
		double leftSpeed, rightSpeed;

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

	public void setRobotAccelerations(int acceleration) {

		leftMotor.setAcceleration(acceleration);
		rightMotor.setAcceleration(acceleration);
	}

	public void setRobotSpeed(int speed) {

		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}

	public void goForward(double distance) {

		/*
		 * Restoring forward speed to the motors.
		 */
		setRobotSpeed(FORWARD_SPEED);
		leftMotor.rotate(getTachoFromDistance(this.leftRadius, distance), true);
		rightMotor.rotate(getTachoFromDistance(this.rightRadius, distance),
				false);
	}
	public void goForwardOdo(double distance) {

		/*
		 * Restoring forward speed to the motors.
		 */
		setRobotSpeed(FORWARD_SPEED);
		leftMotor.rotate(getTachoFromDistance(this.leftRadius, distance), true);
		rightMotor.rotate(getTachoFromDistance(this.rightRadius, distance),
				false);
	}

	public void goBackward(double distance) {
		goForward(-distance);
	}

	public void turnLeft(int speed) {
		setSpeeds(0, -speed);
	}

	public void turnRight(int speed) {
		setSpeeds(0, speed);
	}

	public void flt() {
		leftMotor.flt(true);
		rightMotor.flt(true);
	}

	// Stops both motors
	public void stop() {
		setRobotSpeed(0);
	}

	// Accessors
	public Odometer getOdometer() {
		return this.odometer;
	}

}
