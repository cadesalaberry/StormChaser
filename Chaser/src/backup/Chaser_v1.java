package backup;

/**
 * Chaser.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 * 
 */

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.Bluetooth;

public class Chaser_v1 {

	static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor defenseMotor = new NXTRegulatedMotor(MotorPort.C);

	public final static double LEFT_RADIUS = 2.67;
	public final static double RIGHT_RADIUS = 2.67;
	public final static double WIDTH = 16.1;

	private static Bluetooth bt = new Bluetooth(200);
	private static Odometer odometer = new Odometer(leftMotor, rightMotor);
	private static RobotCtrl robotCtrl = new RobotCtrl(odometer, leftMotor,
			rightMotor);
	private static USLocalizer usLocalization = new USLocalizer(robotCtrl,
			SensorPort.S3);
	private static Navigator nav = new Navigator(robotCtrl, odometer);

	public static void main(String[] args) {

		bt.start();
		// localizeUS(usLocalization);
		// lightSensorTest(robotCtrl);
		navigation();
		bt.stop();

		/*
		 * Get values from bluetooth, Start Bluetooth Logger Start odometer, Do
		 * localization, start correction, Loop { Navigate to dispenser avoiding
		 * objects, get ball from dispenser, compute best position to shoot, go
		 * to best position avoiding objects, shoot }
		 */

	}

	public static void localizeUS() {

		bt.addToReport(usLocalization);
		bt.start();
		usLocalization.start();
	}

	public static void lightSensorTest(RobotCtrl robotCtrl) {
		LineSensor ls = new LineSensor(SensorPort.S1, 100);
		bt.start();
		robotCtrl.flt();
		ls.start();
	}

	public static void navigation() {

		bt.addToReport(nav);
		bt.start();
		nav.defineRestrictions();
		nav.travelPath(nav.searchNode(90));// x: 135 y: 75

	}
}
