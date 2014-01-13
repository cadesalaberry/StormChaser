import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;

public class RS485Transmiter {

	DataInputStream dis;
	DataOutputStream dos;
	NXTConnection con;

	public RS485Transmiter() {

	}

	/**
	 * Send a double to Storm. wait for it to come back.
	 * @param distance
	 * @throws Exception
	 */
	public void transmit(double distance) throws Exception {

		try {
			LCD.drawString("write: " + distance, 0, 6);
			dos.writeInt((int) distance);
			dos.flush();
		} catch (IOException ioe) {
			LCD.drawString("Write Exception", 0, 5);
		}

		try {
			int status = dis.readInt();
			LCD.drawString("Read: " + status, 0, 7);
			if (status == distance) {

			}
		} catch (IOException ioe) {
			LCD.drawString("Read Exception ", 0, 5);
		}
	}
	/**
	 * Initialize the connection with Storm.
	 */
	public void start() {
		LCD.clear();
		LCD.drawString("Name: " + "Storm", 0, 0);
		LCD.drawString("Type: " + "RS485", 0, 1);
		LCD.drawString("Mode: " + "Raw", 0, 2);
		LCD.drawString("Connecting...", 0, 3);

		con = RS485.getConnector().connect("Storm", NXTConnection.RAW);

		if (con == null) {
			LCD.drawString("Connect fail", 0, 5);
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
			Sound.buzz();
			System.exit(1);
		}

		Sound.twoBeeps();

		LCD.drawString("Connected       ", 0, 3);
		LCD.refresh();
		dis = con.openDataInputStream();
		dos = con.openDataOutputStream();
	}

	/**
	 * Properly closes the connection with Storm.
	 */
	public void stop() {
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