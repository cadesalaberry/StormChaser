package backup;
import BluetoothReporter;
import Chaser;
import lejos.nxt.LCD;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Odometer_v2 implements TimerListener, BluetoothReporter {

	private final static double width = Chaser.WIDTH;
	private final static double radiusLeft = Chaser.LEFT_RADIUS;
	private final static double radiusRight = Chaser.RIGHT_RADIUS;
	private final static int ODOMETER_REFRESH = 50;

	private Timer odometerTimer;
	private Object lock;
	private NXTRegulatedMotor leftMotor, rightMotor;

	// variables instantiated for calculation
	private double x, y, theta, thetaNotCorrected;
	private double thetaC = 0.0;
	private double thetaD = 0.0;
	private double tachoLeft1 = 0.0;
	private double tachoLeft2 = 0.0;
	private double tachoLeftC = 0.0;
	private double tachoRight1 = 0.0;
	private double tachoRight2 = 0.0;
	private double tachoRightC = 0.0;
	private double arcLengthC;
	private double x2,y2;
	

	public Odometer_v2(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
		x = 0;
		y = 0;
		theta = 0;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		lock = new Object();
		this.odometerTimer = new Timer(ODOMETER_REFRESH, this);
		odometerTimer.start();
	}

	public void timedOut() {

		tachoLeft2 = leftMotor.getTachoCount();
		tachoRight2 = rightMotor.getTachoCount();

		// compute and scale up tachoLeftC, tachoRightC
		tachoLeftC = (tachoLeft2 - tachoLeft1) * Math.PI / 180.0 * 100;
		tachoRightC = (tachoRight2 - tachoRight1) * Math.PI / 180.0 * 100;

		// compute delta theta and arcLengthC and scale down
		thetaC = ((tachoRightC * radiusRight) - (tachoLeftC * radiusLeft))
				/ (width * 100.0);
		arcLengthC = (tachoRightC * radiusRight + tachoLeftC * radiusLeft) / 200;

		tachoLeft1 = tachoLeft2;
		tachoRight1 = tachoRight2;

		x2 = arcLengthC * Math.cos(theta + thetaC / 2.0);
		y2 = arcLengthC * Math.sin(theta + thetaC / 2.0);
		x += x2;
		y += y2;
		theta += thetaC;

		synchronized (lock) {

			
			
			// convert to degrees
			thetaD = theta  * 180.0 / Math.PI;
			
			thetaNotCorrected = thetaD;
			// wrap around 360
			if (thetaD >= 360) {
				thetaD -= 360;
			} else if (thetaD < 0) {
				thetaD += 360;
			}
			LCD.drawString(formattedDoubleToString(x, 2), 0, 1);
			LCD.drawString(formattedDoubleToString(y, 2), 0, 2);
			LCD.drawString(formattedDoubleToString(thetaD, 2), 0, 3);
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
				position[2] = thetaD;
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

	// return theta in degrees
	public double getTheta() {
		double result;
		synchronized (lock) {
			result = thetaD;
		}
		return result;
	}

	public int getHeading() {
		return (int) getTheta();
	}

	public NXTRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}

	public NXTRegulatedMotor getRightMotor() {
		return this.rightMotor;
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
				theta = (position[2]*Math.PI/180.0);
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
			this.theta = theta * Math.PI/180.0;
		}
	}
	
	// <------------------- I CHANGED THIS TO PUBLIC!!! (To print Strings in OdoCorrection
	public static String formattedDoubleToString(double x, int places) { 
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
	
	public String reportStatus(boolean printName){
		String status;

		if (printName) {

			status = new String("x");
			status += "\ty";
			status += "\ttheta";

		} else {

			status = new String("" + (int) getX());
			status += "\t" + (int) getY();
			status += "\t" + (int) getTheta();
		}

		return status;
	}

}
