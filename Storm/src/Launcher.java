/*
 * Launcher.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 2
 */

import lejos.nxt.*;
import lejos.nxt.comm.RConsole;

public class Launcher {

	public NXTRegulatedMotor leftWheelMotor;
	public NXTRegulatedMotor rightWheelMotor;
	public NXTRegulatedMotor triggerMotor;

	public static TouchSensor ts = new TouchSensor(SensorPort.S3);
	public int iButton = 0;

	public int currentDelay = 0;
	public int launcherDelay = 0;
	public int launcherPower = -1;
	public int launcherSpeed = 0;

	public final int DEFAULT_DELAY = 2500;
	String status;

	public Launcher() {

	}

	public Launcher(NXTRegulatedMotor leftWheelMotor,
			NXTRegulatedMotor rightWheelMotor, NXTRegulatedMotor triggerMotor) {

		this.leftWheelMotor = leftWheelMotor;
		this.rightWheelMotor = rightWheelMotor;
		this.triggerMotor = triggerMotor;
	}

	/**
	 * Shoot at given speed after a default delay of 2.5s.
	 * @param speed
	 */
	public void shootAtSpeed(int speed) {
	
		fireAtSpeedWithDelay(speed, DEFAULT_DELAY);
	}

	/**
	 * Loop the fire function in order to shoot multiple time consecutively
	 * without restarting the program.
	 * 
	 * @param power
	 *            power at which the launcher will fire.
	 * @param delay
	 *            delay after which the launcher will fire.
	 */

	public void chainFireAtPower(int power, int delay) {

		// Loop if nobody presses the escape button
		while (iButton != Button.ID_ESCAPE) {

			if (ts.isPressed() || iButton == Button.ID_ENTER) {

				LCD.drawString("P:" + power, 0, 1);
				LCD.drawString("V:" + Battery.getVoltageMilliVolt(), 7, 1);
				LCD.drawString("D:" + delay, 0, 1);
				fireAtPowerWithDelay(power, delay);

			} else {
				resetLauncher();
			}

			iButton = Button.waitForPress(100);
		}
	}

	/**
	 * Loop the fire function in order to shoot multiple time consecutively
	 * without restarting the program.
	 * 
	 * @param delay
	 *            delay after which the launcher will fire.
	 */

	public void chainFireAtSpeed(int speed, int delay) {

		// Loop if nobody presses the escape button
		while (iButton != Button.ID_ESCAPE) {

			if (ts.isPressed() || iButton == Button.ID_ENTER) {
				
				LCD.drawString("V:" + Battery.getVoltageMilliVolt(), 0, 3);
				fireAtSpeedWithDelay(speed, delay);

			} else {

				resetLauncher();
			}

			iButton = Button.waitForPress(100);
		}
	}

	/**
	 * Uses MotorPort.*.controlMotor(power, mode) for the motors to get the max
	 * power before shooting.
	 * 
	 * @param power
	 *            power at which the motor will turn.
	 * @param delay
	 *            delay after which the system will trigger fire.
	 */

	void fireAtPowerWithDelay(int power, int delay) {

		// Start rotating the wheels.
		MotorPort.A.controlMotor(power, 1);
		MotorPort.C.controlMotor(power, 1);

		triggerAfter(delay);
		resetLauncher();

	}

	/**
	 * Uses Motor.*.setSpeed() with a counter that progressively increases the
	 * speed.
	 * 
	 * @param speed
	 *            speed at which the wheels are set to turn.
	 * @param delay
	 *            delay after which the launcher will fire.
	 */
	private void fireAtSpeedWithDelay(int speed, int delay) {

		leftWheelMotor.setSpeed(speed);
		rightWheelMotor.setSpeed(speed);

		leftWheelMotor.forward();
		rightWheelMotor.forward();

		triggerAfter(delay);

		resetLauncher();

	}

	/**
	 * The delay is provided in Milliseconds.
	 * 
	 * @param delay
	 *            delay after which the launcher will fire.
	 */
	private void triggerAfter(int delay) {

		try {

			// Start counting down.
			int currentDelay = 0;
			while (currentDelay < delay) {
				Thread.sleep(100);
				currentDelay += 100;
			}
		} catch (Exception e) {
		}
		trigger();
	}

	/**
	 * make the trigger move to push the balls into the two turning wheels.
	 * 
	 */
	private void trigger() {

		// Initialize the trigger speed.
		triggerMotor.setSpeed(100);
		triggerMotor.rotateTo(-20);
		triggerMotor.setSpeed(500);
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		triggerMotor.rotateTo(90);

		// Reload the trigger.
		triggerMotor.rotateTo(0);
	}

	/**
	 * Put the launcher's turning wheels into float mode for the gears not to
	 * block. It also resets the tachometers of the motors for the next launch.
	 * 
	 */
	void resetLauncher() {
		// Return all the motors to their original states.
		// (float mode)
		leftWheelMotor.flt(true);
		rightWheelMotor.flt(true);

		leftWheelMotor.setSpeed(0);
		rightWheelMotor.setSpeed(0);

		// Reset the tachometers for next launch.
		leftWheelMotor.resetTachoCount();
		rightWheelMotor.resetTachoCount();
	}

	/**
	 * Tries to use a approximation to vary speed in function of the distance needed.
	 * @param distance
	 * @return
	 */
	
	@Deprecated
	private int getSpeed(double distance) {

		return (int) (0.00000487216345319615506 * Math.pow(distance, 3)
				- 0.00436600207678971512 * Math.pow(distance, 2)
				+ 3.78534687966156148 * distance + 237.115488723734756);

	}
}
