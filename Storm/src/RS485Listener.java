import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.util.TextMenu;

import java.io.*;

public class RS485Listener {

	Launcher launcher;

	int intRead;

	DataInputStream dis;
	DataOutputStream dos;
	NXTConnection con;

	public RS485Listener(Launcher launcher) {
		this.launcher = launcher;
	}

	/**
	 * Start listening for connection from Chaser.
	 */
	public void start() {
		LCD.clear();
		LCD.drawString("Type: " + "RS485", 0, 0);
		LCD.drawString("Mode: " + "Raw", 0, 1);
		LCD.drawString("Waiting...", 0, 2);

		con = RS485.getConnector().waitForConnection(60000, NXTConnection.RAW);

		LCD.drawString("Connected...", 0, 2);

		dis = con.openDataInputStream();
		dos = con.openDataOutputStream();
	}

	/**
	 * Listen for a certain number of data from Chaser. the default is 100.
	 * @param numberOfBalls
	 */
	public void listen(int numberOfBalls) {

		LCD.clear();
		for (int i = 0; i < numberOfBalls; i++) {
			intRead = 0;

			/*
			 * Listen for the transmiter to send a distance to shoot at.
			 */

			try {
				intRead = dis.readInt();
			} catch (Exception e) {
				break;
			}

			/*
			 * Confirm that the value has been received by displaying on the
			 * screen.
			 */
			LCD.drawString("Read: " + intRead, 0, 2);

			/*
			 * Make the robot shoot at the received distance.
			 */
			launcher.shootAtSpeed(intRead);
			
			/*
			 * Confirm that the launcher has been triggered.
			 */
			try {
				dos.writeInt(intRead);
				dos.flush();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Properly closes the connection with the other Brick.
	 */
	public void stop() {

		LCD.clear();
		try {
			LCD.drawString("Closing...    ", 0, 3);
			dis.close();
			dos.close();
			con.close();
		} catch (IOException ioe) {
			LCD.drawString("Close Exception", 0, 5);
			LCD.refresh();
		}

		LCD.drawString("Finished        ", 0, 3);

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
	}
}