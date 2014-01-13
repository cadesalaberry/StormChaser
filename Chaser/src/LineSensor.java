/** LineSensor.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 */

import java.util.ArrayList;

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LineSensor implements TimerListener, BluetoothReporter {

	private Timer lineSensorTimer;
	private LightSensor sensorLight;
	private String status;
	private ArrayList<LineListener> toNotify;

	/*
	 * Initialize the default values used.
	 */
	private final static int DEFAULT_REFRESH = 50;
	private final static int LINE_THRESHOLD = -20;
	private final static double DEFAULT_DISTANCE_FROM_ROTATION_CENTER = 10.3;
	private final static double DEFAULT_ANGLE_FROM_HEADING = 148.7;

	private int[] derivative0 = new int[3];
	private int[] derivative1 = new int[2];

	private static int numLines = 0;
	private boolean lineRead = false;

	private double angleFromHeading;
	private double distanceFromRotationCenter;

	private int secondDifference;

	/**
	 * Uses the LightSensor Position as input for a future implementation of LineListener,
	 * as well as for OdometryCorrector.
	 * @param port light sensor port
	 * @param lineSensorRefresh 
	 * @param distanceFromRotationCenter 
	 * @param angleFromHeading smallest angle between Storm Chaser heading and light sensor orientation.
	 */
	public LineSensor(SensorPort port, int lineSensorRefresh,
			double distanceFromRotationCenter, double angleFromHeading) {

		this.lineSensorTimer = new Timer(lineSensorRefresh, this);
		this.sensorLight = new LightSensor(port);
		this.toNotify = new ArrayList<LineListener>();

		sensorLight.setFloodlight(true);

		derivative0[0] = sensorLight.getNormalizedLightValue();
		derivative0[1] = sensorLight.getNormalizedLightValue();

		derivative1[0] = derivative0[1] - derivative0[0];

		sensorLight.setFloodlight(false);
	}
	
	/**
	 * Main Constructor. It takes as input the SensorPort of the light sensor,
	 * and the refresh delay for the timer.
	 * @param port
	 * @param lineSensorRefresh
	 */
	public LineSensor(SensorPort port, int lineSensorRefresh) {

		this(port, lineSensorRefresh, DEFAULT_DISTANCE_FROM_ROTATION_CENTER,
				DEFAULT_ANGLE_FROM_HEADING);
	}

	/**
	 * Constructor to keep backward compatibility.
	 * @param port
	 */
	public LineSensor(SensorPort port) {
		this(port, DEFAULT_REFRESH);
	}

	/**
	 * Compares the differences in the RAW values read from the light sensor
	 * to determine if a line has been read.
	 */
	 public void timedOut() {

		derivative0[2] = sensorLight.getNormalizedLightValue();
		derivative1[1] = derivative0[2] - derivative0[1];
		secondDifference = derivative1[1] - derivative1[0];

		if (secondDifference < LINE_THRESHOLD
				&& derivative1[1] < (LINE_THRESHOLD + 15) && !lineRead) {
			Sound.beep();
			numLines++;
			lineRead = true;

			
			for (LineListener a : toNotify) {
			
			a.lineIsRead();
			
			}

		} else
			lineRead = false;

		derivative1[0] = derivative1[1];

		derivative0[0] = derivative0[1];
		derivative0[1] = derivative0[2];

	}

	public int getSecondDifference() {
		return secondDifference;
	}

	public int getCurrentReadings() {
		return sensorLight.getLightValue();
	}

	/**
	 * Returns the number of lines the sensor have seen since it is on.
	 * @return number of lines seen
	 */
	public int getNumLines() {
		return numLines;
	}

	/**
	 * Returns true if a line is seen by the light sensor.
	 * @return true if a line is read.
	 */
	public boolean checkForLine() {
		return lineRead;
	}

	/**
	 * switches the lightSensor on, and start the LineSensor timer.
	 */
	public void start() {
		sensorLight.setFloodlight(true);

		lineSensorTimer.start();
	}

	/**
	 * Properly stops the LineSensor.
	 */
	public void stop() {

		lineSensorTimer.stop();
		sensorLight.setFloodlight(false);

	}

	/**
	 * Uses the LineListener interface to report a lineIsRead event.
	 * a is added to to list of LineListener to notify when a line is seen.
	 * @param a
	 */
	public void startNotifying(LineListener a) {

		/*
		 * Check if the object is already in the ArrayList.
		 */
		for (LineListener b : toNotify) {
			if (b == a)
				break;
		}
		/*
		 * If it does not, add to the toNotify ArrayList.
		 */
		toNotify.add(a);
	}

	/**
	 * Uses the LineListener interface to report a
	 * 
	 * @param a
	 */
	public void stopNotifying(LineListener a) {

		/*
		 * Look for the object in the ArrayList.
		 */
		for (LineListener b : toNotify) {
			if (b == a)
				toNotify.remove(toNotify.indexOf(a));
		}
	}

	/**
	 * Computes the position of the light sensor for the odometer position. It
	 * returns an array containing x and y.
	 * 
	 * @param odometerPosition
	 * @return
	 */
	public double[] getSensorPosition(double[] odometerPosition) {

		double[] sensorPosition = new double[2];

		sensorPosition[0] = odometerPosition[0]
				+ distanceFromRotationCenter
				* Math.cos(Math.toRadians(odometerPosition[2]
						+ angleFromHeading));
		sensorPosition[1] = odometerPosition[1]
				+ distanceFromRotationCenter
				* Math.sin(Math.toRadians(odometerPosition[2]
						+ angleFromHeading));

		return sensorPosition;
	}

	/**
	 * Returns a String representing the status of this class. (Structured as a
	 * table).
	 * 
	 */
	public String reportStatus(boolean getName) {

		if (getName) {

			status = new String("derivative0[0]");
			status += "\tderivative0[1]";
			status += "\tderivative0[2]";
			status += "\tderivative1[0]";
			status += "\tderivative1[1]";
			status += "\tnumLines";
			status += "\tsecondDifference";

		} else {

			status = new String("" + derivative0[0]);
			status += "\t" + derivative0[1];
			status += "\t" + derivative0[2];
			status += "\t" + derivative1[0];
			status += "\t" + derivative1[1];
			status += "\t" + numLines;
			status += "\t" + secondDifference;
		}
		return status;
	}

}
