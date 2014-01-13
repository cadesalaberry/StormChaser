/** Bluetooth.java
 * 
 * @author Charles-Antoine de Salaberry, Nigel Kut
 * @version 1
 */

import lejos.util.Timer;
import lejos.util.TimerListener;

public class OdometerCorrector implements TimerListener {

	Odometer odometer;
	Timer odometerCorrectorTimer;
	private LineSensor leftLineSensor;
	private LineSensor rightLineSensor;

	private static final int DEFAULT_REFRESH = 100;

	public OdometerCorrector(Odometer odometer, LineSensor leftLineSensor,
			LineSensor rightLineSensor, int correctorRefresh) {

		this.odometer = odometer;
		this.leftLineSensor = leftLineSensor;
		this.rightLineSensor = rightLineSensor;
		this.odometerCorrectorTimer = new Timer(correctorRefresh, this);

	}

	public OdometerCorrector(Odometer odometer, LineSensor leftLineSensor,
			LineSensor rightLineSensor) {
		this(odometer, leftLineSensor, rightLineSensor, DEFAULT_REFRESH);
	}

	public void timedOut() {

		if (leftLineSensor.checkForLine()) {

		}
		if (rightLineSensor.checkForLine()) {

		}
		// if line, identify light sensor
		// get position of light sensor
		// correct position of the light sensor that beeped
		// correct position of rotation center from the position position
		correctPosition();

	}

	public void correctPosition() {

	}

	private double getClosestLine(double input) {

		return Math.round(input / 30.48) * 30.48;
	}

	private double getXLineSensor() {
		return 0;
	}

	private double getYLineSensor() {
		return 0;
	}
}