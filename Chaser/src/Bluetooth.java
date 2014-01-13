/** Bluetooth.java
 * This class is used to report the status of every other classes through bluetooth. 
 * If a class implements BluetoothReporter, and is added to the Bluetooth object through the method .addToReport(),
 * this class will call the .reportStatus(boolean getName) according to the Timer set up.
 * .reportStatus(boolean getName) should display all the important info of the class while it is running.
 * 
 * @author Charles-Antoine de Salaberry
 * @version 3
 */

import java.util.ArrayList;
import java.util.List;

import lejos.nxt.comm.RConsole;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Bluetooth implements TimerListener {

	private Timer bluetoothTimer;

	private ArrayList<BluetoothReporter> toReport;

	private int line;
	private final boolean printName = true;

	/**
	 * Initialize the Bluetooth reporter to get ready to report status.
	 * 
	 * @param bluetoothRefresh
	 *            refresh time after which it refreshes the status.
	 */
	public Bluetooth(int bluetoothRefresh) {

		this.bluetoothTimer = new Timer(bluetoothRefresh, this);
		line = 0;
		toReport = new ArrayList<BluetoothReporter>();
	}

	
	/**
	 * Displays on RConsole the status of the BluetoothReporter added to this object.
	 */
	public void timedOut() {

		/*
		 * Sends the name of the value every 100 lines.
		 */
		if (line % 100 == 0) {
			for (BluetoothReporter a : toReport) {
				RConsole.println(a.reportStatus(printName));
				line++;
			}
		}
		
		/*
		 * Sends the status of every BluetoothReporter in toReport.
		 */
		for (BluetoothReporter a : toReport) {
			RConsole.println(a.reportStatus(!printName));
			line++;
		}
	}

	/**
	 * Adds reporter to the list of BluetoothReporter that currently reports through bluetooth.
	 */
	public void addToReport(BluetoothReporter reporter) {

		toReport.add(reporter);
	}

	/**
	 * Waits for connection with RConsole over bluetooth for 10 seconds, then starts reporting.
	 */
	public void start() {

		/*
		 * Listens for bluetooth connection during 10 seconds.
		 */
		RConsole.openBluetooth(10000);
		// start the timer
		bluetoothTimer.start();
	}
	
	/**
	 * Properly closes the connection with RConsole, then stops reporting.
	 */
	public void stop() {

		bluetoothTimer.stop();
		RConsole.close();
	}
}