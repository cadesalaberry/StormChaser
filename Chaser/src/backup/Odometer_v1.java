package backup;

import lejos.nxt.LCD;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Odometer_v1 implements TimerListener {
	public static final int LCD_REFRESH = 100;
	private Timer lcdTimer;

	private double x, y, theta;

	private Object lock;
	private NXTRegulatedMotor leftMotor, rightMotor;

	// robot's dimensions
	private double width = 16.8;
	private double radiusLeft = 2.68;
	private double radiusRight = 2.68;

	// variables instantiated for calculation
	private double theta1 = 0.0;
	private double theta2 = 0.0;
	private double thetaC = 0.0;
	private double tachoLeft1 = 0.0;
	private double tachoLeft2 = 0.0;
	private double tachoLeftC = 0.0;
	private double tachoRight1 = 0.0;
	private double tachoRight2 = 0.0;
	private double tachoRightC = 0.0;

	// variables calculated initialized
	private double arcLengthC;
	private double x2, x1;
	private double y2, y1;

	public Odometer_v1(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
		x = 0.0;
		y = 0.0;
		theta = 0.0;

		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		lock = new Object();
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		lcdTimer.start();
	}

	public void timedOut() {
		while (true) {

			tachoLeft2 = leftMotor.getTachoCount();
			tachoRight2 = rightMotor.getTachoCount();

			// compute and scale up tachoLeftC, tachoRightC
			tachoLeftC = (tachoLeft2 - tachoLeft1) * Math.PI / 180.0 * 100;
			tachoRightC = (tachoRight2 - tachoRight1) * Math.PI / 180.0 * 100;

			// compute delta theta and arcLengthC and scale down
			thetaC = ((tachoRightC * radiusRight) - (tachoLeftC * radiusLeft))
					/ -(width * 100);
			arcLengthC = (tachoRightC * radiusRight + tachoLeftC * radiusLeft) / 200;

			tachoLeft1 = tachoLeft2;
			tachoRight1 = tachoRight2;

			x2 = arcLengthC * Math.cos(theta1 + thetaC / 2.0);
			y2 = arcLengthC * Math.sin(theta1 + thetaC / 2.0);
			y1 += y2;
			theta1 = theta1 + thetaC;

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				x += x2;
				y = -y1;
				theta = -theta1;
				if (theta > 6.28) {
					theta = theta - 6.28;
				} else if (theta < -6.28) {
					theta = theta + 6.28;
				}
				LCD.drawString(formattedDoubleToString(x, 2), 0, 1);
				LCD.drawString(formattedDoubleToString(y, 2), 0, 2);
				LCD.drawString(formattedDoubleToString(theta, 2), 0, 3);
			}
		}
	}

	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {

			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;

		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";

		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long) x;
			if (t < 0)
				t = -t;

			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}

			result += stack;
		}

		// put the decimal, if needed
		if (places > 0) {
			result += ".";

			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long) x);
			}
		}

		return result;
	}

}
