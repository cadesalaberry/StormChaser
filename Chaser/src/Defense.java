/**
n  * @author : Nigel Kut 
 * Date : 03.04.12
 */

import lejos.nxt.NXTRegulatedMotor;

public class Defense {

	private NXTRegulatedMotor defenseMotor;
	private Navigator navigator;
	private RobotCtrl robotCtrl;
	private final static int defenseRotationAngle = Chaser.DEFENSE_ROTATION_ANGLE;
	private final static int defenseSpeed = Chaser.DEFENSE_SPEED;

	/**
	 * Default constructor
	 * 
	 * @param navigator
	 * @param motor
	 * @param robotCtrl
	 */
	public Defense(Navigator navigator, NXTRegulatedMotor motor,
			RobotCtrl robotCtrl) {

		this.navigator = navigator;
		this.defenseMotor = motor;
		this.robotCtrl = robotCtrl;

	}

	/**
	 * Make the Robot navigate to the closest node to the basket, then process to defend.
	 */
	public void start() {
		navigator.defineRestrictions(true);

		// node 69 should be the tile just in front of basket
		navigator.travelPath(navigator.searchNode(69));
		robotCtrl.turnTo(90);
		robotCtrl.goToIntersect();
		robotCtrl.turn(180);
		deployMechanism();
		robotCtrl.goBackward(25);

	}

	/**
	 * Deploys the defense mechanism by turning the motor.
	 */
	public void deployMechanism() {
		defenseMotor.setSpeed(defenseSpeed);
		defenseMotor.rotate(defenseRotationAngle);
	}

	/**
	 * Undeploys the defense system by turning the motor inversely.
	 */
	public void undeployMechanism() {
		defenseMotor.rotate(-defenseRotationAngle);
	}

}
