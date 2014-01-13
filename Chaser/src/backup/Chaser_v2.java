package backup;

/**
 * Chaser.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 0
 * 
 */

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.Bluetooth;

public class Chaser_v2 {

	static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor defenseMotor = new NXTRegulatedMotor(MotorPort.C);

	public final static double LEFT_RADIUS = 2.65;
	public final static double RIGHT_RADIUS = 2.65;
	public final static double WIDTH = 16.1;
	public final static double LIGHT_TO_CENTER = 15.0;
	public final static double LIGHT_L_TO_LIGHT_R = 15.0;
	public final static double LEFT_LIGHT_ANGLE = 15.0;
	public final static double RIGHT_LIGHT_ANGLE = 15.0;
	public final static double ANGLE = Math.toRadians(45);

	public final double BASKET_X = 91.5, BASKET_Y = 152.5;

	private static Bluetooth bt = new Bluetooth(50);
	private static Odometer odometer = new Odometer(leftMotor, rightMotor);
	private static RobotCtrl robotCtrl = new RobotCtrl(odometer, leftMotor,
			rightMotor);
	private static USLocalizer usLocalization = new USLocalizer(robotCtrl,
			SensorPort.S3);
	private static Navigator nav = new Navigator(robotCtrl, odometer);
	private static LineSensor lineSensorL = new LineSensor(SensorPort.S1);
	private static LineSensor lineSensorR = new LineSensor(SensorPort.S2);
	private static OdometerCorrection_v4 odoCorrect = new OdometerCorrection_v4(
			robotCtrl, lineSensorL, lineSensorR);
	private static RS485Transmiter transmiter;

	public static void main(String[] args) {

		// connectToStorm()
		localizeUS();
		// startCorrection();
		nav.defineRestrictions();
		nav.travelPath(nav.searchNode(50));
		// dispenser()
		// shootAt(computeDistanceFromBasket())

		// CorrectionTest();

		Button.waitForPress();

		/*
		 * Get values from bluetooth, Start Bluetooth Logger Start odometer, Do
		 * localization, start correction, Loop { Navigate to dispenser avoiding
		 * objects, get ball from dispenser, compute best position to shoot, go
		 * to best position avoiding objects, shoot }
		 */
		// disconnectFromStorm()
		// bt.stop();
	}

	public static void localizeUS() {

		bt.addToReport(usLocalization);
		bt.start();
		usLocalization.startSimple();
	}

	public static void lightSensorTest() {
		LineSensor lls = new LineSensor(SensorPort.S1, 100);
		LineSensor rls = new LineSensor(SensorPort.S2, 100);
		bt.start();
		robotCtrl.flt();
		lls.start();
		rls.start();
		Button.waitForPress();
	}

	public static void navigationTest() {

		bt.addToReport(nav);
		bt.start();
		odoCorrect.start();
		// nav.defineRestrictions();
		// nav.travelPath(nav.searchNode(107));
		Button.waitForPress();// x: 135 y: 75

	}

	public static void CorrectionTest() {

		bt.addToReport(odoCorrect);
		bt.start();
		odoCorrect.start();
		robotCtrl.travelTo(90, 0);
		odoCorrect.stop();
		bt.stop();
		Button.waitForPress();
	}

	public static void connectToStorm() {
		transmiter.start();
	}

	public void disconnectFromStorm() {
		transmiter.stop();
	}

	public void shootAt(double distance) {

		try {
			transmiter.transmit(distance);
		} catch (Exception e) {
		}
	}

	public double computeDistanceFromBasket() {

		return Math.sqrt(Math.pow(odometer.getX() - BASKET_X, 2)
				+ Math.pow(odometer.getY() - BASKET_Y, 2));
	}

}
