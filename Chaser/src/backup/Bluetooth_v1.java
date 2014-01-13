package backup;

/** Bluetooth.java
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 */

import lejos.nxt.comm.RConsole;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Bluetooth_v1 implements TimerListener {

	private Timer bluetoothTimer;
	private USLocalizer localizer = null;
	private Navigator nav = null;

	private int line;
	private final boolean printName = true;

	/**
	 * Initialize the Bluetooth reporter to get ready to report status.
	 * 
	 * @param localizer
	 *            object to get status from.
	 * @param bluetoothRefresh
	 *            refresh time after which it refreshes the status.
	 */
	public Bluetooth_v1(USLocalizer localizer, int bluetoothRefresh) {

		this.bluetoothTimer = new Timer(bluetoothRefresh, this);
		this.localizer = localizer;

	}

	public void timedOut() {
		if (line % 100 == 0) {

			if (localizer != null) {
				RConsole.println(System.currentTimeMillis() + "\t"
						+ localizer.reportStatus(printName));
				line++;
			}

			if (nav != null) {
				RConsole.println(System.currentTimeMillis() + "\t"
						+ nav.reportStatus(printName));
				line++;
			}
		}

		if (nav != null) {
			RConsole.println(System.currentTimeMillis() + "\t"
					+ nav.reportStatus(printName));
			line++;
		}

		if (nav != null) {
			RConsole.println(System.currentTimeMillis() + "\t"
					+ nav.reportStatus(!printName));
			line++;
		}
	}

	public void addToReport(Navigator nav) {
		this.nav = nav;
	}

	public void start() {
		RConsole.openBluetooth(60000);
		// start the timer
		bluetoothTimer.start();
	}

	public void stop() {
		bluetoothTimer.stop();
		RConsole.close();
	}

}
