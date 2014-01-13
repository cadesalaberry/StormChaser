import lejos.nxt.Battery;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Storm {

	/*
	 * Declarations of Motor attached to the MotorPorts.
	 */
	private static NXTRegulatedMotor leftWheelMotor = new NXTRegulatedMotor(
			MotorPort.A);
	private static NXTRegulatedMotor rightWheelMotor = new NXTRegulatedMotor(
			MotorPort.C);
	private static NXTRegulatedMotor triggerMotor = new NXTRegulatedMotor(
			MotorPort.B);

	/*
	 * Declarations of Sensors attached to the SensorPorts.
	 */
	private static TouchSensor ts = new TouchSensor(SensorPort.S3);

	/*
	 * Declaration of variables to report over bluetooth.
	 */
	public static int currentDelay = 0;
	public static int launcherDelay = 4;
	public static int launcherPower = -1;
	public static int launcherSpeed = 700;

	/*
	 * Declare Objects used in the main.
	 */
	private static Menu mainMenu = new Menu();
	private static Launcher launcher = new Launcher(leftWheelMotor,
			rightWheelMotor, triggerMotor);
	private static RS485Listener listener = new RS485Listener(launcher);

	public static int iButton = 0;
	public static int iTime = 0;

	public static final int DEFAULT_DELAY = 25;

	public static void main(String[] args) {

		compete();
		//testSpeed();
		turnOff();
	}

	/**
	 * Asks the user what speed to use for every shoots.
	 * This method is used as testing for the launcher.
	 */
	public static void testSpeed() {

		launcherDelay = mainMenu.askForDelay();
		launcherSpeed = mainMenu.askForSpeed(700);
		// Loop if nobody presses the escape button
		while (iButton != Button.ID_ESCAPE) {

			LCD.clear();
			LCD.drawString("S:" + launcherSpeed, 0, 1);
			LCD.drawString("D:" + launcherDelay, 0, 2);
			LCD.drawString("V:" + Battery.getVoltageMilliVolt(), 0, 3);
			
			launcher.chainFireAtSpeed(launcherSpeed, launcherDelay);
			
			launcherSpeed = mainMenu.askForSpeed(launcherSpeed);
			iButton = Button.waitForPress();

		}
	}

	/**
	 * Gives us the choice between setPower(), or setSpeed().
	 * It allowed us to test which one we should use.
	 */
	public static void test() {

		/*
		 * First, get the values from the user by displaying the instructions on
		 * the screen. If the user agrees with the parameters set in the menu,
		 * proceed to the launching. If not, display the menu again.
		 */

		while (!mainMenu.summarizeLauncher()) {

			if (mainMenu.display()) {
				launcherPower = mainMenu.askForPower();
			} else {
				launcherSpeed = mainMenu.askForSpeed();
			}

			launcherDelay = mainMenu.askForDelay();

		}

		LCD.clearDisplay();
		LCD.drawString("     Shooting     ", 0, 0);

		if (launcherPower != -1) {

			launcher.chainFireAtPower(launcherPower, launcherDelay);

		} else {

			launcher.chainFireAtSpeed(launcherSpeed, launcherDelay);
		}

		LCD.clearDisplay();
	}
	/**
	 * Starts to listen for connection to Chaser.
	 */
	public static void compete() {
		listener.start();
		listener.listen(100);
	}

	/**
	 * Properly turns off the Brick.
	 */
	public static void turnOff() {
		if (listener != null)
				listener.stop();
		LCD.drawString("Program finished.", 0, 0);
		Button.waitForPress();
	}
}