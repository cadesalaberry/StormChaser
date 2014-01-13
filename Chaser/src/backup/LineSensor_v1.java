package backup;

/** LineSensor.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 */

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LineSensor_v1 implements TimerListener {

	private Timer lineSensorTimer;
	private LightSensor sensorLight;
	private String status;

	private static final int DEFAULT_REFRESH = 100;
	private final boolean getName = true;

	private int[] derivative0 = new int[3];
	private int[] derivative1 = new int[2];
	private final int LINE = -20;
	private static int numLines = 0;
	boolean lineRead = false;

	public static int secondDifference;

	public LineSensor_v1(SensorPort port, int lineSensorRefresh) {

		this.lineSensorTimer = new Timer(lineSensorRefresh, this);
		this.sensorLight = new LightSensor(port);

		sensorLight.setFloodlight(true);

		derivative0[0] = sensorLight.getNormalizedLightValue();
		derivative0[1] = sensorLight.getNormalizedLightValue();

		derivative1[0] = derivative0[1] - derivative0[0];

		// sensorLight.setFloodlight(false);
	}

	public LineSensor_v1(SensorPort port) {
		this(port, DEFAULT_REFRESH);
	}

	public void timedOut() {

		// sensorLight.setFloodlight(true);

		derivative0[2] = sensorLight.getNormalizedLightValue();
		derivative1[1] = derivative0[2] - derivative0[1];
		secondDifference = derivative1[1] - derivative1[0];

		if (secondDifference < LINE && derivative1[1] < (LINE + 10)
				&& !lineRead) {
			Sound.beep();
			numLines++;
			lineRead = true;
		} else
			lineRead = false;

		derivative1[0] = derivative1[1];

		derivative0[0] = derivative0[1];
		derivative0[1] = derivative0[2];

		// sensorLight.setFloodlight(false);
	}

	public int getSecondDifference() {
		return secondDifference;
	}

	public int getCurrentReadings() {
		return sensorLight.getLightValue();
	}

	public int getNumLines() {
		return numLines;
	}

	public boolean checkForLine() {
		return lineRead;
	}

	public void start() {
		sensorLight.setFloodlight(true);
		lineSensorTimer.start();
	}

	public void stop() {
		sensorLight.setFloodlight(false);
		lineSensorTimer.stop();
	}

	public String reportStatus() {

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
