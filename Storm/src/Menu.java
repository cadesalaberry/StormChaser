import lejos.nxt.Button;
import lejos.nxt.LCD;

public class Menu {
	
	public final int DEFAULT_DELAY = 4;
	public int iButton = 0;
	public static int launcherSpeed;
	public static int launcherDelay;
	public static int launcherPower;
	
	int menuSpeed = 600;
	/**
	 * Start displaying the menu on the screen, asking for power mode, or speed
	 * mode.
	 */
	
	public Menu(){
		LCD.clearDisplay();
	}
	
	/**
	 * Gives the choice between Power and Speed.
	 * @return true if power is choosed.
	 */
	public boolean display() {

		LCD.clearDisplay();
		LCD.drawString("                 ", 0, 0);
		LCD.drawString("  Power | Speed  ", 0, 2);
		LCD.drawString("    <   |   >    ", 0, 4);

		iButton = Button.waitForPress();

		if (iButton == Button.ID_LEFT) {

			return true;

		}
		
		return false;
		
	}

	/**
	 * Asks for the user to confirm its choice of settings.
	 * @return
	 */
	public boolean summarizeLauncher() {

		LCD.clearDisplay();
		LCD.drawString("Delay: " + launcherDelay, 0, 0);
		LCD.drawString("Speed: " + launcherSpeed, 0, 1);
		LCD.drawString("Power: " + launcherPower, 0, 2);
		LCD.drawString("OKI ?", 0, 5);

		iButton = Button.waitForPress();

		if (iButton == Button.ID_ESCAPE) {

			return false;

		}

		return true;
	}

	/**
	 * Display on screen for the user to customize the power at which the motors
	 * should be turning before it fires.
	 * 
	 * @return power the power that will be applied to the motors.
	 */
	public int askForPower() {
		LCD.clearDisplay();
		LCD.drawString("      Power     ", 0, 0);
		LCD.drawString("     (In %)     ", 0, 1);
		LCD.drawString("   Default=80%  ", 0, 2);

		iButton = 0;
		int menuPower = 80;

		while (iButton != Button.ID_ESCAPE) {

			LCD.drawString("                     ", 0, 4);
			LCD.drawString("  <|-| " + menuPower + " |+|>  ", 0, 4);

			iButton = Button.waitForPress();

			if (iButton == Button.ID_LEFT) {

				if (menuPower > 0)
					menuPower -= 1;

			} else if (iButton == Button.ID_RIGHT) {

				if (menuPower < 100)
					menuPower += 1;

			} else if (iButton == Button.ID_ENTER) {

				return menuPower;
			}
		}
		return menuPower;
	}

	
	/**
	 * Display on screen for the user to customize the speed at which the motors
	 * should be turning before it fires.
	 * 
	 * @return menuSpeed the speed that the motors will turn at.
	 */
	public int askForSpeed() {
		LCD.clearDisplay();
		LCD.drawString("      Speed     ", 0, 0);
		LCD.drawString("   (Max=2000)   ", 0, 1);
		LCD.drawString("  Default=600  ", 0, 2);

		iButton = 0;
		

		while (iButton != Button.ID_ESCAPE) {

			LCD.drawString("                     ", 0, 4);
			LCD.drawString("  <|-| " + menuSpeed + " |+|>  ", 0, 4);

			iButton = Button.waitForPress();

			if (iButton == Button.ID_LEFT) {

				if (menuSpeed > 0)
					menuSpeed -= 25;

			} else if (iButton == Button.ID_RIGHT) {

				if (menuSpeed < 2000)
					menuSpeed += 25;

			} else if (iButton == Button.ID_ENTER) {

				return menuSpeed;
			}
		}
		launcherSpeed = menuSpeed;
		return menuSpeed;
	}
	
	public int askForSpeed(int lastUsed) {
		menuSpeed = lastUsed;
		return askForSpeed();
	}
	
	
	/**
	 * Display on screen for the user to customize the delay before the robot
	 * fires.
	 * 
	 * @return delay the time the robot should wait before shooting.
	 */
	public int askForDelay() {
		LCD.clearDisplay();
		LCD.drawString("      Delay    ", 0, 0);
		LCD.drawString("  10 unit = 1s ", 0, 1);
		LCD.drawString(" Default=25=2.5s", 0, 2);

		iButton = 0;
		int menuDelay = 25;

		while (iButton != Button.ID_ENTER) {

			LCD.drawString("                     ", 0, 4);
			LCD.drawString("  <|-| " + menuDelay + " |+|>  ", 0, 4);

			iButton = Button.waitForPress();

			if (iButton == Button.ID_LEFT) {

				if (menuDelay > 0)
					menuDelay -= 5;

			} else if (iButton == Button.ID_RIGHT) {

				if (menuDelay < 100)
					menuDelay += 5;

			} else if (iButton == Button.ID_ENTER) {

				return menuDelay * 100;
			}
		}
		launcherDelay = menuDelay;
		return menuDelay;
	}
	
	
}