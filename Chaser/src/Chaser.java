/**
 * Chaser.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 4
 * 
 */

import bluetooth.*;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;

public class Chaser {

	static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor defenseMotor = new NXTRegulatedMotor(MotorPort.C);

	public final static double LEFT_RADIUS = 2.64;
	public final static double RIGHT_RADIUS = 2.64;
	public final static double WIDTH = 16.0;
	public final static double LIGHT_TO_CENTER = 10.5;
	public final static double LIGHT_L_TO_LIGHT_R = 18.0;
	public final static double ANGLE = Math.toRadians(29.0);
	public final static int DEFENSE_SPEED = 600;
	public final static int DEFENSE_ROTATION_ANGLE = 150;

	/*
	 * Prepare for the input via bluetooth. Uses default values if nothing is
	 * reset.
	 */
	public static int DISPENSER_X = 1, DISPENSER_Y = 10;
	public static int DISPENSER_ORIENTATION = 3;
	public static int DEFENSE_ZONE_X = 2, DEFENSE_ZONE_Y = 2;
	public static StartCorner CORNER = StartCorner.TOP_RIGHT; // CARLOS!!!!!!!!!!!!!!! I CHANGED THIS TO
										// AN INT
	public static PlayerRole ROLE = PlayerRole.FORWARD;

	public static boolean isTurning = false;
	public static boolean isNavigating = false;

	private static Bluetooth bt = new Bluetooth(50);
	private static Odometer odometer = new Odometer(leftMotor, rightMotor);

	public static LineSensor lineSensorL = new LineSensor(SensorPort.S1);
	public static LineSensor lineSensorR = new LineSensor(SensorPort.S2);
	private static OdometerCorrection odoCorrect = new OdometerCorrection(
			leftMotor, rightMotor, odometer, lineSensorL, lineSensorR);
	private static RobotCtrl robotCtrl = new RobotCtrl(odometer, odoCorrect, leftMotor,
			rightMotor);
	private static USLocalizer usLocalization = new USLocalizer(robotCtrl,
			SensorPort.S3);
	private static LightLocalizer lightLocalization = new LightLocalizer(
			robotCtrl, lineSensorL,  odoCorrect);

	private static Navigator nav = new Navigator(robotCtrl, odometer);
	private static Dispenser Dispenser = new Dispenser(robotCtrl);

	private static RS485Transmiter transmiter = new RS485Transmiter();
	private static Defense defense = new Defense(nav, defenseMotor, robotCtrl);

	public static void main(String[] args) {

		compete();
		turnOff();
	}

	/**
	 * Testing class.
	 */
	private static void jonathanTest() {
		//connectToStorm();
		//bt.start();
		//bt.addToReport(odometer);
		
		//receiveBluetoothInstructions();
		localizeUS();
		//lineSensorR.start();
		//lineSensorL.start();
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		//odoCorrect.start();/*
	
		//localizeLight2();
		//lightLocalization.start2();
		 //robotCtrl.travelTo(15, 15);
		/* lineSensorR.start();
		lineSensorL.start();
		odoCorrect.start();*/
		nav.defineRestrictions(false);
		//nav.travelPath(nav.searchNode(35));
		nav.dispenserNode();
		//
		Dispenser.press();
		//
		nav.shootNode();
		//defense.start();
		//
		//shoot();
		//odoCorrect.start();
		//defense.start();
		//Sound.beepSequenceUp();*/
	}

	/**
	 * Makes StormChaser follow the routine of the competition.
	 * Please never edit it.
	 */
	private static void compete() {

		connectToStorm();
		receiveBluetoothInstructions();

		localizeUS();
		lineSensorL.start();
		lineSensorR.start();
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}
		odoCorrect.start();

		nav.defineRestrictions(ROLE == PlayerRole.DEFENDER);
		while ( ROLE == PlayerRole.FORWARD ) {
			nav.dispenserNode();
		
			odoCorrect.stop();
			Dispenser.press();
			odoCorrect.start();

			nav.shootNode();
			shoot();

		}
		defense.start();

	}
	/**
	 * Localizes StormChaser using the Ultrasonic Sensor.
	 * StormChaser ends up to face approximately 45 degrees.
	 */
	public static void localizeUS() {

		bt.addToReport(usLocalization);
		bt.start();
		usLocalization.startSimple2();
		bt.stop();
	}

	/**
	 * Localizes StormChaser by going to the closest intersection,
	 * rotating, and correcting it's angle relative to the line detected.
	 */
	@Deprecated
	public static void localizeLight() {
		// lineSensorL.startNotifying(lightLocalization);
		Chaser.isNavigating=true;
		lightLocalization.start2();

		robotCtrl.stop();

	}
	
	/**
	 * Light localizes by hitting a vertical line, correcting y,
	 * going back, turning at 90 degrees, then hitting an horizontal line and correcting x.
	 */
	@Deprecated
	public static void localizeLight2() {
		// lineSensorL.startNotifying(lightLocalization);

		
		robotCtrl.turn(45);
		robotCtrl.goForward(28);
		robotCtrl.goBackward(28);
		robotCtrl.turn(-90);
		robotCtrl.goForward(28);
		robotCtrl.goBackward(28);
		robotCtrl.stop();

	}
	
	/**
	 * Tests the LineSensor class by turning them on, and putting the robot in float mode.
	 */
	public static void lightSensorTest() {
		LineSensor lls = new LineSensor(SensorPort.S1, 100);
		LineSensor rls = new LineSensor(SensorPort.S2, 100);
		bt.start();
		bt.addToReport(lls);
		bt.addToReport(rls);
		robotCtrl.flt();
		lls.start();
		rls.start();
		Button.waitForPress();
		bt.stop();
	}
	
	/**
	 * Tests the Navigation class by travelling to a specific node.
	 */
	public static void navigationTest() {

		bt.start();
		bt.addToReport(nav);
		odoCorrect.start();

		nav.defineRestrictions(false);
		nav.travelPath(nav.searchNode(107));// x: 135 y: 75

	}
	
	/**
	 * Tests the OdometryCorrection class by travelling a path while it is used.
	 */
	public static void CorrectionTest() {

		/*
		TODO: implement BluetoothReporter in OdometryCorrection
		bt.start();
		bt.addToReport(odoCorrect);
		*/
	
		odoCorrect.start();

		robotCtrl.travelTo(30.5, 0);
		robotCtrl.travelTo(45.75, 0);
		robotCtrl.travelTo(61, 0);
		odoCorrect.stop();

		/*
		 * robotCtrl.travelTo(76.25, 30.5);
		 * 
		 * 
		 * robotCtrl.travelTo(76.25,45.75); robotCtrl.travelTo(76.25, 61);
		 */
		// robotCtrl.travelTo(76.25, 61);
		// robotCtrl.travelTo(75, 15);
		// robotCtrl.travelTo(75, 15);*/

		// robotCtrl.travelTo(75, 45);
		// robotCtrl.travelTo(75, 75);

	}
	
	/**
	 * Tries to connect with Storm and then tells him to shoot.
	 */
	public static void shootTest(){
		connectToStorm();
		shoot();
	}
	
	/**
	 * Transmits the speed at which Storm should shoot.
	 * Returns when done.
	 */
	public static void shoot() {
		try {
			/*
			 * Transmit the speed at which it should shoot.
			 */
			transmiter.transmit(600);
		} catch (Exception e) {
		}

	}
	
	
	/**
	 * Connects to the other NXT Brick.
	 */
	public static void connectToStorm() {

		try {
			transmiter.start();
		} catch (Exception e) {
		}
	}

	/**
	 * Disconnects from the other NXT Brick.
	 */
	public static void disconnectFromStorm() {

		try {
			transmiter.stop();
		} catch (Exception e) {
		}
	}

	/**
	 * Gets StormChaser ready to receive the field infos via bluetooth.
	 */
	public static void receiveBluetoothInstructions() {
		BluetoothConnection conn = new BluetoothConnection();
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			CORNER = t.startingCorner;
			ROLE = t.role;
			DEFENSE_ZONE_X = t.w1;
			DEFENSE_ZONE_Y = t.w2;
			DISPENSER_X = t.bx;
			DISPENSER_Y = t.by;
			DISPENSER_ORIENTATION = t.bsigma;
			// print out the transmission information
			conn.printTransmission();
		}
		// stall until user decides to end program
		// Button.waitForPress();
	}

	/**
	 * Used before to transmit the distance at which StormChaser should shoot.
	 * @param distance
	 */
	
	@Deprecated
	public static void shootAt(double distance) {

		try {
			transmiter.transmit(distance);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Properly turns off Storm.
	 */
	public static void turnOff() {

		if (odoCorrect != null)
			odoCorrect.stop();
		if (odometer != null)
			odometer.stop();
		if (bt != null)
			bt.stop();
		if (transmiter != null)
			disconnectFromStorm();

	}

}
